package com.example.ptv.model

data class Channel(
    val id: String = "",
    val name: String,
    val url: String,
    val logo: String = "",
    val group: String = "",
    val language: String = "",
    val country: String = "",
)
