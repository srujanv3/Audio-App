package com.blogspot.svdevs.wysaaudio.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.KeyEvent
import androidx.annotation.RequiresApi
import com.blogspot.svdevs.wysaaudio.service.MusicService.Companion.mediaPlayer
import com.blogspot.svdevs.wysaaudio.ui.MainActivity
import com.blogspot.svdevs.wysaaudio.ui.MainActivity.Companion.musicService
import com.blogspot.svdevs.wysaaudio.utils.Constants.ACTION_STOP
import com.blogspot.svdevs.wysaaudio.utils.NetworkUtil
import kotlin.system.exitProcess

class NotificationReceiver: BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action) {
            ACTION_STOP -> {
               exitProcess(1)
            }
        }

        val event: KeyEvent? = intent!!.getParcelableExtra(Intent.EXTRA_KEY_EVENT) as KeyEvent?
        if (event?.getAction() !== KeyEvent.ACTION_DOWN) return

        when (event.keyCode) {
            KeyEvent.KEYCODE_MEDIA_STOP -> { exitProcess(1) }
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {if (mediaPlayer!!.isPlaying) musicService.pauseService() else
            musicService.startService()}
//            KeyEvent.KEYCODE_MEDIA_NEXT -> {}
//            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {}
        }

    }
}