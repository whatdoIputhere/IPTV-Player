package com.example.ptv.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ptv.model.PlaylistConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPlaylistsScreen(
    playlists: List<PlaylistConfig>,
    activePlaylistId: String?,
    onAddXtream: (String, String, String, String) -> Unit,
    onAddM3U: (String, String) -> Unit,
    onSelectPlaylist: (String) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onBack: () -> Unit
) {
    var showM3UDialog by remember { mutableStateOf(false) }
    var showXtreamDialog by remember { mutableStateOf(false) }
    var showAddMenu by remember { mutableStateOf(false) }
    
    var m3uName by remember { mutableStateOf("") }
    var m3uUrl by remember { mutableStateOf("") }
    
    var xtreamName by remember { mutableStateOf("") }
    var xtreamHost by remember { mutableStateOf("") }
    var xtreamUsername by remember { mutableStateOf("") }
    var xtreamPassword by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(48.dp),
                title = { Text("Saved Playlists", style = MaterialTheme.typography.titleSmall) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddMenu = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Playlist")
                    }
                    DropdownMenu(
                        expanded = showAddMenu, 
                        onDismissRequest = { showAddMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add M3U URL") },
                            onClick = {
                                showAddMenu = false
                                showM3UDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Add Xtream Codes") },
                            onClick = {
                                showAddMenu = false
                                showXtreamDialog = true
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (playlists.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(), 
                    contentAlignment = Alignment.Center
                ) {
                    Text("No playlists saved.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(playlists) { playlist ->
                        ListItem(
                            headlineContent = { Text(playlist.displayName) },
                            supportingContent = { Text(playlist.type) },
                            trailingContent = {
                                if (playlist.id == activePlaylistId) {
                                    Text("Active", color = MaterialTheme.colorScheme.primary)
                                } else {
                                    Button(onClick = { onSelectPlaylist(playlist.id) }) {
                                        Text("Set Active")
                                    }
                                }
                            }
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = { onSelectPlaylist(playlist.id) }) {
                                Text("Load now")
                            }
                            TextButton(onClick = { onDeletePlaylist(playlist.id) }) {
                                Text("Delete")
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
            title = { Text("Add M3U Playlist") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = m3uName, 
                        onValueChange = { m3uName = it }, 
                        label = { Text("Display name") }
                    )
                    OutlinedTextField(
                        value = m3uUrl, 
                        onValueChange = { m3uUrl = it }, 
                        label = { Text("M3U URL") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (m3uUrl.isNotBlank()) {
                            onAddM3U(m3uName.ifBlank { "M3U Playlist" }, m3uUrl)
                            m3uName = ""
                            m3uUrl = ""
                            showM3UDialog = false
                        }
                    }
                ) { 
                    Text("Save") 
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showM3UDialog = false
                        m3uName = ""
                        m3uUrl = ""
                    }
                ) { 
                    Text("Cancel") 
                }
            }
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
            title = { Text("Add Xtream Codes") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = xtreamName,
                        onValueChange = { xtreamName = it },
                        label = { Text("Display name") }
                    )
                    OutlinedTextField(
                        value = xtreamHost,
                        onValueChange = { xtreamHost = it },
                        label = { Text("Host URL") },
                        placeholder = { Text("http://example.com:8080") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                    )
                    OutlinedTextField(
                        value = xtreamUsername,
                        onValueChange = { xtreamUsername = it },
                        label = { Text("Username") }
                    )
                    OutlinedTextField(
                        value = xtreamPassword,
                        onValueChange = { xtreamPassword = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (xtreamHost.isNotBlank() && xtreamUsername.isNotBlank() && xtreamPassword.isNotBlank()) {
                            onAddXtream(
                                xtreamName.ifBlank { "Xtream Playlist" }, 
                                xtreamHost, 
                                xtreamUsername, 
                                xtreamPassword
                            )
                            xtreamName = ""
                            xtreamHost = ""
                            xtreamUsername = ""
                            xtreamPassword = ""
                            showXtreamDialog = false
                        }
                    }
                ) { 
                    Text("Save") 
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showXtreamDialog = false
                        xtreamName = ""
                        xtreamHost = ""
                        xtreamUsername = ""
                        xtreamPassword = ""
                    }
                ) { 
                    Text("Cancel") 
                }
            }
        )
    }
}
