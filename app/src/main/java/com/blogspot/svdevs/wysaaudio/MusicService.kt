package com.blogspot.svdevs.wysaaudio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.blogspot.svdevs.wysaaudio.Constants.ACTION_PAUSE
import com.blogspot.svdevs.wysaaudio.Constants.ACTION_SHOW_MUSIC_ACTIVITY
import com.blogspot.svdevs.wysaaudio.Constants.ACTION_START
import com.blogspot.svdevs.wysaaudio.Constants.ACTION_STOP
import com.blogspot.svdevs.wysaaudio.Constants.AUDIO_SOURCE
import com.blogspot.svdevs.wysaaudio.Constants.NOTIFICATION_CHANNEL_ID
import com.blogspot.svdevs.wysaaudio.Constants.NOTIFICATION_CHANNEL_NAME
import com.blogspot.svdevs.wysaaudio.Constants.SERVICE_ID

class MusicService: LifecycleService() {

    companion object {
        val isPlaying = MutableLiveData<Boolean>()
        var mediaPlayer: MediaPlayer? = null
    }

    private lateinit var mediaSession: MediaSessionCompat

    override fun onCreate() {
        super.onCreate()

        //Initializing the media player
        mediaPlayer = MediaPlayer.create(this, Uri.parse(AUDIO_SOURCE))

        // initial value of live data
        isPlaying.postValue(false)

        // media session
        mediaSession = MediaSessionCompat(this, "MY MUSIC")
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START -> {
                    startService()
                }
                ACTION_PAUSE -> {
                    pauseService()
                }
                ACTION_STOP -> {
                    destroyService()
                }
                else -> {
                    Log.d("SERVICE", "UNKNOWN COMMAND: ")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // Start service
    private fun startService() {
        isPlaying.postValue(true)
        mediaPlayer?.start()
        showNotification()

    }

    // Pause service
    private fun pauseService() {
        if (mediaPlayer?.isPlaying == true) {
            isPlaying.postValue(false)
            mediaPlayer?.pause()
        }
    }

    // Destroy service
    private fun destroyService() {
        isPlaying.postValue(false)
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        mediaPlayer?.release()
        stopForeground(true)
        stopSelf()
    }

    // Display notification
    fun showNotification() {

        // system service used when working with notifications
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)

//        val pauseIntent = Intent(this,NotificationReceiver::class.java).setAction(ACTION_PAUSE)
//        val pausePendingIntent = PendingIntent.getBroadcast(this,0,pauseIntent,PendingIntent.FLAG_UPDATE_CURRENT)
//
//        val playIntent = Intent(this,NotificationReceiver::class.java).setAction(ACTION_START)
//        val playPendingIntent = PendingIntent.getBroadcast(this,0,playIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        val stopIntent = Intent(this, NotificationReceiver::class.java).setAction(ACTION_STOP)
        val stopPendingIntent =
            PendingIntent.getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        //create actual notification
        val baseNotificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.play)
            .setContentTitle("Wysa Audio")
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
            )
            .setContentIntent(getMainActivityPendingIntent()) // nav to app on notification click
            .addAction(R.drawable.stop, "Stop", stopPendingIntent)
//            .addAction(R.drawable.play, "Play", playPendingIntent)
//            .addAction(R.drawable.pause, "Pause", pausePendingIntent)


        startForeground(SERVICE_ID, baseNotificationBuilder.build())

    }

    //pending intent function
    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_MUSIC_ACTIVITY
        },
        PendingIntent.FLAG_UPDATE_CURRENT
    )
}