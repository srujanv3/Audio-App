package com.blogspot.svdevs.wysaaudio.service

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.media.session.MediaButtonReceiver
import androidx.media.session.MediaButtonReceiver.handleIntent
import com.blogspot.svdevs.wysaaudio.R
import com.blogspot.svdevs.wysaaudio.ui.MainActivity.Companion.binding
import com.blogspot.svdevs.wysaaudio.utils.Constants.ACTION_PAUSE
import com.blogspot.svdevs.wysaaudio.utils.Constants.ACTION_START
import com.blogspot.svdevs.wysaaudio.utils.Constants.ACTION_STOP
import com.blogspot.svdevs.wysaaudio.utils.Constants.AUDIO_SOURCE
import com.blogspot.svdevs.wysaaudio.utils.Constants.SERVICE_ID
import com.blogspot.svdevs.wysaaudio.utils.NetworkUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.O)
class MusicService : LifecycleService() {

    private lateinit var runnable: Runnable

    private lateinit var audioManager: AudioManager
    private lateinit var focusRequest: AudioFocusRequest

    @Inject
    lateinit var playbackAttrib: AudioAttributes

    companion object {
        val isPlaying = MutableLiveData<Boolean>()
        var mediaPlayer: MediaPlayer? = null
        var isServiceDestroyed = true // check is service is not destroyed for play pause feature

    }

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    lateinit var currentNotificationBuilder: NotificationCompat.Builder

    private lateinit var mediaSession: MediaSessionCompat

    val audioFocusListener = AudioManager.OnAudioFocusChangeListener {

        when (it) {
            AudioManager.AUDIOFOCUS_GAIN -> startService()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pauseService()
            AudioManager.AUDIOFOCUS_LOSS -> pauseService()
        }
    }

    override fun onCreate() {
        super.onCreate()

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
            setAudioAttributes(playbackAttrib)
            setAcceptsDelayedFocusGain(true)
            setOnAudioFocusChangeListener(audioFocusListener)
            build()
        }

        //Initializing the media player
        mediaPlayer = MediaPlayer.create(this, Uri.parse(AUDIO_SOURCE))

        // initial value of live data
        isPlaying.postValue(false)

        //init notification builder
        currentNotificationBuilder = baseNotificationBuilder

        // media session
        mediaSession = MediaSessionCompat(baseContext, "MY MUSIC")

        // init seekbar
        binding.seekBar.progress = 0
        binding.seekBar.max = mediaPlayer!!.duration

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        val audioFocus = audioManager.requestAudioFocus(focusRequest)

        intent?.let {
            when (it.action) {
                ACTION_START -> {
                    if (audioFocus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        startService()
                    }
                    isPlaying.observe(this, Observer {
                        showNotification(it)
                    })
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

        MediaButtonReceiver.handleIntent(mediaSession, intent)

        return super.onStartCommand(intent, flags, startId)
    }

    // Start service
     fun startService() {
        isPlaying.postValue(true)
        mediaPlayer?.start()
        isServiceDestroyed = false

        // trigger seekbar
        seekbarSetup()

    }

    // Pause service
     fun pauseService() {
        if (mediaPlayer?.isPlaying == true) {
            isPlaying.postValue(false)
            mediaPlayer?.pause()
            isServiceDestroyed = false
        }
    }

    // Destroy service
     fun destroyService() {

        // release audio focus
        audioManager.abandonAudioFocusRequest(focusRequest)

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
    fun showNotification(isPlaying: Boolean) {

        // action handling part

        //remove previous actions before updating the notification
        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }


        // play / pause from notification
        val pendingIntent = if (isPlaying) {
            //pause the service
            val pauseIntent = Intent(this, MusicService::class.java).apply {
                action = ACTION_PAUSE
            }
            PendingIntent.getService(this, 0, pauseIntent, FLAG_UPDATE_CURRENT)
        } else {
            // resume the service
            val resumeIntent = Intent(this, MusicService::class.java).apply {
                action = ACTION_START
            }
            PendingIntent.getService(this, 1, resumeIntent, FLAG_UPDATE_CURRENT)
        }

        val actionIcon = if (isPlaying) {
            R.drawable.pause
        } else {
            R.drawable.play
        }

        val stopIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent =
            PendingIntent.getBroadcast(this, 2, stopIntent, FLAG_UPDATE_CURRENT)


        currentNotificationBuilder = baseNotificationBuilder
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
            )
            .addAction(actionIcon, "Pause", pendingIntent)
            .addAction(R.drawable.stop, "Stop", stopPendingIntent)


        val playbackSpeed: Float = if (isPlaying) 0F else 1F

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mediaSession.setMetadata(
                MediaMetadataCompat.Builder()
                    .putLong(
                        MediaMetadataCompat.METADATA_KEY_DURATION,
                        mediaPlayer!!.duration.toLong()
                    )
                    .build()
            )
            mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setState(
                        PlaybackStateCompat.STATE_PLAYING,
                        mediaPlayer!!.currentPosition.toLong(),
                        playbackSpeed
                    )
                    .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                    .build()
            )

        }


        startForeground(SERVICE_ID, currentNotificationBuilder.build())

    }

    private fun seekbarSetup() {
        // reset seekbar when new audio start playing
//        binding.seekBar.progress = 0
//        binding.seekBar.max = mediaPlayer!!.duration

        runnable = Runnable {
            if (!isServiceDestroyed) {
                binding.seekBar.progress = mediaPlayer!!.currentPosition
                Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
            }
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

}
