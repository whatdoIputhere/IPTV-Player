package com.whatdoiputhere.iptvplayer.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "xtream_configs")
data class XtreamConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val host: String,
    val username: String,
    val password: String,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

data class XtreamInfo(
    val userInfo: UserInfo? = null,
    val serverInfo: ServerInfo? = null,
)

data class UserInfo(
    val username: String? = null,
    val password: String? = null,
    val message: String? = null,
    val auth: Int? = null,
    val status: String? = null,
    val exp_date: String? = null,
    val is_trial: String? = null,
    val active_cons: String? = null,
    val created_at: String? = null,
    val max_connections: String? = null,
    val allowed_output_formats: List<String>? = null,
)

data class ServerInfo(
    val url: String? = null,
    val port: String? = null,
    val https_port: String? = null,
    val server_protocol: String? = null,
    val rtmp_port: String? = null,
    val timezone: String? = null,
    val timestamp_now: Long? = null,
    val time_now: String? = null,
)

data class XtreamCategory(
    val category_id: String,
    val category_name: String,
    val parent_id: Int = 0,
)

data class XtreamChannel(
    val num: Int,
    val name: String,
    val stream_type: String,
    val stream_id: Int,
    val stream_icon: String,
    val epg_channel_id: String? = null,
    val added: String,
    val is_adult: String,
    val category_id: String,
    val custom_sid: String? = null,
    val tv_archive: Int = 0,
    val direct_source: String? = null,
    val tv_archive_duration: Int = 0,
)
