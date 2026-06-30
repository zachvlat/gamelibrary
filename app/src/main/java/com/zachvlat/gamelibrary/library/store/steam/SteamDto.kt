package com.zachvlat.gamelibrary.library.store.steam

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SteamUserDataResponse(
    val rgOwnedApps: List<Long>? = null,
    val rgOwnedPackages: List<Long>? = null
)

@Serializable
data class SteamAppDetailsResponse(
    val success: Boolean = false,
    val data: SteamAppData? = null
)

@Serializable
data class SteamAppData(
    @SerialName("steam_appid") val steamAppid: Long = 0,
    val name: String = "",
    val developers: List<String>? = null,
    val publishers: List<String>? = null,
    val description: String? = null,
    @SerialName("short_description") val shortDescription: String? = null,
    val header_image: String? = null,
    val capsule_image: String? = null,
    val release_date: SteamReleaseDate? = null,
    val genres: List<SteamGenre>? = null,
    val platforms: SteamPlatforms? = null,
    val metacritic: SteamMetacritic? = null
)

@Serializable
data class SteamReleaseDate(
    val coming_soon: Boolean = false,
    val date: String = ""
)

@Serializable
data class SteamGenre(
    val id: String = "",
    val description: String = ""
)

@Serializable
data class SteamPlatforms(
    val windows: Boolean = false,
    val mac: Boolean = false,
    val linux: Boolean = false
)

@Serializable
data class SteamMetacritic(
    val score: Int = 0,
    val url: String = ""
)
