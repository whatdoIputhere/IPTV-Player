package com.whatdoiputhere.iptvplayer.api

import com.whatdoiputhere.iptvplayer.model.XtreamCategory
import com.whatdoiputhere.iptvplayer.model.XtreamChannel
import com.whatdoiputhere.iptvplayer.model.XtreamInfo
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface XtreamApiService {
    @GET("player_api.php")
    suspend fun getAccountInfo(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_account_info",
    ): Response<XtreamInfo>

    @GET("player_api.php")
    suspend fun getLiveCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_categories",
    ): Response<List<XtreamCategory>>

    @GET("player_api.php")
    suspend fun getLiveStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_streams",
        @Query("category_id") categoryId: String? = null,
    ): Response<List<XtreamChannel>>

    @GET("player_api.php")
    suspend fun getVodCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_categories",
    ): Response<List<XtreamCategory>>

    @GET("player_api.php")
    suspend fun getVodStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_streams",
        @Query("category_id") categoryId: String? = null,
    ): Response<List<XtreamChannel>>
}
