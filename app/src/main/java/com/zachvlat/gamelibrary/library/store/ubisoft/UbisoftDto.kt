package com.zachvlat.gamelibrary.library.store.ubisoft

import kotlinx.serialization.Serializable

@Serializable
data class UbisoftGameData(
    val name: String,
    val cover: String? = null,
    val playTime: String? = null,
    val lastPlayed: String? = null,
    val platform: String? = null
)
