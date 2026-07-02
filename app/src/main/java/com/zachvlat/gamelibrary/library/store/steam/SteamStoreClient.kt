package com.zachvlat.gamelibrary.library.store.steam

import android.util.Log
import com.zachvlat.gamelibrary.library.auth.TokenStorage
import com.zachvlat.gamelibrary.library.model.GameInfo
import com.zachvlat.gamelibrary.library.model.LoginData
import com.zachvlat.gamelibrary.library.model.Store
import com.zachvlat.gamelibrary.library.store.StoreClient
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class SteamStoreClient(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage
) : StoreClient {

    companion object {
        private const val TAG = "SteamStoreClient"
        private const val KEY_GAMES_JSON = "games_json"
        private const val KEY_PROFILE_URL = "profile_url"
    }

    override val store: Store get() = Store.STEAM

    private var scrapedGames: List<GameInfo>? = null

    override suspend fun isLoggedIn(): Boolean {
        val json = tokenStorage.getToken(store.name, KEY_GAMES_JSON)
        val profileUrl = tokenStorage.getToken(store.name, KEY_PROFILE_URL)
        return json != null && profileUrl != null
    }

    suspend fun getProfileUrl(): String? {
        return tokenStorage.getToken(store.name, KEY_PROFILE_URL)
    }

    override suspend fun getLoginData(): LoginData {
        return LoginData(url = "https://steamcommunity.com/my/games/?tab=all")
    }

    override suspend fun completeLogin(authCode: String): Boolean {
        if (authCode.isBlank()) return false
        return try {
            val (gamesJson, profileUrl) = parseSteamResponse(authCode)
            val steamGames = Json.decodeFromString<List<SteamScrapedGame>>(gamesJson)
            val gameInfos = steamGames.map { it.toGameInfo() }
            tokenStorage.saveToken(store.name, KEY_GAMES_JSON, gamesJson)
            if (profileUrl != null) {
                tokenStorage.saveToken(store.name, KEY_PROFILE_URL, profileUrl)
            }
            scrapedGames = gameInfos
            Log.d(TAG, "Successfully imported ${gameInfos.size} games")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse imported games JSON: ${e.message}", e)
            false
        }
    }

    override suspend fun refreshLibrary(): List<GameInfo> {
        if (scrapedGames == null) {
            loadGamesFromStorage()
        }
        return scrapedGames ?: emptyList()
    }

    override suspend fun logout() {
        tokenStorage.clearTokens(store.name)
        scrapedGames = null
    }

    private suspend fun loadGamesFromStorage() {
        val json = tokenStorage.getToken(store.name, KEY_GAMES_JSON)
        if (json != null) {
            try {
                val (gamesJson, _) = parseSteamResponse(json)
                val steamGames = Json.decodeFromString<List<SteamScrapedGame>>(gamesJson)
                scrapedGames = steamGames.map { it.toGameInfo() }
                Log.d(TAG, "Loaded ${scrapedGames!!.size} games from storage")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to decode stored games JSON: ${e.message}", e)
                tokenStorage.clearTokens(store.name)
                scrapedGames = null
            }
        } else {
            scrapedGames = null
        }
    }

    private fun parseSteamResponse(raw: String): Pair<String, String?> {
        val element = Json.parseToJsonElement(raw)
        return if (element.jsonObject.containsKey("games")) {
            val games = element.jsonObject["games"]!!.jsonArray.toString()
            val profileUrl = element.jsonObject["profileUrl"]?.jsonPrimitive?.content
            Pair(games, profileUrl)
        } else {
            Pair(raw, null)
        }
    }

    private fun SteamScrapedGame.toGameInfo(): GameInfo {
        val appId = this.appId
            ?: (this.storeUrl?.let { """app/(\d+)""".toRegex().find(it)?.groupValues?.getOrNull(1) })
            ?: "unknown"

        val effectiveStoreUrl = this.storeUrl ?: "https://store.steampowered.com/app/$appId"

        return GameInfo(
            store = Store.STEAM,
            appName = appId,
            title = this.name ?: "Unknown",
            developer = null,
            description = null,
            artCover = this.libraryImage ?: this.headerImage,
            artSquare = this.headerImage,
            artLogo = null,
            artBackground = null,
            releaseDate = null,
            genres = null,
            canRunOffline = false,
            storeUrl = effectiveStoreUrl,
            isLinuxNative = false,
            isMacNative = false
        )
    }
}