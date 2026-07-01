package com.zachvlat.gamelibrary.library.store.steam

import com.zachvlat.gamelibrary.library.auth.TokenStorage
import com.zachvlat.gamelibrary.library.model.GameInfo
import com.zachvlat.gamelibrary.library.model.LoginData
import com.zachvlat.gamelibrary.library.model.Store
import com.zachvlat.gamelibrary.library.store.StoreClient
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

class SteamStoreClient(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage
) : StoreClient {

    override val store: Store get() = Store.STEAM

    private var scrapedGames: List<GameInfo>? = null

    override suspend fun isLoggedIn(): Boolean {
        return tokenStorage.getToken(store.name, "logged_in") != null
    }

    override suspend fun getLoginData(): LoginData {
        return LoginData(url = "")
    }

    override suspend fun completeLogin(authCode: String): Boolean {
        if (authCode.isBlank()) return false
        return try {
            val steamGames = Json.decodeFromString<List<SteamScrapedGame>>(authCode)
            scrapedGames = steamGames.map { it.toGameInfo() }
            tokenStorage.saveToken(store.name, "logged_in", "true")
            android.util.Log.d("SteamClient", "Parsed ${scrapedGames!!.size} games successfully")
            true
        } catch (e: Exception) {
            android.util.Log.e("SteamClient", "Failed to parse games JSON: ${e.message}", e)
            false
        }
    }

    override suspend fun refreshLibrary(): List<GameInfo> {
        val games = scrapedGames
        android.util.Log.d("SteamClient", "refreshLibrary: in-memory cache is ${if (games != null) "available (${games.size} games)" else "null"}")
        return games ?: emptyList()
    }

    override suspend fun logout() {
        tokenStorage.clearTokens(store.name)
        scrapedGames = null
    }

    private fun SteamScrapedGame.toGameInfo(): GameInfo {
        val id = appId ?: (storeUrl?.let { """/app/(\d+)""".toRegex().find(it)?.groupValues?.getOrNull(1) }) ?: "unknown"
        return GameInfo(
            store = Store.STEAM,
            appName = id,
            title = name ?: "Unknown",
            developer = null,
            description = null,
            artCover = libraryImage ?: headerImage,
            artSquare = headerImage,
            artLogo = null,
            artBackground = null,
            releaseDate = null,
            genres = null,
            canRunOffline = false,
            storeUrl = storeUrl ?: "https://store.steampowered.com/app/$id",
            isLinuxNative = false,
            isMacNative = false
        )
    }
}
