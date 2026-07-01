package com.zachvlat.gamelibrary.library.store.steam

import kotlinx.serialization.Serializable

@Serializable
data class SteamScrapedGame(
    val appId: String? = null,
    val name: String? = null,
    val storeUrl: String? = null,
    val headerImage: String? = null,
    val libraryImage: String? = null,
    val achievements: SteamScrapedAchievements? = null,
    val community: SteamScrapedCommunity? = null,
    val links: SteamScrapedLinks? = null
)

@Serializable
data class SteamScrapedAchievements(
    val earned: Int? = null,
    val total: Int? = null,
    val percent: Double? = null
)

@Serializable
data class SteamScrapedCommunity(
    val user: String? = null,
    val myAchievements: String? = null,
    val globalAchievements: String? = null,
    val groups: String? = null
)

@Serializable
data class SteamScrapedLinks(
    val forums: String? = null,
    val officialWebsite: String? = null,
    val news: String? = null
)
