package com.whatdoiputhere.iptvplayer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

@UnstableApi
class VideoPlayerViewModel(
    application: Application,
) : AndroidViewModel(application) {
    var exoPlayer: ExoPlayer
        private set

    init {
        val appContext = application.applicationContext

        val logging =
            HttpLoggingInterceptor(
                object : HttpLoggingInterceptor.Logger {
                    override fun log(message: String) {}
                },
            ).apply { level = HttpLoggingInterceptor.Level.BASIC }

        val okHttpClient =
            OkHttpClient
                .Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

        val dataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

        val loadControl =
            DefaultLoadControl
                .Builder()
                .setBufferDurationsMs(20000, 120000, 2000, 5000)
                .build()

        exoPlayer = buildPlayer(appContext, mediaSourceFactory, loadControl)
    }

    private fun buildPlayer(
        appContext: android.content.Context,
        mediaSourceFactory: DefaultMediaSourceFactory,
        loadControl: DefaultLoadControl,
    ): ExoPlayer =
        ExoPlayer
            .Builder(appContext)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .build()

    fun setUrl(url: String) {
        val currentUri =
            exoPlayer.currentMediaItem
                ?.localConfiguration
                ?.uri
                ?.toString()
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

    fun stopPlayback(
        clear: Boolean = false,
        release: Boolean = false,
    ) {
        try {
            exoPlayer.playWhenReady = false
            exoPlayer.stop()
            if (clear) {
                exoPlayer.clearMediaItems()
            }
            if (release) {
                try {
                    exoPlayer.release()
                } finally {
                    val appContext = getApplication<Application>().applicationContext

                    val logging =
                        okhttp3.logging
                            .HttpLoggingInterceptor(
                                object : okhttp3.logging.HttpLoggingInterceptor.Logger {
                                    override fun log(message: String) {}
                                },
                            ).apply { level = okhttp3.logging.HttpLoggingInterceptor.Level.BASIC }
                    val okHttpClient =
                        okhttp3.OkHttpClient
                            .Builder()
                            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                            .addInterceptor(logging)
                            .build()
                    val dataSourceFactory =
                        androidx.media3.datasource.okhttp.OkHttpDataSource
                            .Factory(okHttpClient)
                    val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
                    val loadControl =
                        DefaultLoadControl
                            .Builder()
                            .setBufferDurationsMs(20000, 120000, 2000, 5000)
                            .build()
                    exoPlayer = buildPlayer(appContext, mediaSourceFactory, loadControl)
                }
            }
        } catch (_: Throwable) {
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
