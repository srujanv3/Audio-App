package com.blogspot.svdevs.wysaaudio.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.blogspot.svdevs.wysaaudio.ui.MainActivity.Companion.binding
import com.blogspot.svdevs.wysaaudio.R
import com.blogspot.svdevs.wysaaudio.utils.Constants.ACTION_PAUSE
import com.blogspot.svdevs.wysaaudio.utils.Constants.ACTION_START
import com.blogspot.svdevs.wysaaudio.utils.Constants.ACTION_STOP
import com.blogspot.svdevs.wysaaudio.utils.Constants.AUDIO_SOURCE
import com.blogspot.svdevs.wysaaudio.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.blogspot.svdevs.wysaaudio.utils.Constants.NOTIFICATION_CHANNEL_NAME
import com.blogspot.svdevs.wysaaudio.utils.Constants.SERVICE_ID
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MusicService: LifecycleService() {

    var isServiceDestroyed = true // check is service is not destroyed for play pause feature
    private lateinit var runnable: Runnable

    companion object {
        val isPlaying = MutableLiveData<Boolean>()
        var mediaPlayer: MediaPlayer? = null
    }

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    lateinit var currentNotificationBuilder: NotificationCompat.Builder

    private lateinit var mediaSession: MediaSessionCompat

    override fun onCreate() {
        super.onCreate()

        //Initializing the media player
        mediaPlayer = MediaPlayer.create(this, Uri.parse(AUDIO_SOURCE))

        // initial value of live data
        isPlaying.postValue(false)

        //init notification builder
        currentNotificationBuilder = baseNotificationBuilder

        // media session
        mediaSession = MediaSessionCompat(this, "MY MUSIC")

        // show notification when service is created
        showNotification()

        // init seekbar
        binding.seekBar.progress = 0
        binding.seekBar.max = mediaPlayer!!.duration
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
        isServiceDestroyed = false

        // trigger seekbar
        seekbarSetup()

    }

    // Pause service
    private fun pauseService() {
        if (mediaPlayer?.isPlaying == true) {
            isPlaying.postValue(false)
            mediaPlayer?.pause()
            isServiceDestroyed = false
        }
    }

    // Destroy service
    private fun destroyService() {
        isPlaying.postValue(false)
        isServiceDestroyed = true
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        mediaPlayer?.release()
        stopForeground(true)
        stopSelf()

        // reset seekbar
        isServiceDestroyed = true
        binding.seekBar.progress = 0
    }

    // Display notification
    fun showNotification() {

        // system service used when working with notifications
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        // action handling part
        val stopIntent = Intent(this, NotificationReceiver::class.java).setAction(ACTION_STOP)
        val stopPendingIntent =
            PendingIntent.getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        currentNotificationBuilder = baseNotificationBuilder
            .addAction(R.drawable.stop, "Stop", stopPendingIntent)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
            )
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.music))

        notificationManager.notify(SERVICE_ID, currentNotificationBuilder.build())

    }

    // Create notification channel
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)

        startForeground(SERVICE_ID, baseNotificationBuilder.build())

    }

    private fun seekbarSetup() {
        // reset seekbar when new audio start playing
//        binding.seekBar.progress = 0
//        binding.seekBar.max = mediaPlayer!!.duration

        runnable = Runnable {
            if(!isServiceDestroyed) {
                binding.seekBar.progress = mediaPlayer!!.currentPosition
                Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
            }
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }
}
