package com.blogspot.svdevs.wysaaudio

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.blogspot.svdevs.wysaaudio.utils.Constants.ACTION_PAUSE
import com.blogspot.svdevs.wysaaudio.utils.Constants.ACTION_START
import com.blogspot.svdevs.wysaaudio.utils.Constants.ACTION_STOP
import com.blogspot.svdevs.wysaaudio.service.MusicService.Companion.mediaPlayer
import com.blogspot.svdevs.wysaaudio.databinding.ActivityMainBinding
import com.blogspot.svdevs.wysaaudio.service.MusicService

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    companion object {
        var isPlayingMain = false
//        var mediaPlayer: MediaPlayer? = null
        //var mediaPlayer: MediaPlayer? = MusicService.mediaPlayer
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
        }
        mediaPlayer?.setOnCompletionListener {
            sendCommandToService(ACTION_STOP)
        }
    }

    private fun subscribeToObserver() {
        MusicService.isPlaying.observe(this, Observer {
            updatePlaying(it)
        })
    }

    private fun updatePlaying(isPlaying: Boolean?) {

//        if(mediaPlayer==null) {
////            mediaPlayer = MediaPlayer()
//            MediaPlayer.create(this, Uri.parse(Constants.AUDIO_SOURCE))
//        }
//        mediaPlayer!!.reset()
//        mediaPlayer!!.setDataSource(this, Uri.parse(Constants.AUDIO_SOURCE))
//        mediaPlayer!!.prepare()
//        mediaPlayer!!.start()


//        this.isPlayingMain = isPlaying!!
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