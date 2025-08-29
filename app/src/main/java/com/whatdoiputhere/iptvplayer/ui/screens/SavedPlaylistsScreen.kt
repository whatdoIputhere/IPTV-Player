package com.whatdoiputhere.iptvplayer.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.whatdoiputhere.iptvplayer.R
import com.whatdoiputhere.iptvplayer.model.PlaylistConfig
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun savedPlaylistsScreen(
    playlists: List<PlaylistConfig>,
    activePlaylistId: String?,
    loadingPlaylistId: String?,
    onAddXtream: (String, String, String, String) -> Unit,
    onAddM3U: (String, String) -> Unit,
    onAddSamplePlaylist: () -> Unit,
    onValidateXtream: suspend (String, String, String) -> Boolean,
    onValidateM3U: suspend (String) -> Boolean,
    onSelectPlaylist: (String) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    val m3uNameRequester = remember { FocusRequester() }
    val m3uUrlRequester = remember { FocusRequester() }
    val xNameRequester = remember { FocusRequester() }
    val xHostRequester = remember { FocusRequester() }
    val xUserRequester = remember { FocusRequester() }
    val xPassRequester = remember { FocusRequester() }
    var showM3UDialog by remember { mutableStateOf(false) }
    var showXtreamDialog by remember { mutableStateOf(false) }
    var showAddMenu by remember { mutableStateOf(false) }

    var m3uName by remember { mutableStateOf("") }
    var m3uUrl by remember { mutableStateOf("") }

    var xtreamName by remember { mutableStateOf("") }
    var xtreamHost by remember { mutableStateOf("") }
    var xtreamUsername by remember { mutableStateOf("") }
    var xtreamPassword by remember { mutableStateOf("") }
    var xtreamHasError by remember { mutableStateOf(false) }
    var xtreamValidating by remember { mutableStateOf(false) }

    val defaultM3uName = stringResource(id = R.string.m3u_playlist)
    val defaultXtreamName = stringResource(id = R.string.xtream_playlist)
    var m3uHasError by remember { mutableStateOf(false) }
    var m3uValidating by remember { mutableStateOf(false) }

    val topbarConfig = LocalConfiguration.current
    val isLandscape = topbarConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
    val searchInputHeight = 58.dp
    val topBarHeight = if (isLandscape) searchInputHeight + 16.dp else 56.dp

    Scaffold(
        topBar = {
            Box(
                modifier =
                    Modifier
                        .height(topBarHeight)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background),
            ) {
                Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.fillMaxHeight().padding(start = 8.dp), contentAlignment = Alignment.CenterStart) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(id = R.string.back),
                                )
                            }
                            Text(
                                stringResource(id = R.string.saved_playlists),
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                    }

                    Box(modifier = Modifier.fillMaxHeight().padding(end = 8.dp), contentAlignment = Alignment.CenterEnd) {
                        if (!playlists.isEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                                IconButton(onClick = { showAddMenu = true }) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = stringResource(id = R.string.add_playlist),
                                    )
                                }
                                DropdownMenu(
                                    expanded = showAddMenu,
                                    onDismissRequest = { showAddMenu = false },
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(id = R.string.add_m3u_url)) },
                                        onClick = {
                                            showAddMenu = false
                                            showM3UDialog = true
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(stringResource(id = R.string.add_xtream_code))
                                        },
                                        onClick = {
                                            showAddMenu = false
                                            showXtreamDialog = true
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(stringResource(id = R.string.add_sample_playlist))
                                        },
                                        onClick = {
                                            showAddMenu = false
                                            showXtreamDialog = false
                                            onAddSamplePlaylist()
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (playlists.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(stringResource(id = R.string.no_playlists_saved))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { showXtreamDialog = true }) { Text(stringResource(id = R.string.add_xtream_code)) }
                    Button(onClick = { showM3UDialog = true }) { Text(stringResource(id = R.string.add_m3u_url)) }
                    Button(onClick = { onAddSamplePlaylist() }) { Text(stringResource(id = R.string.add_sample_playlist)) }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(playlists, key = { it.id }) { playlist ->
                        ListItem(
                            headlineContent = { Text(playlist.displayName) },
                            supportingContent = { Text(playlist.type) },
                            trailingContent = {
                                when (playlist.id) {
                                    loadingPlaylistId -> {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                        }
                                    }
                                    activePlaylistId -> {
                                        Text(
                                            stringResource(id = R.string.active),
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                    else -> {
                                        Button(onClick = { onSelectPlaylist(playlist.id) }) {
                                            Text(stringResource(id = R.string.set_active))
                                        }
                                    }
                                }
                            },
                            colors =
                                androidx.compose.material3.ListItemDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.background,
                                    headlineColor = MaterialTheme.colorScheme.onBackground,
                                    supportingColor = MaterialTheme.colorScheme.onBackground,
                                    leadingIconColor = MaterialTheme.colorScheme.onBackground,
                                    trailingIconColor = MaterialTheme.colorScheme.onBackground,
                                ),
                        )
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            TextButton(onClick = { onDeletePlaylist(playlist.id) }) {
                                Text(stringResource(id = R.string.delete))
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (showM3UDialog) {
        AlertDialog(
            onDismissRequest = {
                showM3UDialog = false
                m3uName = ""
                m3uUrl = ""
            },
            title = { Text(stringResource(id = R.string.add_m3u_url)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        modifier = Modifier.focusRequester(m3uNameRequester),
                        value = m3uName,
                        onValueChange = { m3uName = it },
                        label = { Text(stringResource(id = R.string.display_name)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { m3uUrlRequester.requestFocus() }),
                    )
                    OutlinedTextField(
                        modifier = Modifier.focusRequester(m3uUrlRequester),
                        value = m3uUrl,
                        onValueChange = { m3uUrl = it },
                        label = { Text(stringResource(id = R.string.m3u_url)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                        keyboardActions =
                            KeyboardActions(onDone = {
                            }),
                    )
                    if (m3uHasError) {
                        Text(
                            stringResource(id = R.string.invalid_m3u_url),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    if (m3uValidating) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Text(stringResource(id = R.string.validating_m3u_url))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !m3uValidating,
                    onClick = {
                        if (m3uUrl.isNotBlank() && !m3uValidating) {
                            m3uValidating = true
                            m3uHasError = false
                            scope.launch {
                                val ok = onValidateM3U(m3uUrl)
                                m3uValidating = false
                                if (ok) {
                                    onAddM3U(m3uName.ifBlank { defaultM3uName }, m3uUrl)
                                    m3uName = ""
                                    m3uUrl = ""
                                    showM3UDialog = false
                                } else {
                                    m3uHasError = true
                                }
                            }
                        }
                    },
                ) { Text(stringResource(id = R.string.save)) }
            },
            dismissButton = {
                TextButton(
                    enabled = !m3uValidating,
                    onClick = {
                        showM3UDialog = false
                        m3uName = ""
                        m3uUrl = ""
                        m3uHasError = false
                        m3uValidating = false
                    },
                ) { Text(stringResource(id = R.string.cancel)) }
            },
        )
    }

    if (showXtreamDialog) {
        AlertDialog(
            onDismissRequest = {
                showXtreamDialog = false
                xtreamName = ""
                xtreamHost = ""
                xtreamUsername = ""
                xtreamPassword = ""
            },
            title = { Text(stringResource(id = R.string.add_xtream_code)) },
            text = {
                androidx.compose.foundation.rememberScrollState().let { scrollState ->
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = 0.dp, max = 400.dp)
                                .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.focusRequester(xNameRequester),
                            value = xtreamName,
                            onValueChange = { xtreamName = it },
                            label = { Text(stringResource(id = R.string.display_name)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { xHostRequester.requestFocus() }),
                        )
                        OutlinedTextField(
                            modifier = Modifier.focusRequester(xHostRequester),
                            value = xtreamHost,
                            onValueChange = { xtreamHost = it },
                            label = { Text(stringResource(id = R.string.host_url)) },
                            placeholder = { Text(stringResource(id = R.string.example_host)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { xUserRequester.requestFocus() }),
                        )
                        OutlinedTextField(
                            modifier = Modifier.focusRequester(xUserRequester),
                            value = xtreamUsername,
                            onValueChange = { xtreamUsername = it },
                            label = { Text(stringResource(id = R.string.username)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { xPassRequester.requestFocus() }),
                        )
                        OutlinedTextField(
                            modifier = Modifier.focusRequester(xPassRequester),
                            value = xtreamPassword,
                            onValueChange = { xtreamPassword = it },
                            label = { Text(stringResource(id = R.string.password)) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions =
                                KeyboardActions(onDone = {
                                }),
                        )
                        if (xtreamHasError) {
                            Text(
                                stringResource(id = R.string.invalid_xtream),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        if (xtreamValidating) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Text(stringResource(id = R.string.testing_connection))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !xtreamValidating,
                    onClick = {
                        val inputsValid =
                            xtreamHost.isNotBlank() &&
                                xtreamUsername.isNotBlank() &&
                                xtreamPassword.isNotBlank() &&
                                !xtreamValidating
                        if (inputsValid) {
                            xtreamValidating = true
                            xtreamHasError = false
                            scope.launch {
                                val ok = onValidateXtream(xtreamHost, xtreamUsername, xtreamPassword)
                                xtreamValidating = false
                                if (ok) {
                                    onAddXtream(
                                        xtreamName.ifBlank { defaultXtreamName },
                                        xtreamHost,
                                        xtreamUsername,
                                        xtreamPassword,
                                    )
                                    xtreamName = ""
                                    xtreamHost = ""
                                    xtreamUsername = ""
                                    xtreamPassword = ""
                                    showXtreamDialog = false
                                } else {
                                    xtreamHasError = true
                                }
                            }
                        }
                    },
                ) { Text(stringResource(id = R.string.save)) }
            },
            dismissButton = {
                TextButton(
                    enabled = !xtreamValidating,
                    onClick = {
                        showXtreamDialog = false
                        xtreamName = ""
                        xtreamHost = ""
                        xtreamUsername = ""
                        xtreamPassword = ""
                        xtreamHasError = false
                        xtreamValidating = false
                    },
                ) { Text(stringResource(id = R.string.cancel)) }
            },
        )
    }
}
