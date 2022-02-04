package com.blogspot.svdevs.wysaaudio.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import com.blogspot.svdevs.wysaaudio.MainActivity
import com.blogspot.svdevs.wysaaudio.R
import com.blogspot.svdevs.wysaaudio.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped


@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @Provides
    @ServiceScoped
    fun provideMainActivityPendingIntent(@ApplicationContext app: Context) = PendingIntent.getActivity(
    app,
    0,
    Intent(app, MainActivity::class.java).also {
        it.action = Constants.ACTION_SHOW_MUSIC_ACTIVITY
    },
    PendingIntent.FLAG_UPDATE_CURRENT
    )


    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(@ApplicationContext app: Context, pendingIntent: PendingIntent) =
     NotificationCompat.Builder(app,
        Constants.NOTIFICATION_CHANNEL_ID
    )
        .setAutoCancel(false)
        .setOngoing(true)
        .setSmallIcon(R.drawable.play)
//    .setLargeIcon(
//        BitmapFactory.decodeResource(resources,
//    R.drawable.music
//    ))
    .setContentTitle("Wysa Audio")
//    .setStyle(
//    androidx.media.app.NotificationCompat.MediaStyle()
//    .setMediaSession(mediaSession.sessionToken)
//    )
    .setVisibility(VISIBILITY_PUBLIC)
    .setContentIntent(pendingIntent) // nav to app on notification click
//    .addAction(R.drawable.stop, "Stop", stopPendingIntent)
//    .addAction(R.drawable.play, "Play", null)
//    .addAction(R.drawable.pause, "Pause", null)

}