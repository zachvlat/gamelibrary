package com.zachvlat.gamelibrary.library.model

import kotlinx.serialization.Serializable

@Serializable
data class NowPlayingInfo(
    val store: Store,
    val appName: String,
    val completionPercent: Int
)
