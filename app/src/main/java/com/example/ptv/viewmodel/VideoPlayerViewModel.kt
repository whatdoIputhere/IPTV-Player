package com.example.ptv.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class VideoPlayerViewModel(application: Application) : AndroidViewModel(application) {
    val exoPlayer: ExoPlayer

    init {
        val appContext = application.applicationContext

        val logging = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {}
        }).apply { level = HttpLoggingInterceptor.Level.BASIC }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        val dataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(20000, 120000, 2000, 5000)
            .build()

        exoPlayer = ExoPlayer.Builder(appContext)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .build()
    }

    fun setUrl(url: String) {
        val currentUri = exoPlayer.currentMediaItem?.localConfiguration?.uri?.toString()
        if (currentUri == url) {
            if (!exoPlayer.playWhenReady && !exoPlayer.isPlaying) {
                try {
                    exoPlayer.playWhenReady = true
                    exoPlayer.play()
                } catch (_: Throwable) {
                }
            }
            return
        }

        val mediaItemBuilder = MediaItem.Builder().setUri(url)
        if (url.contains(".m3u8", ignoreCase = true)) {
            mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_M3U8)
        }

        val wasPlaying = exoPlayer.playWhenReady
        val prevPosition = exoPlayer.currentPosition

        exoPlayer.setMediaItem(mediaItemBuilder.build())
        try {
            exoPlayer.playWhenReady = true
            exoPlayer.prepare()
            exoPlayer.play()
        } catch (_: Throwable) {
        }
        if (wasPlaying && prevPosition > 0) {
            try {
                exoPlayer.seekTo(prevPosition)
            } catch (_: Throwable) {
            }
        }
    }

    override fun onCleared() {
        try {
            exoPlayer.release()
        } catch (t: Throwable) {
        }
        super.onCleared()
    }
}
