package com.zachvlat.gamelibrary.library.store.itch

import android.util.Log
import com.zachvlat.gamelibrary.library.auth.TokenStorage
import com.zachvlat.gamelibrary.library.model.GameInfo
import com.zachvlat.gamelibrary.library.model.LoginData
import com.zachvlat.gamelibrary.library.model.Store
import com.zachvlat.gamelibrary.library.store.StoreClient
import com.zachvlat.gamelibrary.library.util.ItchConstants
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ItchStoreClient(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage
) : StoreClient {

    companion object {
        private const val TAG = "ItchStoreClient"
        private const val KEY_GAMES_JSON = "games_json"
        private const val KEY_PURCHASES_URL = "purchases_url"
    }

    override val store: Store get() = Store.ITCH

    private var cachedGames: List<GameInfo>? = null

    override suspend fun isLoggedIn(): Boolean {
        val json = tokenStorage.getToken(store.name, KEY_GAMES_JSON)
        val purchasesUrl = tokenStorage.getToken(store.name, KEY_PURCHASES_URL)
        return json != null && purchasesUrl != null
    }

    suspend fun getPurchasesUrl(): String? {
        return tokenStorage.getToken(store.name, KEY_PURCHASES_URL)
    }

    override suspend fun getLoginData(): LoginData {
        return LoginData(url = ItchConstants.PURCHASES_URL)
    }

    override suspend fun completeLogin(jsonData: String): Boolean {
        if (jsonData.isBlank()) return false
        return try {
            val (gamesJson, purchasesUrl) = parseItchResponse(jsonData)
            val games = Json.decodeFromString<List<ItchGameData>>(gamesJson)
            tokenStorage.saveToken(store.name, KEY_GAMES_JSON, gamesJson)
            if (purchasesUrl != null) {
                tokenStorage.saveToken(store.name, KEY_PURCHASES_URL, purchasesUrl)
            }
            cachedGames = games.map { it.toGameInfo() }
            Log.d(TAG, "Successfully imported ${games.size} games")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse imported games JSON: ${e.message}", e)
            false
        }
    }

    override suspend fun refreshLibrary(): List<GameInfo> {
        if (cachedGames == null) {
            loadGamesFromStorage()
        }
        return cachedGames ?: emptyList()
    }

    override suspend fun logout() {
        tokenStorage.clearTokens(store.name)
        cachedGames = null
    }

    private suspend fun loadGamesFromStorage() {
        val json = tokenStorage.getToken(store.name, KEY_GAMES_JSON)
        if (json != null) {
            try {
                val (gamesJson, _) = parseItchResponse(json)
                val games = Json.decodeFromString<List<ItchGameData>>(gamesJson)
                cachedGames = games.map { it.toGameInfo() }
                Log.d(TAG, "Loaded ${cachedGames!!.size} games from storage")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to decode stored games JSON: ${e.message}", e)
                tokenStorage.clearTokens(store.name)
                cachedGames = null
            }
        } else {
            cachedGames = null
        }
    }

    private fun parseItchResponse(raw: String): Pair<String, String?> {
        val element = Json.parseToJsonElement(raw)
        return if (element.jsonObject.containsKey("games")) {
            val games = element.jsonObject["games"]!!.jsonArray.toString()
            val purchasesUrl = element.jsonObject["purchasesUrl"]?.jsonPrimitive?.content
            Pair(games, purchasesUrl)
        } else {
            Pair(raw, null)
        }
    }

    private fun ItchGameData.toGameInfo(): GameInfo {
        return GameInfo(
            store = Store.ITCH,
            appName = "itch_$id",
            title = title,
            developer = author,
            description = null,
            artCover = cover?.let { if (it.startsWith("//")) "https:$it" else it },
            artSquare = null,
            artLogo = null,
            artBackground = null,
            releaseDate = null,
            genres = null,
            canRunOffline = false,
            storeUrl = url,
            isLinuxNative = false,
            isMacNative = false
        )
    }
}
