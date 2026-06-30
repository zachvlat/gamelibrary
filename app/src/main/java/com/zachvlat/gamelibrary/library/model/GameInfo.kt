package com.zachvlat.gamelibrary.library.model

data class GameInfo(
    val store: Store,
    val appName: String,
    val title: String,
    val developer: String?,
    val description: String?,
    val artCover: String?,
    val artSquare: String?,
    val artLogo: String?,
    val artBackground: String?,
    val releaseDate: String?,
    val genres: List<String>?,
    val canRunOffline: Boolean,
    val storeUrl: String?,
    val isLinuxNative: Boolean,
    val isMacNative: Boolean
)
