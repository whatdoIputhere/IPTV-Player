package com.example.ptv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ptv.ui.screens.ChannelListScreen
import com.example.ptv.ui.screens.VideoPlayerScreen
import com.example.ptv.ui.theme.PtvTheme
import com.example.ptv.viewmodel.IPTVViewModel
import com.example.ptv.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PtvTheme {
                IPTVApp()
            }
        }
    }
}

@Composable
fun IPTVApp() {
    val viewModel: IPTVViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    
   
    BackHandler(enabled = uiState.currentScreen != Screen.ChannelList || uiState.selectedCategory != null) {
        when {
            uiState.currentScreen == Screen.VideoPlayer -> viewModel.clearSelectedChannel()
            uiState.currentScreen == Screen.SavedPlaylists -> viewModel.navigateToChannelList()
            uiState.selectedCategory != null -> viewModel.clearSelectedCategory()
            else -> { /* Let system handle - exit app */ }
        }
    }
    
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
           
            if (uiState.error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${uiState.error}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                when (uiState.currentScreen) {
                    Screen.VideoPlayer -> {
                        uiState.selectedChannel?.let { channel ->
                            VideoPlayerScreen(
                                channel = channel,
                                onBackClick = { viewModel.clearSelectedChannel() }
                            )
                        }
                    }
                    Screen.SavedPlaylists -> {
                        com.example.ptv.ui.screens.SavedPlaylistsScreen(
                            playlists = uiState.savedPlaylists,
                            activePlaylistId = uiState.activePlaylistId,
                            onAddXtream = { name, host, username, password -> 
                                viewModel.addXtreamFromDialog(name, host, username, password) 
                            },
                            onAddM3U = { name, url -> viewModel.addM3UPlaylist(name, url) },
                            onSelectPlaylist = { id -> viewModel.setActivePlaylist(id) },
                            onDeletePlaylist = { id -> viewModel.removePlaylist(id) },
                            onBack = { viewModel.navigateToChannelList() }
                        )
                    }
                    Screen.ChannelList -> {
                        ChannelListScreen(
                            uiState = uiState,
                            onChannelClick = { channel -> viewModel.selectChannel(channel) },
                            onSearchQueryChange = { query -> viewModel.updateSearchQuery(query) },
                            onCategorySelect = { category -> viewModel.selectGroup(category) },
                            onShowSavedPlaylists = { viewModel.navigateToSavedPlaylists() },
                            onRefreshPlaylist = { viewModel.refreshCurrentPlaylist() },
                            onBackToCategories = { viewModel.clearSelectedCategory() }
                        )
                    }
                }
            }
        }
    }
}