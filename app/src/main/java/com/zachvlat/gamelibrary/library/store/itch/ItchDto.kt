package com.zachvlat.gamelibrary.library.store.itch

import kotlinx.serialization.Serializable

@Serializable
data class ItchGameData(
    val id: String,
    val title: String,
    val url: String? = null,
    val author: String? = null,
    val cover: String? = null
)
