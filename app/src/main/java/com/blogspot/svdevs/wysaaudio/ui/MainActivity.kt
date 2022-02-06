package com.blogspot.svdevs.wysaaudio.ui

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.blogspot.svdevs.wysaaudio.utils.Constants.ACTION_PAUSE
import com.blogspot.svdevs.wysaaudio.utils.Constants.ACTION_START
import com.blogspot.svdevs.wysaaudio.utils.Constants.ACTION_STOP
import com.blogspot.svdevs.wysaaudio.databinding.ActivityMainBinding
import com.blogspot.svdevs.wysaaudio.service.MusicService

class MainActivity : AppCompatActivity(), MediaPlayer.OnCompletionListener {


    companion object {
        var isPlayingMain = false
        lateinit var binding: ActivityMainBinding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Observing live data
        subscribeToObserver()

        binding.apply {
            play.setOnClickListener {
                sendCommandToService(ACTION_START)
             }
            pause.setOnClickListener {
                sendCommandToService(ACTION_PAUSE)
            }
            stop.setOnClickListener {
                sendCommandToService(ACTION_STOP)
            }
//                    seekBar.apply {
//                    progress = 0
//                    max = mediaPlayer!!.duration
//                }

//            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//                override fun onProgressChanged(
//                    seekBar: SeekBar?,
//                    progress: Int,
//                    fromUser: Boolean
//                ) {
//                    if(fromUser) {
//                        MusicService.mediaPlayer!!.seekTo(progress)
//                    }
//                }
//
//                override fun onStartTrackingTouch(seekBar: SeekBar?)  = Unit
//
//                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
//            })
        }

    }

    private fun subscribeToObserver() {
        MusicService.isPlaying.observe(this, Observer {
            updatePlaying(it)
        })
    }

    private fun updatePlaying(isPlaying: Boolean?) {

          isPlayingMain = isPlaying!!

        if (!isPlaying) {
            //service not playing
            binding.pause.visibility = View.GONE
            binding.play.visibility = View.VISIBLE

        } else {
            //service playing
            binding.play.visibility = View.GONE
            binding.pause.visibility = View.VISIBLE
        }

    }

    private fun sendCommandToService(action:String){
        Intent(this, MusicService::class.java).also {
            it.action = action
            this.startService(it)
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        sendCommandToService(ACTION_STOP)
    }
}