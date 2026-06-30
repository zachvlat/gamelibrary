package com.zachvlat.gamelibrary.library.store.steam

import android.webkit.CookieManager
import com.zachvlat.gamelibrary.library.auth.TokenStorage
import com.zachvlat.gamelibrary.library.model.GameInfo
import com.zachvlat.gamelibrary.library.model.LoginData
import com.zachvlat.gamelibrary.library.model.Store
import com.zachvlat.gamelibrary.library.store.StoreClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders

class SteamStoreClient(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage
) : StoreClient {

    override val store: Store get() = Store.STEAM

    override suspend fun isLoggedIn(): Boolean {
        return getSteamId() != null
    }

    override suspend fun getLoginData(): LoginData {
        val params = listOf(
            "openid.ns" to "http://specs.openid.net/auth/2.0",
            "openid.mode" to "checkid_setup",
            "openid.return_to" to SteamConstants.REDIRECT_URL,
            "openid.realm" to "https://gamelibrary",
            "openid.identity" to "http://specs.openid.net/auth/2.0/identifier_select",
            "openid.claimed_id" to "http://specs.openid.net/auth/2.0/identifier_select"
        )
        val url = SteamConstants.OPENID_URL + "?" + params.joinToString("&") { "${it.first}=${java.net.URLEncoder.encode(it.second, "UTF-8")}" }
        return LoginData(url)
    }

    override suspend fun completeLogin(authCode: String): Boolean {
        val steamId = extractSteamId(authCode) ?: return false
        tokenStorage.saveToken(store.name, "steam_id", steamId)
        try {
            val cookies = CookieManager.getInstance().getCookie("https://store.steampowered.com")
                ?: CookieManager.getInstance().getCookie("https://steamcommunity.com")
            if (cookies != null) {
                tokenStorage.saveToken(store.name, "cookies", cookies)
                println("[Steam] Saved cookies: ${cookies.take(80)}...")
            } else {
                println("[Steam] No cookies found")
            }
        } catch (e: Exception) {
            println("[Steam] Cookie error: ${e.message}")
        }
        return true
    }

    override suspend fun refreshLibrary(): List<GameInfo> {
        getSteamId() ?: throw IllegalStateException("Not authenticated with Steam")
        val cookies = getSavedCookies() ?: throw IllegalStateException("Session expired, please re-login")

        val userData = fetchUserData(cookies)
        val ownedAppIds = userData.rgOwnedApps
        println("[Steam] rgOwnedApps: ${ownedAppIds?.size ?: "null"}")
        if (ownedAppIds.isNullOrEmpty()) return emptyList()

        val batches = ownedAppIds.chunked(50)
        val games = mutableListOf<GameInfo>()
        for (batch in batches) {
            val details = fetchAppDetails(batch, cookies)
            for ((appIdStr, detailResponse) in details) {
                if (detailResponse.success && detailResponse.data != null) {
                    games.add(detailResponse.data.toGameInfo())
                } else if (detailResponse.data == null) {
                    val appId = appIdStr.toLongOrNull() ?: continue
                    games.add(basicGameInfo(appId))
                }
            }
        }
        println("[Steam] Loaded ${games.size} games")
        return games
    }

    override suspend fun logout() {
        tokenStorage.clearTokens(store.name)
    }

    private suspend fun fetchUserData(cookies: String): SteamUserDataResponse {
        val url = "https://store.steampowered.com/dynamicstore/userdata/"
        val response: HttpResponse = httpClient.get(url) {
            header(HttpHeaders.Cookie, cookies)
            header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            header("Referer", "https://store.steampowered.com/")
        }
        println("[Steam] userdata status: ${response.status}")
        return response.body()
    }

    private suspend fun fetchAppDetails(
        appIds: List<Long>,
        cookies: String
    ): Map<String, SteamAppDetailsResponse> {
        val ids = appIds.joinToString(",")
        val url = "https://store.steampowered.com/api/appdetails"
        val response: HttpResponse = httpClient.get(url) {
            header(HttpHeaders.Cookie, cookies)
            parameter("appids", ids)
            parameter("cc", "US")
            parameter("l", "en")
        }
        return response.body()
    }

    private suspend fun getSavedCookies(): String? = tokenStorage.getToken(store.name, "cookies")

    private suspend fun getSteamId(): String? = tokenStorage.getToken(store.name, "steam_id")

    private fun extractSteamId(claimedId: String): String? {
        val parts = claimedId.trimEnd('/').split("/")
        return parts.lastOrNull()?.takeIf { it.all { c -> c.isDigit() } }
    }

    private fun basicGameInfo(appId: Long): GameInfo {
        return GameInfo(
            store = Store.STEAM,
            appName = appId.toString(),
            title = "Unknown",
            developer = null,
            description = null,
            artCover = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/$appId/header.jpg",
            artSquare = null,
            artLogo = null,
            artBackground = null,
            releaseDate = null,
            genres = null,
            canRunOffline = false,
            storeUrl = "https://store.steampowered.com/app/$appId",
            isLinuxNative = false,
            isMacNative = false
        )
    }

    private fun SteamAppData.toGameInfo(): GameInfo {
        return GameInfo(
            store = Store.STEAM,
            appName = steamAppid.toString(),
            title = name.ifBlank { "Unknown" },
            developer = developers?.firstOrNull(),
            description = shortDescription ?: description,
            artCover = header_image ?: "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/$steamAppid/header.jpg",
            artSquare = capsule_image,
            artLogo = null,
            artBackground = null,
            releaseDate = release_date?.date?.takeIf { it.isNotBlank() },
            genres = genres?.mapNotNull { it.description.takeIf { g -> g.isNotBlank() } }?.takeIf { it.isNotEmpty() },
            canRunOffline = false,
            storeUrl = "https://store.steampowered.com/app/$steamAppid",
            isLinuxNative = platforms?.linux == true,
            isMacNative = platforms?.mac == true
        )
    }
}
