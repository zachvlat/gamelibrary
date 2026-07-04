package com.zachvlat.gamelibrary.library.store.ea

import android.util.Log
import com.zachvlat.gamelibrary.library.auth.TokenStorage
import com.zachvlat.gamelibrary.library.model.GameInfo
import com.zachvlat.gamelibrary.library.model.LoginData
import com.zachvlat.gamelibrary.library.model.Store
import com.zachvlat.gamelibrary.library.store.StoreClient
import com.zachvlat.gamelibrary.library.util.EaConstants
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

private val eaJson = Json { ignoreUnknownKeys = true }

class EaStoreClient(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage
) : StoreClient {

    companion object {
        private const val TAG = "EaStoreClient"
        private const val KEY_ORDER_JSON = "order_json"
    }

    override val store: Store get() = Store.EA

    private var cachedGames: List<GameInfo>? = null

    override suspend fun isLoggedIn(): Boolean {
        return tokenStorage.getToken(store.name, KEY_ORDER_JSON) != null
    }

    override suspend fun getLoginData(): LoginData {
        return LoginData(url = EaConstants.ORDER_HISTORY_URL)
    }

    override suspend fun completeLogin(jsonData: String): Boolean {
        if (jsonData.isBlank()) return false
        return try {
            val response = eaJson.decodeFromString<EaOrderHistoryResponse>(jsonData)
            val games = parseGames(response)
            tokenStorage.saveToken(store.name, KEY_ORDER_JSON, jsonData)
            cachedGames = games
            Log.d(TAG, "Successfully imported ${games.size} games")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse EA order JSON: ${e.message}", e)
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
        val json = tokenStorage.getToken(store.name, KEY_ORDER_JSON)
        if (json != null) {
            try {
                val response = eaJson.decodeFromString<EaOrderHistoryResponse>(json)
                cachedGames = parseGames(response)
                Log.d(TAG, "Loaded ${cachedGames!!.size} games from storage")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to decode stored EA JSON: ${e.message}", e)
                tokenStorage.clearTokens(store.name)
                cachedGames = null
            }
        } else {
            cachedGames = null
        }
    }

    private fun parseGames(response: EaOrderHistoryResponse): List<GameInfo> {
        val orders = response.result?.orders ?: return emptyList()
        val seen = mutableSetOf<String>()
        val games = mutableListOf<GameInfo>()

        for (order in orders) {
            if (order.status != "Completed") continue
            for (item in order.items) {
                if (item.itemType != "SALES") continue
                if (item.status != "FULFILLED") continue
                val name = item.name ?: continue
                val key = name.lowercase().trim()
                if (!seen.add(key)) continue
                games.add(item.toGameInfo(name, order.date))
            }
        }

        return games
    }

    private fun EaOrderItem.toGameInfo(title: String, releaseDate: String?): GameInfo {
        return GameInfo(
            store = Store.EA,
            appName = "ea_${title.lowercase().replace("[^a-z0-9]+".toRegex(), "_")}",
            title = title,
            developer = "Electronic Arts",
            description = null,
            artCover = thumbnail,
            artSquare = null,
            artLogo = null,
            artBackground = null,
            releaseDate = releaseDate,
            genres = null,
            canRunOffline = false,
            storeUrl = null,
            isLinuxNative = false,
            isMacNative = false
        )
    }
}
