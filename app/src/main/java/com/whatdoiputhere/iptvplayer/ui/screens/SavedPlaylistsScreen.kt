package com.whatdoiputhere.iptvplayer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
    onAddXtream: (String, String, String, String) -> Unit,
    onAddM3U: (String, String) -> Unit,
    onValidateXtream: suspend (String, String, String) -> Boolean,
    onValidateM3U: suspend (String) -> Boolean,
    onSelectPlaylist: (String) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
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

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(48.dp),
                title = {
                    Text(
                        stringResource(id = R.string.saved_playlists),
                        style = MaterialTheme.typography.titleSmall,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back),
                        )
                    }
                },
                actions = {
                    if (!playlists.isEmpty()) {
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
                        }
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (playlists.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(stringResource(id = R.string.no_playlists_saved))
                        Button(onClick = { showXtreamDialog = true }) {
                            Text(stringResource(id = R.string.add_xtream_code))
                        }
                        Button(onClick = { showM3UDialog = true }) {
                            Text(stringResource(id = R.string.add_m3u_url))
                        }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(playlists, key = { it.id }) { playlist ->
                        ListItem(
                            headlineContent = { Text(playlist.displayName) },
                            supportingContent = { Text(playlist.type) },
                            trailingContent = {
                                if (playlist.id == activePlaylistId) {
                                    Text(
                                        stringResource(id = R.string.active),
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                } else {
                                    Button(onClick = { onSelectPlaylist(playlist.id) }) {
                                        Text(stringResource(id = R.string.set_active))
                                    }
                                }
                            },
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
                        value = m3uName,
                        onValueChange = { m3uName = it },
                        label = { Text(stringResource(id = R.string.display_name)) },
                    )
                    OutlinedTextField(
                        value = m3uUrl,
                        onValueChange = { m3uUrl = it },
                        label = { Text(stringResource(id = R.string.m3u_url)) },
                    )
                    if (m3uHasError) {
                        Text(
                            stringResource(id = R.string.invalid_m3u_url),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
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
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = xtreamName,
                        onValueChange = { xtreamName = it },
                        label = { Text(stringResource(id = R.string.display_name)) },
                    )
                    OutlinedTextField(
                        value = xtreamHost,
                        onValueChange = { xtreamHost = it },
                        label = { Text(stringResource(id = R.string.host_url)) },
                        placeholder = { Text(stringResource(id = R.string.example_host)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    )
                    OutlinedTextField(
                        value = xtreamUsername,
                        onValueChange = { xtreamUsername = it },
                        label = { Text(stringResource(id = R.string.username)) },
                    )
                    OutlinedTextField(
                        value = xtreamPassword,
                        onValueChange = { xtreamPassword = it },
                        label = { Text(stringResource(id = R.string.password)) },
                        visualTransformation = PasswordVisualTransformation(),
                    )
                    if (xtreamHasError) {
                        Text(
                            stringResource(id = R.string.invalid_xtream),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
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
