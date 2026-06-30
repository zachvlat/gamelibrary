package com.zachvlat.gamelibrary.library.model

data class LoginData(
    val url: String,
    val state: Map<String, String> = emptyMap()
)
