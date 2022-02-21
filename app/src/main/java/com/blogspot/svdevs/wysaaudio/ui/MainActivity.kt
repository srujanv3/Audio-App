package com.blogspot.svdevs.wysaaudio.ui

import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.blogspot.svdevs.wysaaudio.databinding.ActivityMainBinding
import com.blogspot.svdevs.wysaaudio.service.MusicService
import com.blogspot.svdevs.wysaaudio.service.MusicService.Companion.mediaPlayer
import com.blogspot.svdevs.wysaaudio.utils.Constants.ACTION_PAUSE
import com.blogspot.svdevs.wysaaudio.utils.Constants.ACTION_START
import com.blogspot.svdevs.wysaaudio.utils.Constants.ACTION_STOP

class MainActivity : AppCompatActivity() {

    companion object {
        var isPlayingMain = false
        lateinit var binding: ActivityMainBinding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Screen display adjustments
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

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

            // init seekbar
            if(MusicService.mediaPlayer != null){
                // to handle seekbar when launched on Notification click
                seekBar.max = mediaPlayer!!.duration
            }



            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if(!MusicService.isServiceDestroyed){ // issue fixed !!!
                        if(MusicService.mediaPlayer != null && fromUser) {
                            MusicService.mediaPlayer!!.seekTo(progress)
                        }
                    }

                }

                override fun onStartTrackingTouch(seekBar: SeekBar?)  = Unit

                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            })
        }

//        mediaPlayer?.setOnCompletionListener {
//            mediaPlayer!!.release()
//
//            updatePlaying(isPlaying = false)
//        }
    }

    private fun subscribeToObserver() {
        MusicService.isPlaying.observe(this, Observer {
            updatePlaying(it)
        })
    }

    // update play & pause buttons
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
}