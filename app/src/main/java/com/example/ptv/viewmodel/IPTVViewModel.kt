package com.example.ptv.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ptv.model.Channel
import com.example.ptv.model.XtreamConfig
import com.example.ptv.model.PlaylistConfig
import com.example.ptv.repository.SimpleIPTVRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.google.gson.Gson

data class IPTVUiState(
    val channels: List<Channel> = emptyList(),
    val filteredChannels: List<Channel> = emptyList(),
    val selectedChannel: Channel? = null,
    val isLoading: Boolean = false,
    val loadingStatus: String = "",
    val loadingProgress: Float = 0f,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedGroup: String = "All",
    val selectedCategory: String? = null,
    val currentScreen: Screen = Screen.ChannelList,
    val xtreamConfigs: List<XtreamConfig> = emptyList(),
    val activeXtreamConfigId: Long? = null,
    val isTestingConnection: Boolean = false,
    val testResult: String? = null
    ,
    val savedPlaylists: List<PlaylistConfig> = emptyList(),
    val activePlaylistId: String? = null
)

enum class Screen {
    ChannelList,
    VideoPlayer,
    SavedPlaylists
}

class IPTVViewModel(application: Application) : AndroidViewModel(application) {
   
    private fun loadSavedPlaylists() {
        val playlists = repository.getAllSavedPlaylists()
        val activeId = repository.getActivePlaylistId()
        _uiState.value = _uiState.value.copy(
            savedPlaylists = playlists,
            activePlaylistId = activeId
        )
    }

    fun navigateToSavedPlaylists() {
        loadSavedPlaylists()
        _uiState.value = _uiState.value.copy(currentScreen = Screen.SavedPlaylists)
    }

    fun addPlaylist(playlist: PlaylistConfig) {
        repository.savePlaylistConfig(playlist)
        loadSavedPlaylists()
    }

    fun addM3UPlaylist(name: String, url: String) {
       
        val id = url.trim()
        val cfg = PlaylistConfig(
            id = id,
            displayName = name.ifBlank { "M3U" },
            type = "M3U",
            data = url.trim()
        )
        addPlaylist(cfg)
        setActivePlaylist(id)
        loadChannelsFromUrl(url)
    }

    fun addXtreamFromDialog(name: String, host: String, username: String, password: String) {
        val config = XtreamConfig(
            id = System.currentTimeMillis(),
            name = name.ifBlank { "Xtream Playlist" },
            host = host.trim(),
            username = username.trim(),
            password = password.trim()
        )
        
       
        val id = "xtream:${host.trim()}:${username.trim()}"
        val playlistConfig = PlaylistConfig(
            id = id,
            displayName = name.ifBlank { config.name },
            type = "Xtream",
            data = gson.toJson(config)
        )
        
        addPlaylist(playlistConfig)
        setActivePlaylist(id)
        
       
        repository.saveXtreamConfig(config)
        repository.setActiveXtreamConfig(config.id)
        
       
        loadChannelsFromXtream(config)
    }

    fun setActivePlaylist(id: String) {
        repository.setActivePlaylist(id)
        val playlists = repository.getAllSavedPlaylists()
        val selected = playlists.find { it.id == id }
        loadSavedPlaylists()
        selected?.let { pl ->
            when (pl.type) {
                "M3U" -> loadChannelsFromUrl(pl.data)
                "Xtream" -> runCatching { gson.fromJson(pl.data, XtreamConfig::class.java) }.getOrNull()?.let { cfg ->
                    loadChannelsFromXtream(cfg)
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        error = "Unsupported playlist type: ${pl.type}"
                    )
                }
            }
        }
    }

    fun removePlaylist(id: String) {
        repository.removePlaylist(id)
        loadSavedPlaylists()
    }
    private val repository = SimpleIPTVRepository(application)
    
    private val _uiState = MutableStateFlow(IPTVUiState())
    val uiState: StateFlow<IPTVUiState> = _uiState.asStateFlow()
    
    private val gson = Gson()
    
    init {
       
        viewModelScope.launch {
            try {
                loadXtreamConfigs()
                loadSavedPlaylists()
            } catch (e: Exception) {
               
                _uiState.value = _uiState.value.copy(
                    error = "Database initialization failed: ${e.message}"
                )
            }
        }
    }
    
    private fun loadXtreamConfigs() {
        viewModelScope.launch {
            try {
                combine(
                    repository.getAllXtreamConfigs(),
                    repository.getActiveXtreamConfig()
                ) { configs, activeConfig ->
                    _uiState.value = _uiState.value.copy(
                        xtreamConfigs = configs,
                        activeXtreamConfigId = activeConfig?.id
                    )
                    
                   
                    if (activeConfig != null && _uiState.value.channels.isEmpty()) {
                        loadChannelsFromXtream(activeConfig)
                    }
                }.collect { /* Collection handled in combine */ }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load Xtream configurations: ${e.message}"
                )
            }
        }
    }
    
    fun loadChannelsFromUrl(url: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                error = null,
                loadingStatus = "Connecting to server...",
                loadingProgress = 0.2f
            )
            try {
                _uiState.value = _uiState.value.copy(
                    loadingStatus = "Downloading playlist...",
                    loadingProgress = 0.5f
                )
                
                val channels = repository.loadChannelsFromUrl(url)
                
                _uiState.value = _uiState.value.copy(
                    loadingStatus = "Processing channels...",
                    loadingProgress = 0.8f
                )
                
                _uiState.value = _uiState.value.copy(
                    channels = channels,
                    filteredChannels = channels,
                    isLoading = false,
                    loadingStatus = "",
                    loadingProgress = 1.0f
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    loadingStatus = "",
                    loadingProgress = 0f,
                    error = "Failed to load channels: ${e.message}"
                )
            }
        }
    }
    
    fun loadChannelsFromXtream(config: XtreamConfig, forceRefresh: Boolean = false) {
        viewModelScope.launch {
           
            if (!forceRefresh) {
                _uiState.value = _uiState.value.copy(
                    isLoading = true, 
                    error = null,
                    loadingStatus = "Loading...",
                    loadingProgress = 0.5f
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = true, 
                    error = null,
                    loadingStatus = "Refreshing ${config.name}...",
                    loadingProgress = 0.1f
                )
            }
            
            try {
                val channels = repository.loadChannelsFromXtream(config, forceRefresh)
                
               
                _uiState.value = _uiState.value.copy(
                    channels = channels,
                    filteredChannels = channels,
                    isLoading = false,
                    loadingStatus = "",
                    loadingProgress = 1.0f,
                    currentScreen = Screen.ChannelList
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    loadingStatus = "",
                    loadingProgress = 0f,
                    error = "Failed to load channels from Xtream: ${e.message}"
                )
            }
        }
    }
    
    
    fun selectChannel(channel: Channel) {
        _uiState.value = _uiState.value.copy(
            selectedChannel = channel,
            currentScreen = Screen.VideoPlayer
        )
    }
    
    fun clearSelectedChannel() {
        _uiState.value = _uiState.value.copy(
            selectedChannel = null,
            currentScreen = Screen.ChannelList
        )
    }
    
    fun navigateToChannelList() {
        _uiState.value = _uiState.value.copy(currentScreen = Screen.ChannelList)
        
       
        viewModelScope.launch {
            val activeConfig = _uiState.value.xtreamConfigs.find { it.id == _uiState.value.activeXtreamConfigId }
            if (activeConfig != null && _uiState.value.channels.all { it.group.startsWith("Demo") }) {
               
                loadChannelsFromXtream(activeConfig)
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterChannels()
    }
    
    fun selectGroup(group: String) {
        _uiState.value = _uiState.value.copy(
            selectedGroup = group,
            selectedCategory = group
        )
        filterChannels()
    }
    
    fun clearSelectedCategory() {
        _uiState.value = _uiState.value.copy(
            selectedCategory = null,
            searchQuery = ""
        )
    }
    
    private fun filterChannels() {
        val currentState = _uiState.value
        val filtered = currentState.channels.filter { channel ->
            val matchesSearch = if (currentState.searchQuery.isBlank()) {
                true
            } else {
                channel.name.contains(currentState.searchQuery, ignoreCase = true)
            }
            
            val matchesGroup = if (currentState.selectedGroup == "All") {
                true
            } else {
                channel.group == currentState.selectedGroup
            }
            
            matchesSearch && matchesGroup
        }
        
        _uiState.value = currentState.copy(filteredChannels = filtered)
    }
    
   
    fun addXtreamConfig(config: XtreamConfig) {
        viewModelScope.launch {
            try {
                val configId = repository.saveXtreamConfig(config)
                if (_uiState.value.xtreamConfigs.isEmpty()) {
                   
                    repository.setActiveXtreamConfig(configId)
                   
                    loadChannelsFromXtream(config.copy(id = configId))
                } else {
                   
                    repository.setActiveXtreamConfig(configId)
                    loadChannelsFromXtream(config.copy(id = configId))
                }
                val playlistId = "xtream:${config.host}:${config.username}"
                val displayName = if (config.name.isNotBlank()) config.name else "${config.username}@${config.host}"
                val playlist = PlaylistConfig(
                    id = playlistId,
                    displayName = displayName,
                    type = "Xtream",
                    data = gson.toJson(config.copy(id = configId))
                )
                repository.savePlaylistConfig(playlist)
                repository.setActivePlaylist(playlistId)
                loadSavedPlaylists()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save configuration: ${e.message}"
                )
            }
        }
    }
    
    fun deleteXtreamConfig(config: XtreamConfig) {
        viewModelScope.launch {
            try {
                repository.deleteXtreamConfig(config)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete configuration: ${e.message}"
                )
            }
        }
    }
    
    fun setActiveXtreamConfig(configId: Long) {
        viewModelScope.launch {
            try {
                repository.setActiveXtreamConfig(configId)
               
                val config = _uiState.value.xtreamConfigs.find { it.id == configId }
                config?.let { loadChannelsFromXtream(it) }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to set active configuration: ${e.message}"
                )
            }
        }
    }
    
    fun testXtreamConfig(config: XtreamConfig) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isTestingConnection = true,
                testResult = null
            )
            
            try {
                val result = repository.testXtreamConnection(config.host, config.username, config.password)
                val testMessage = if (result.isSuccess && result.getOrNull() == true) {
                    "✓ Connection successful!"
                } else {
                    "✗ Connection failed: ${result.exceptionOrNull()?.message ?: "Unknown error"}"
                }
                
                _uiState.value = _uiState.value.copy(
                    isTestingConnection = false,
                    testResult = testMessage
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isTestingConnection = false,
                    testResult = "✗ Connection failed: ${e.message}"
                )
            }
        }
    }
    
    fun refreshCurrentPlaylist() {
       
        val activePlaylistId = _uiState.value.activePlaylistId
        val activePl = _uiState.value.savedPlaylists.find { it.id == activePlaylistId }
        if (activePl != null) {
            when (activePl.type) {
                "M3U" -> loadChannelsFromUrl(activePl.data)
                "Xtream" -> runCatching { com.google.gson.Gson().fromJson(activePl.data, XtreamConfig::class.java) }
                    .getOrNull()?.let { cfg -> loadChannelsFromXtream(cfg, forceRefresh = true) }
            }
            return
        }

       
        val activeConfig = _uiState.value.xtreamConfigs.find { it.id == _uiState.value.activeXtreamConfigId }
        if (activeConfig != null) {
            loadChannelsFromXtream(activeConfig, forceRefresh = true)
        }
    }
    
   
}
