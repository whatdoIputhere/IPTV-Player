package com.whatdoiputhere.iptvplayer.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatdoiputhere.iptvplayer.R
import com.whatdoiputhere.iptvplayer.model.Channel
import com.whatdoiputhere.iptvplayer.ui.components.channelItem
import com.whatdoiputhere.iptvplayer.util.FunFacts
import com.whatdoiputhere.iptvplayer.viewmodel.IPTVUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun channelListScreen(
    uiState: IPTVUiState,
    onChannelClick: (Channel) -> Unit,
    onSaveScroll: (firstVisibleIndex: Int, firstVisibleScrollOffset: Int) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onShowSavedPlaylists: () -> Unit = {},
    onRefreshPlaylist: () -> Unit = {},
    onBackToCategories: () -> Unit = {},
) {
    var categorySearchQuery by rememberSaveable { mutableStateOf("") }

    val lastAutoRefreshError = remember { mutableStateOf<String?>(null) }
    LaunchedEffect(uiState.error) {
        val err = uiState.error
        if (!err.isNullOrBlank() && err.contains("458") && lastAutoRefreshError.value != err) {
            lastAutoRefreshError.value = err
            onRefreshPlaylist()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(56.dp),
                title = {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.titleSmall,
                        modifier =
                            Modifier
                                .fillMaxHeight()
                                .wrapContentHeight(Alignment.CenterVertically),
                    )
                },
                navigationIcon = {
                    if (uiState.selectedCategory != null) {
                        IconButton(
                            onClick = {
                                categorySearchQuery = ""
                                onBackToCategories()
                            },
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(id = R.string.back),
                            )
                        }
                    }
                },
                actions = {
                    if (uiState.channels.isNotEmpty()) {
                        IconButton(
                            onClick = onRefreshPlaylist,
                            enabled = !uiState.isLoading,
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription =
                                    stringResource(id = R.string.refresh_playlist),
                                tint =
                                    if (uiState.isLoading) {
                                        MaterialTheme.colorScheme.onSurface
                                            .copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                            )
                        }
                        TextButton(
                            onClick = onShowSavedPlaylists,
                            modifier = Modifier.height(36.dp).padding(end = 4.dp),
                            contentPadding =
                                PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                stringResource(id = R.string.playlists),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (uiState.channels.isNotEmpty()) {
                val categories =
                    remember(uiState.channels) {
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
                        label = {
                            Text(
                                stringResource(id = R.string.search_categories),
                                style =
                                    MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                    ),
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = stringResource(id = R.string.search),
                            )
                        },
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp).height(58.dp),
                        singleLine = true,
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val filteredCategories by
                        remember(categories, categorySearchQuery) {
                            derivedStateOf {
                                if (categorySearchQuery.isBlank()) {
                                    categories
                                } else {
                                    categories.filter { category ->
                                        category.contains(
                                            categorySearchQuery,
                                            ignoreCase = true,
                                        )
                                    }
                                }
                            }
                        }

                    val gridState =
                        rememberLazyGridState(
                            initialFirstVisibleItemIndex =
                                uiState.channelListFirstVisibleIndex ?: 0,
                            initialFirstVisibleItemScrollOffset =
                                uiState.channelListFirstVisibleScrollOffset ?: 0,
                        )

                    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                        LazyVerticalGrid(
                            state = gridState,
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(filteredCategories, key = { it }, contentType = { "category" }) { category ->
                                Card(
                                    modifier =
                                        Modifier.fillMaxWidth().height(84.dp).clickable {
                                            onSaveScroll(
                                                gridState.firstVisibleItemIndex,
                                                gridState.firstVisibleItemScrollOffset,
                                            )
                                            onCategorySelect(category)
                                        },
                                    colors =
                                        CardDefaults.cardColors(
                                            containerColor =
                                                MaterialTheme.colorScheme
                                                    .primaryContainer,
                                        ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize().padding(6.dp),
                                    ) {
                                        Text(
                                            text = category,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (filteredCategories.isEmpty() && categorySearchQuery.isNotBlank()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(id = R.string.no_categories_match),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            )
                        }
                    }
                } else {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = onSearchQueryChange,
                            label = {
                                Text(
                                    stringResource(id = R.string.search_channels),
                                    style =
                                        MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                        ),
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription =
                                        stringResource(id = R.string.search),
                                )
                            },
                            textStyle =
                                MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            modifier = Modifier.fillMaxWidth().height(58.dp),
                            singleLine = true,
                            maxLines = 1,
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(id = R.string.error),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                                Text(
                                    text = uiState.error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            }
                        }
                    }
                    uiState.channels.isEmpty() && !uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(id = R.string.no_channels_loaded),
                                    style = MaterialTheme.typography.headlineSmall,
                                )
                                Text(
                                    text = stringResource(id = R.string.add_and_load_playlist),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                                Button(
                                    onClick = onShowSavedPlaylists,
                                    modifier = Modifier.padding(top = 16.dp),
                                ) { Text(stringResource(id = R.string.playlists)) }
                            }
                        }
                    }
                    uiState.selectedCategory != null -> {
                        val filteredChannels = uiState.filteredChannels
                        if (filteredChannels.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) { Text(stringResource(id = R.string.no_channels_match_in_category)) }
                        } else {
                            val configuration = LocalConfiguration.current
                            if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                val gridState =
                                    rememberLazyGridState(
                                        initialFirstVisibleItemIndex =
                                            uiState.channelListFirstVisibleIndex ?: 0,
                                        initialFirstVisibleItemScrollOffset =
                                            uiState.channelListFirstVisibleScrollOffset
                                                ?: 0,
                                    )

                                CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                                    LazyVerticalGrid(
                                        state = gridState,
                                        columns = GridCells.Fixed(3),
                                        modifier =
                                            Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        content = {
                                            items(
                                                filteredChannels,
                                                key = { it.url },
                                                contentType = { "channel" },
                                            ) { channel ->
                                                channelItem(
                                                    channel = channel,
                                                    onChannelClick = {
                                                        onSaveScroll(
                                                            gridState.firstVisibleItemIndex,
                                                            gridState
                                                                .firstVisibleItemScrollOffset,
                                                        )
                                                        onChannelClick(channel)
                                                    },
                                                    modifier = Modifier.fillMaxWidth().height(88.dp),
                                                    compact = true,
                                                )
                                            }
                                        },
                                    )
                                }
                            } else {
                                val listState =
                                    rememberLazyListState(
                                        initialFirstVisibleItemIndex =
                                            uiState.channelListFirstVisibleIndex ?: 0,
                                        initialFirstVisibleItemScrollOffset =
                                            uiState.channelListFirstVisibleScrollOffset
                                                ?: 0,
                                    )

                                CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                                    LazyColumn(state = listState) {
                                        items(
                                            filteredChannels,
                                            key = { it.url },
                                            contentType = { "channel" },
                                        ) { channel ->
                                            channelItem(
                                                channel = channel,
                                                onChannelClick = {
                                                    onSaveScroll(
                                                        listState.firstVisibleItemIndex,
                                                        listState.firstVisibleItemScrollOffset,
                                                    )
                                                    onChannelClick(channel)
                                                },
                                                modifier = Modifier.fillMaxWidth().height(88.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (uiState.isLoading &&
                    (
                        uiState.channels.isNotEmpty() ||
                            uiState.loadingStatus.isNotBlank()
                    )
                ) {
                    loadingOverlay(
                        status = uiState.loadingStatus,
                        progress = uiState.loadingProgress,
                        hasContent = uiState.channels.isNotEmpty(),
                    )
                }

                if (uiState.isLoading && uiState.channels.isEmpty()) {
                    enhancedLoadingScreen(
                        status = uiState.loadingStatus,
                        progress = uiState.loadingProgress,
                    )
                }
            }
        }
    }
}

@Composable
fun enhancedLoadingScreen(
    status: String,
    progress: Float,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")

    val rotation by
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "rotation",
        )

    val alpha by
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.7f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "pulse",
        )

    val scale by
        infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "scale",
        )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = alpha)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Card(
                modifier = Modifier.size(120.dp).scale(scale),
                shape = RoundedCornerShape(60.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription =
                            stringResource(id = com.whatdoiputhere.iptvplayer.R.string.loading),
                        modifier = Modifier.size(48.dp).rotate(rotation),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (progress > 0f) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.width(200.dp).height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer,
                            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                if (status.isNotBlank()) {
                    Text(
                        text = status,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.alpha(0.8f),
                    )
                } else {
                    Text(
                        text =
                            stringResource(
                                id = com.whatdoiputhere.iptvplayer.R.string.loading_iptv_channels,
                            ),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.alpha(0.8f),
                    )
                }

                val currentTip = remember { FunFacts.randomFact() }

                Text(
                    text = currentTip,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp).alpha(0.7f),
                )
            }
        }
    }
}

@Composable
fun loadingOverlay(
    status: String,
    progress: Float,
    hasContent: Boolean,
) {
    if (hasContent) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                    .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                }

                Text(
                    text =
                        status.ifBlank {
                            stringResource(id = com.whatdoiputhere.iptvplayer.R.string.loading_from_cache)
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
