package com.zachvlat.gamelibrary.library.store.ubisoft

import android.util.Log
import com.zachvlat.gamelibrary.library.auth.TokenStorage
import com.zachvlat.gamelibrary.library.model.GameInfo
import com.zachvlat.gamelibrary.library.model.LoginData
import com.zachvlat.gamelibrary.library.model.Store
import com.zachvlat.gamelibrary.library.store.StoreClient
import com.zachvlat.gamelibrary.library.util.UbisoftConstants
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

private val ubisoftJson = Json { ignoreUnknownKeys = true }

class UbisoftStoreClient(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage
) : StoreClient {

    companion object {
        private const val TAG = "UbisoftStoreClient"
        private const val KEY_GAMES_JSON = "games_json"
    }

    override val store: Store get() = Store.UBISOFT

    private var cachedGames: List<GameInfo>? = null

    override suspend fun isLoggedIn(): Boolean {
        return tokenStorage.getToken(store.name, KEY_GAMES_JSON) != null
    }

    override suspend fun getLoginData(): LoginData {
        return LoginData(url = UbisoftConstants.GAMES_ACTIVITY_URL)
    }

    override suspend fun completeLogin(authCode: String): Boolean {
        if (authCode.isBlank()) return false
        return try {
            val games = ubisoftJson.decodeFromString<List<UbisoftGameData>>(authCode)
            tokenStorage.saveToken(store.name, KEY_GAMES_JSON, authCode)
            cachedGames = games.map { it.toGameInfo() }
            Log.d(TAG, "Successfully imported ${games.size} games")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse Ubisoft games JSON: ${e.message}", e)
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
                val games = ubisoftJson.decodeFromString<List<UbisoftGameData>>(json)
                cachedGames = games.map { it.toGameInfo() }
                Log.d(TAG, "Loaded ${cachedGames!!.size} games from storage")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to decode stored Ubisoft JSON: ${e.message}", e)
                tokenStorage.clearTokens(store.name)
                cachedGames = null
            }
        } else {
            cachedGames = null
        }
    }

    private fun UbisoftGameData.toGameInfo(): GameInfo {
        val sanitized = name.lowercase().replace("[^a-z0-9]+".toRegex(), "_").trim('_')
        return GameInfo(
            store = Store.UBISOFT,
            appName = "ubisoft_$sanitized",
            title = name,
            developer = null,
            description = null,
            artCover = cover,
            artSquare = null,
            artLogo = null,
            artBackground = null,
            releaseDate = null,
            genres = null,
            canRunOffline = false,
            storeUrl = null,
            isLinuxNative = false,
            isMacNative = false
        )
    }
}
