package com.zachvlat.gamelibrary.library.store.itch

import com.zachvlat.gamelibrary.library.auth.TokenStorage
import com.zachvlat.gamelibrary.library.model.GameInfo
import com.zachvlat.gamelibrary.library.model.LoginData
import com.zachvlat.gamelibrary.library.model.Store
import com.zachvlat.gamelibrary.library.store.StoreClient
import com.zachvlat.gamelibrary.library.util.ItchConstants
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

class ItchStoreClient(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage
) : StoreClient {

    override val store: Store get() = Store.ITCH

    override suspend fun isLoggedIn(): Boolean {
        return tokenStorage.getToken(store.name, "games_json") != null
    }

    override suspend fun getLoginData(): LoginData {
        return LoginData(url = ItchConstants.PURCHASES_URL)
    }

    override suspend fun completeLogin(jsonData: String): Boolean {
        return try {
            val games = Json.decodeFromString<List<ItchGameData>>(jsonData)
            tokenStorage.saveToken(store.name, "games_json", jsonData)
            games.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun refreshLibrary(): List<GameInfo> {
        val json = tokenStorage.getToken(store.name, "games_json")
            ?: throw IllegalStateException("No itch.io data cached. Please login again.")
        val games = Json.decodeFromString<List<ItchGameData>>(json)
        return games.map { it.toGameInfo() }
    }

    override suspend fun logout() {
        tokenStorage.clearTokens(store.name)
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
