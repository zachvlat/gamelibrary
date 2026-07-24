package com.zachvlat.gamelibrary.library.store.steam

import kotlinx.serialization.Serializable

@Serializable
data class SteamApiGamesResponse(
    val response: SteamApiGamesData
)

@Serializable
data class SteamApiGamesData(
    val games: List<SteamApiGame>? = null,
    val game_count: Int? = null
)

@Serializable
data class SteamApiGame(
    val appid: Int,
    val name: String,
    val playtime_forever: Int? = null,
    val playtime_windows_forever: Int? = null,
    val playtime_mac_forever: Int? = null,
    val playtime_linux_forever: Int? = null,
    val img_icon_url: String? = null,
    val header_image: String? = null
)
