package com.blogspot.svdevs.wysaaudio.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import com.blogspot.svdevs.wysaaudio.R
import com.blogspot.svdevs.wysaaudio.ui.MainActivity
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
    fun provideMainActivityPendingIntent(@ApplicationContext app: Context) =
        PendingIntent.getActivity(
            app,
            0,
            Intent(app, MainActivity::class.java),
           0
        )


    @ServiceScoped
    @Provides
    fun provideBaseNotificationCompatBuilder(
        @ApplicationContext app: Context,
        pendingIntent: PendingIntent
    ) =
        NotificationCompat.Builder(
            app,
            Constants.NOTIFICATION_CHANNEL_ID
        )
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.music)
            .setContentTitle("Wysa Audio")
            .setContentText("Now playing ...")
            .setPriority(PRIORITY_HIGH)
            .setLargeIcon(BitmapFactory.decodeResource(app.resources, R.drawable.new_icon))
            .setVisibility(VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent) // nav to app on notification click

    @ServiceScoped
    @Provides
    fun providePlaybackAttributes() = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()
}