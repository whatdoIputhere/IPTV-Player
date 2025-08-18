package com.example.ptv.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ptv.model.Channel
import com.example.ptv.ui.components.ChannelItem
import com.example.ptv.util.FunFacts
import com.example.ptv.viewmodel.IPTVUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelListScreen(
    uiState: IPTVUiState,
    onChannelClick: (Channel) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onShowSavedPlaylists: () -> Unit = {},
    onRefreshPlaylist: () -> Unit = {},
    onBackToCategories: () -> Unit = {}
) {
    var categorySearchQuery by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "IPTV Player",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (uiState.channels.isNotEmpty()) {
                        IconButton(
                            onClick = onRefreshPlaylist,
                            enabled = !uiState.isLoading
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh Playlist",
                                tint = if (uiState.isLoading)
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    TextButton(
                        onClick = onShowSavedPlaylists
                    ) {
                        Text("Playlists")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            if (uiState.channels.isNotEmpty()) {

                val categories = remember(uiState.channels) {
                    val seenCategories = mutableSetOf<String>()
                    uiState.channels.mapNotNull { channel ->
                        if (channel.group.isNotBlank() && seenCategories.add(channel.group)) {
                            channel.group
                        } else {
                            null
                        }
                    }
                }

                if (uiState.selectedCategory == null) {

                    OutlinedTextField(
                        value = categorySearchQuery,
                        onValueChange = { categorySearchQuery = it },
                        label = { Text("Search categories") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        singleLine = true
                    )


                    val filteredCategories by remember(categories, categorySearchQuery) {
                        derivedStateOf {
                            if (categorySearchQuery.isBlank()) {
                                categories
                            } else {
                                categories.filter { category ->
                                    category.contains(categorySearchQuery, ignoreCase = true)
                                }
                            }
                        }
                    }


                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredCategories, key = { it }) { category ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clickable {
                                        onCategorySelect(category)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize().padding(8.dp)
                                ) {
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }


                    if (filteredCategories.isEmpty() && categorySearchQuery.isNotBlank()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No categories match your search",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = onSearchQueryChange,
                            label = { Text("Search channels") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            categorySearchQuery = ""
                            onBackToCategories()
                        }) {
                            Text("Categories")
                        }
                    }
                }
            }


            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Error",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = uiState.error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }

                    uiState.channels.isEmpty() && !uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No channels loaded",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Text(
                                    text = "Load a playlist to get started",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                Button(
                                    onClick = onShowSavedPlaylists,
                                    modifier = Modifier.padding(top = 16.dp)
                                ) {
                                    Text("Playlists")
                                }
                            }
                        }
                    }

                    uiState.selectedCategory != null -> {
                        val filteredChannels by remember(
                            uiState.channels,
                            uiState.selectedCategory,
                            uiState.searchQuery
                        ) {
                            derivedStateOf {
                                uiState.channels.filter { channel ->
                                    channel.group == uiState.selectedCategory && (uiState.searchQuery.isBlank() || channel.name.contains(
                                        uiState.searchQuery,
                                        ignoreCase = true
                                    ))
                                }
                            }
                        }
                        if (filteredChannels.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No channels match your search in this category")
                            }
                        } else {
                            LazyColumn {
                                items(filteredChannels, key = { it.name }) { channel ->
                                    ChannelItem(
                                        channel = channel,
                                        onChannelClick = onChannelClick
                                    )
                                }
                            }
                        }
                    }
                }


                if (uiState.isLoading && (uiState.channels.isNotEmpty() || uiState.loadingStatus.isNotBlank())) {
                    LoadingOverlay(
                        status = uiState.loadingStatus,
                        progress = uiState.loadingProgress,
                        hasContent = uiState.channels.isNotEmpty()
                    )
                }


                if (uiState.isLoading && uiState.channels.isEmpty()) {
                    EnhancedLoadingScreen(
                        status = uiState.loadingStatus,
                        progress = uiState.loadingProgress
                    )
                }
            }
        }


    }

}

@Composable
fun EnhancedLoadingScreen(
    status: String,
    progress: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    
   
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )
    
   
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )
    
   
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = alpha)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
           
            Card(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale),
                shape = RoundedCornerShape(60.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Loading",
                        modifier = Modifier
                            .size(48.dp)
                            .rotate(rotation),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
           
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
               
                if (progress > 0f) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                                        .width(200.dp)
                                                        .height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer,
                            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                } else {
                   
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
               
                if (status.isNotBlank()) {
                    Text(
                        text = status,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.alpha(0.8f)
                    )
                } else {
                    Text(
                        text = "Loading IPTV channels...",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.alpha(0.8f)
                    )
                }
                
               
                val currentTip = remember { FunFacts.randomFact() }

                Text(
                    text = currentTip,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .alpha(0.7f)
                )
            }
        }
    }
}

@Composable
fun LoadingOverlay(
    status: String,
    progress: Float,
    hasContent: Boolean
) {
    if (hasContent) {
       
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (progress > 0f) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(24.dp),
                        color = ProgressIndicatorDefaults.circularColor,
                        strokeWidth = 3.dp,
                        trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                        strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp
                    )
                }

                Text(
                    text = status.ifBlank { "Loading from cache..." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
