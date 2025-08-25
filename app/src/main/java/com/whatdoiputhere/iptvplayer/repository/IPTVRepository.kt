package com.whatdoiputhere.iptvplayer.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.whatdoiputhere.iptvplayer.api.XtreamApiService
import com.whatdoiputhere.iptvplayer.model.Channel
import com.whatdoiputhere.iptvplayer.model.XtreamChannel
import com.whatdoiputhere.iptvplayer.model.XtreamConfig
import com.whatdoiputhere.iptvplayer.parser.M3UParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class IPTVRepository(
    private val context: Context,
) {
    private val playlistPrefs: SharedPreferences =
        context.getSharedPreferences("saved_playlists", Context.MODE_PRIVATE)
    private val activePlaylistKey = "active_playlist_id"

    fun savePlaylistConfig(playlist: com.whatdoiputhere.iptvplayer.model.PlaylistConfig) {
        val playlists = getAllSavedPlaylists().toMutableList()
        val existingIndex = playlists.indexOfFirst { it.id == playlist.id }
        if (existingIndex >= 0) {
            playlists[existingIndex] = playlist
        } else {
            playlists.add(playlist)
        }
        val json = gson.toJson(playlists)
        playlistPrefs.edit().putString("playlists", json).apply()
    }

    fun getAllSavedPlaylists(): List<com.whatdoiputhere.iptvplayer.model.PlaylistConfig> {
        val json = playlistPrefs.getString("playlists", null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<com.whatdoiputhere.iptvplayer.model.PlaylistConfig>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun setActivePlaylist(id: String) {
        playlistPrefs.edit().putString(activePlaylistKey, id).apply()
    }

    fun getActivePlaylistId(): String? = playlistPrefs.getString(activePlaylistKey, null)

    fun removePlaylist(id: String) {
        val playlists = getAllSavedPlaylists().filterNot { it.id == id }
        val json = gson.toJson(playlists)
        playlistPrefs.edit().putString("playlists", json).apply()

        if (getActivePlaylistId() == id) {
            playlistPrefs.edit().remove(activePlaylistKey).apply()
        }
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient
            .Builder()
            .connectionPool(ConnectionPool(3, 30, java.util.concurrent.TimeUnit.SECONDS))
            .cache(Cache(java.io.File(context.cacheDir, "http"), 20L * 1024 * 1024))
            .build()
    }
    private val parser = M3UParser()
    private val prefs: SharedPreferences =
        context.getSharedPreferences("xtream_configs", Context.MODE_PRIVATE)
    private val playlistCache: SharedPreferences =
        context.getSharedPreferences("playlist_cache", Context.MODE_PRIVATE)
    private val gson = Gson()

    private var _cachedChannels = MutableStateFlow<List<Channel>>(emptyList())
    val cachedChannels: Flow<List<Channel>> = _cachedChannels.asStateFlow()

    private var memoryCache: List<Channel>? = null
    private var memoryCacheTime: Long = 0
    private val memoryCacheValidityMs = 5 * 60 * 1000L

    private val _xtreamConfigs = MutableStateFlow<List<XtreamConfig>>(emptyList())
    private val _activeXtreamConfig = MutableStateFlow<XtreamConfig?>(null)

    val xtreamConfigs: Flow<List<XtreamConfig>> = _xtreamConfigs.asStateFlow()

    init {

        loadSavedConfiguration()
    }

    private fun loadSavedConfiguration() {
        val savedConfig = getXtreamConfig()
        if (savedConfig != null) {
            _activeXtreamConfig.value = savedConfig
            _xtreamConfigs.value = listOf(savedConfig)
        }
    }

    fun getAllXtreamConfigs(): Flow<List<XtreamConfig>> = _xtreamConfigs.asStateFlow()

    fun getActiveXtreamConfig(): Flow<XtreamConfig?> = _activeXtreamConfig.asStateFlow()

    suspend fun loadChannelsFromM3U(url: String): List<Channel> =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("Failed to fetch playlist: ${response.code}")
                    }
                    val playlistContent = response.body?.string().orEmpty()
                    val channels = parser.parse(playlistContent)
                    _cachedChannels.value = channels
                    channels
                }
            } catch (e: Exception) {
                println("Error loading M3U playlist: ${e.message}")

                _cachedChannels.value
            }
        }

    fun getXtreamConfig(): XtreamConfig? {
        val host = prefs.getString("host", null)
        val username = prefs.getString("username", null)
        val password = prefs.getString("password", null)

        return if (host != null && username != null && password != null) {
            XtreamConfig(1, "Default", host, username, password, isActive = true)
        } else {
            null
        }
    }

    fun clearXtreamConfig() {
        prefs.edit().clear().apply()
        _activeXtreamConfig.value = null
        _xtreamConfigs.value = emptyList()
        println("DEBUG: Cleared Xtream config")
    }

    suspend fun loadChannelsFromXtream(
        config: XtreamConfig,
        forceRefresh: Boolean = false,
    ): List<Channel> =
        withContext(Dispatchers.IO) {
            try {
                if (!forceRefresh) {
                    val currentTime = System.currentTimeMillis()
                    if (memoryCache != null &&
                        (currentTime - memoryCacheTime) < memoryCacheValidityMs
                    ) {
                        _cachedChannels.value = memoryCache!!
                        return@withContext memoryCache!!
                    }

                    val cachedChannels =
                        loadPlaylistFromCache("xtream_${config.host}_${config.username}")
                    if (cachedChannels.isNotEmpty()) {
                        memoryCache = cachedChannels
                        memoryCacheTime = currentTime
                        _cachedChannels.value = cachedChannels
                        return@withContext cachedChannels
                    }
                }

                val base = if (config.host.endsWith('/')) config.host else config.host + "/"
                val retrofit =
                    Retrofit
                        .Builder()
                        .baseUrl(base)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                val service = retrofit.create(XtreamApiService::class.java)

                val categoriesResponse =
                    service.getLiveCategories(config.username, config.password)
                val categoryMap =
                    if (categoriesResponse.isSuccessful && categoriesResponse.body() != null) {
                        categoriesResponse.body()!!.associate {
                            it.category_id to it.category_name
                        }
                    } else {
                        emptyMap()
                    }

                val response = service.getLiveStreams(config.username, config.password)
                if (!response.isSuccessful || response.body() == null) {
                    throw IOException("Failed to fetch Xtream channels: ${response.code()}")
                }

                val xtreamChannels: List<XtreamChannel> = response.body()!!

                val channels =
                    xtreamChannels.map { ch ->
                        val categoryName = categoryMap[ch.category_id] ?: "Uncategorized"
                        val host = base.trimEnd('/')
                        Channel(
                            name = ch.name,
                            url =
                                "$host/live/${config.username}/${config.password}/${ch.stream_id}.m3u8",
                            logo = ch.stream_icon,
                            group = categoryName,
                        )
                    }

                savePlaylistToCache("xtream_${config.host}_${config.username}", channels)

                val currentTime = System.currentTimeMillis()
                memoryCache = channels
                memoryCacheTime = currentTime

                _cachedChannels.value = channels

                channels
            } catch (e: Exception) {
                if (!forceRefresh) {
                    val cachedChannels =
                        loadPlaylistFromCache("xtream_${config.host}_${config.username}")
                    if (cachedChannels.isNotEmpty()) {
                        _cachedChannels.value = cachedChannels
                        cachedChannels
                    } else {
                        emptyList()
                    }
                } else {
                    emptyList()
                }
            }
        }

    private fun loadPlaylistFromCache(key: String): List<Channel> {
        val cacheKey = "${key}_data"
        val timestampKey = "${key}_timestamp"

        val timestamp = playlistCache.getLong(timestampKey, 0L)
        if (timestamp == 0L) {
            return emptyList()
        }

        val currentTime = System.currentTimeMillis()
        val cacheAgeMs = currentTime - timestamp
        val maxCacheAgeMs = 15 * 24 * 60 * 60 * 1000L

        if (cacheAgeMs > maxCacheAgeMs) {
            playlistCache
                .edit()
                .remove(cacheKey)
                .remove(timestampKey)
                .apply()
            return emptyList()
        }

        val cachedData = playlistCache.getString(cacheKey, null) ?: return emptyList()

        return try {
            val type = object : TypeToken<List<Channel>>() {}.type
            gson.fromJson(cachedData, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun savePlaylistToCache(
        key: String,
        channels: List<Channel>,
    ) {
        val cacheKey = "${key}_data"
        val timestampKey = "${key}_timestamp"

        val json = gson.toJson(channels)
        playlistCache
            .edit()
            .putString(cacheKey, json)
            .putLong(timestampKey, System.currentTimeMillis())
            .apply()
    }

    fun clearAllPlaylistCaches() {
        val editor = playlistCache.edit()
        editor.clear()
        editor.apply()
    }

    suspend fun loadChannelsFromUrl(url: String) = loadChannelsFromM3U(url)

    fun saveXtreamConfig(config: XtreamConfig): Long {
        val prefsEditor = prefs.edit()
        prefsEditor.putString("host", config.host)
        prefsEditor.putString("username", config.username)
        prefsEditor.putString("password", config.password)
        prefsEditor.apply()

        val activeConfig = config.copy(isActive = true)
        _activeXtreamConfig.value = activeConfig
        _xtreamConfigs.value = listOf(activeConfig)

        return config.id
    }

    fun setActiveXtreamConfig(id: Long) {
        val current = getXtreamConfig()
        if (current != null) {
            val updated = current.copy(id = id, isActive = true)
            _activeXtreamConfig.value = updated
            _xtreamConfigs.value = listOf(updated)
        }
    }

    fun deleteXtreamConfig(config: XtreamConfig) {
        clearXtreamConfig()
        _activeXtreamConfig.value = null
        _xtreamConfigs.value = emptyList()
    }

    suspend fun testXtreamConnection(
        host: String,
        username: String,
        password: String,
    ): Result<Boolean> =
        try {
            val config = XtreamConfig(0, "Test", host, username, password)
            val channels = loadChannelsFromXtream(config)
            Result.success(channels.isNotEmpty())
        } catch (e: Exception) {
            Result.failure(e)
        }

    fun clearChannelCache() {
        clearAllPlaylistCaches()
        memoryCache = null
        memoryCacheTime = 0
        _cachedChannels.value = emptyList()
    }
}
