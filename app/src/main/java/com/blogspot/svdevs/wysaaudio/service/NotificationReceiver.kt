package com.blogspot.svdevs.wysaaudio.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.blogspot.svdevs.wysaaudio.utils.Constants.ACTION_STOP
import kotlin.system.exitProcess

class NotificationReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action) {
            ACTION_STOP -> {
               exitProcess(1)
            }
        }
    }
}