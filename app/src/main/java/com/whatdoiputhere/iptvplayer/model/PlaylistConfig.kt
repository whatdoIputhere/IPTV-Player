package com.whatdoiputhere.iptvplayer.model

/**
 * Represents a saved playlist, either M3U or Xtream.
 */
data class PlaylistConfig(
    val id: String,
    val displayName: String,
    val type: String,
    val data: String,
)
