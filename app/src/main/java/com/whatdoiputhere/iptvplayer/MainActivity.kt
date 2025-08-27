package com.whatdoiputhere.iptvplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import com.whatdoiputhere.iptvplayer.ui.screens.channelListScreen
import com.whatdoiputhere.iptvplayer.ui.screens.videoPlayerScreen
import com.whatdoiputhere.iptvplayer.ui.theme.iptvPlayerTheme
import com.whatdoiputhere.iptvplayer.viewmodel.IPTVViewModel
import com.whatdoiputhere.iptvplayer.viewmodel.Screen

class MainActivity : ComponentActivity() {
    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            iptvPlayerTheme {
                iptvApp()
            }
        }
    }
}

@UnstableApi
@Composable
fun iptvApp() {
    val viewModel: IPTVViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    var showExitDialog by remember { mutableStateOf(false) }
    val activity = LocalActivity.current

    BackHandler(enabled = true) {
        when {
            uiState.currentScreen == Screen.VideoPlayer -> viewModel.clearSelectedChannel()
            uiState.currentScreen == Screen.SavedPlaylists -> viewModel.navigateToChannelList()
            uiState.selectedCategory != null -> viewModel.clearSelectedCategory()
            else -> showExitDialog = true
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            if (uiState.error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(id = R.string.error_with_message, uiState.error ?: ""),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            } else {
                when (uiState.currentScreen) {
                    Screen.VideoPlayer -> {
                        uiState.selectedChannel?.let { channel ->
                            videoPlayerScreen(
                                channel = channel,
                                onBackClick = { viewModel.clearSelectedChannel() },
                                orientationBeforeStream = uiState.orientationBeforeStream,
                            )
                        }
                    }
                    Screen.SavedPlaylists -> {
                        com.whatdoiputhere.iptvplayer.ui.screens.savedPlaylistsScreen(
                            playlists = uiState.savedPlaylists,
                            activePlaylistId = uiState.activePlaylistId,
                            loadingPlaylistId = uiState.loadingPlaylistId,
                            onAddXtream = { name, host, username, password ->
                                viewModel.addXtreamFromDialog(name, host, username, password)
                            },
                            onAddM3U = { name, url -> viewModel.addM3UPlaylist(name, url) },
                            onValidateXtream = { host, username, password ->
                                viewModel.validateXtream(host, username, password)
                            },
                            onValidateM3U = { url -> viewModel.validateM3U(url) },
                            onSelectPlaylist = { id -> viewModel.setActivePlaylist(id) },
                            onDeletePlaylist = { id -> viewModel.removePlaylist(id) },
                            onBack = { viewModel.navigateToChannelList() },
                        )
                    }
                    Screen.ChannelList -> {
                        channelListScreen(
                            uiState = uiState,
                            onChannelClick = { channel -> viewModel.selectChannel(channel) },
                            onSearchQueryChange = { query -> viewModel.updateSearchQuery(query) },
                            onCategorySelect = { category -> viewModel.selectGroup(category) },
                            onShowSavedPlaylists = { viewModel.navigateToSavedPlaylists() },
                            onRefreshPlaylist = { viewModel.refreshCurrentPlaylist() },
                            onBackToCategories = { viewModel.clearSelectedCategory() },
                            onSaveScroll = { index, offset -> viewModel.saveChannelListScroll(index, offset) },
                        )
                    }
                }
            }
            if (showExitDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showExitDialog = false },
                    confirmButton = {
                        Button(
                            onClick = {
                                showExitDialog = false
                                activity?.finishAndRemoveTask()
                            },
                        ) {
                            Text(stringResource(id = R.string.exit))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showExitDialog = false }) {
                            Text(stringResource(id = R.string.cancel))
                        }
                    },
                    title = { Text(stringResource(id = R.string.exit_app_title)) },
                    text = { Text(stringResource(id = R.string.exit_app_message)) },
                )
            }
        }
    }
}
