package com.whatdoiputhere.iptvplayer.ui.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.whatdoiputhere.iptvplayer.R
import com.whatdoiputhere.iptvplayer.model.Channel
import com.whatdoiputhere.iptvplayer.viewmodel.VideoPlayerViewModel
import kotlinx.coroutines.delay

@UnstableApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun videoPlayerScreen(
    channel: Channel,
    onBackClick: () -> Unit,
    orientationBeforeStream: Int? = null,
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val tag = "VideoPlayerScreen"

    DisposableEffect(activity) {
        val window = activity?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, it.decorView) }

        controller?.let {
            it.hide(WindowInsetsCompat.Type.systemBars())
            it.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        onDispose {
            controller?.show(WindowInsetsCompat.Type.systemBars())
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    var httpErrorCode by remember { mutableStateOf<Int?>(null) }

    val vm: VideoPlayerViewModel =
        androidx.lifecycle.viewmodel.compose
            .viewModel()
    val exoPlayer = vm.exoPlayer

    LaunchedEffect(channel.url) {
        vm.setUrl(channel.url)
    }

    DisposableEffect(Unit) {
        onDispose {
            vm.stopPlayback(clear = true, release = true)
        }
    }

    fun restoreAndUnlock(prevConfig: Int?) {
        prevConfig?.let { prev ->
            val mapped =
                if (prev == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                    android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                } else {
                    android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            activity?.requestedOrientation = mapped
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }, 300L)
        }
    }

    BackHandler {
        vm.stopPlayback(clear = true)
        restoreAndUnlock(orientationBeforeStream)
        onBackClick()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_STOP) {
                    vm.stopPlayback()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var controlsVisible by remember { mutableStateOf(true) }
    var rotationLocked by remember { mutableStateOf(false) }

    LaunchedEffect(controlsVisible) {
        if (controlsVisible) {
            delay(3000L)
            controlsVisible = false
        }
    }

    LaunchedEffect(exoPlayer) {
        while (true) {
            try {
                val pos = exoPlayer.currentPosition
                val bufferedPos = exoPlayer.bufferedPosition
                val bufferedPct = exoPlayer.bufferedPercentage
                val isLoading = exoPlayer.isLoading
                val state = exoPlayer.playbackState
                Log.d(tag, "bufferMetrics: pos=$pos bufferedPos=$bufferedPos bufferedPct=$bufferedPct isLoading=$isLoading state=$state")
            } catch (t: Throwable) {
                Log.w(tag, "bufferMetrics: exception: ${t.message}")
            }
            delay(1000L)
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black),
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    setControllerShowTimeoutMs(3000)
                    setControllerVisibilityListener(
                        PlayerView.ControllerVisibilityListener { visible ->
                            controlsVisible = (visible == android.view.View.VISIBLE)
                        },
                    )
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                view.setControllerShowTimeoutMs(3000)
                view.setControllerVisibilityListener(
                    PlayerView.ControllerVisibilityListener { visible ->
                        controlsVisible = (visible == android.view.View.VISIBLE)
                    },
                )
            },
        )

        if (controlsVisible) {
            Card(
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.7f),
                    ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = {
                            vm.stopPlayback(clear = true)
                            restoreAndUnlock(orientationBeforeStream)
                            onBackClick()
                        },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back),
                        )
                    }

                    IconButton(
                        onClick = {
                            val orientation = context.resources.configuration.orientation
                            val newOrientation =
                                if (orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                                    android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                } else {
                                    android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                }
                            activity?.requestedOrientation = newOrientation
                            if (!rotationLocked) {
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                                }, 3000L)
                            }
                            controlsVisible = false
                        },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.rotate),
                            contentDescription = stringResource(id = R.string.toggle_rotation),
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    IconButton(
                        onClick = {
                            val orientation = context.resources.configuration.orientation
                            val mapped =
                                if (orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                                    android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                } else {
                                    android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                }
                            rotationLocked = !rotationLocked
                            if (rotationLocked) {
                                activity?.requestedOrientation = mapped
                                android.widget.Toast
                                    .makeText(
                                        context,
                                        context.getString(R.string.rotation_locked),
                                        android.widget.Toast.LENGTH_SHORT,
                                    ).show()
                            } else {
                                activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

                                android.widget.Toast
                                    .makeText(
                                        context,
                                        context.getString(R.string.rotation_unlocked),
                                        android.widget.Toast.LENGTH_SHORT,
                                    ).show()
                            }
                            controlsVisible = false
                        },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.rotation_lock),
                            contentDescription = stringResource(id = R.string.rotation_locked),
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = channel.name,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        if (channel.group.isNotEmpty()) {
                            Text(
                                text = channel.group,
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }

        if (httpErrorCode != null) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center,
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.85f)),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = stringResource(id = R.string.stream_unavailable_message),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(id = R.string.server_returned_http, httpErrorCode ?: 0),
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Button(onClick = {
                            restoreAndUnlock(orientationBeforeStream)
                            onBackClick()
                        }) {
                            Text(stringResource(id = R.string.back), color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
