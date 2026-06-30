package com.zachvlat.gamelibrary.library.store.gog

import com.zachvlat.gamelibrary.library.auth.TokenStorage
import com.zachvlat.gamelibrary.library.model.GameInfo
import com.zachvlat.gamelibrary.library.model.LoginData
import com.zachvlat.gamelibrary.library.model.Store
import com.zachvlat.gamelibrary.library.store.StoreClient
import com.zachvlat.gamelibrary.library.util.GogConstants
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
class GogStoreClient(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage
) : StoreClient {

    override val store: Store = Store.GOG

    override suspend fun isLoggedIn(): Boolean {
        val accessToken = tokenStorage.getToken(store.name, "access_token")
        return accessToken != null
    }

    override suspend fun getLoginData(): LoginData {
        val url = buildString {
            append(GogConstants.AUTH_URL)
            append("?client_id=${GogConstants.CLIENT_ID}")
            append("&redirect_uri=${GogConstants.REDIRECT_URI}")
            append("&response_type=code")
            append("&layout=client2")
        }
        return LoginData(url = url)
    }

    override suspend fun completeLogin(authCode: String): Boolean {
        return try {
            val response: HttpResponse = httpClient.get(GogConstants.TOKEN_URL) {
                parameter("client_id", GogConstants.CLIENT_ID)
                parameter("client_secret", GogConstants.CLIENT_SECRET)
                parameter("grant_type", "authorization_code")
                parameter("code", authCode)
                parameter("redirect_uri", GogConstants.REDIRECT_URI)
            }

            if (!response.status.isSuccess()) return false

            val tokenResponse: GogTokenResponse = response.body()
            tokenStorage.saveToken(store.name, "access_token", tokenResponse.accessToken)
            tokenStorage.saveToken(store.name, "expires_in", tokenResponse.expiresIn.toString())
            tokenStorage.saveToken(store.name, "token_type", tokenResponse.tokenType)
            if (tokenResponse.refreshToken != null) {
                tokenStorage.saveToken(store.name, "refresh_token", tokenResponse.refreshToken)
            }
            if (tokenResponse.userId != null) {
                tokenStorage.saveToken(store.name, "user_id", tokenResponse.userId)
            }
            if (tokenResponse.sessionId != null) {
                tokenStorage.saveToken(store.name, "session_id", tokenResponse.sessionId)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun refreshLibrary(): List<GameInfo> {
        val accessToken = tokenStorage.getToken(store.name, "access_token")
            ?: throw IllegalStateException("Not authenticated with GOG")

        val userId = tokenStorage.getToken(store.name, "user_id")
            ?: throw IllegalStateException("User ID not found")

        println("[GOG] Fetching library releases...")
        val externalIds = fetchLibraryReleases(accessToken, userId)
        println("[GOG] Found ${externalIds.size} releases, fetching metadata...")

        val games = mutableListOf<GameInfo>()

        for ((i, item) in externalIds.withIndex()) {
            try {
                println("[GOG] [${i + 1}/${externalIds.size}] ${item.externalId}")
                val metadata = fetchGameMetadata(accessToken, item.externalId, item.certificate)
                if (metadata != null) {
                    val gameInfo = normalizeMetadata(metadata, item.externalId)
                    gameInfo?.let { games.add(it) }
                }
            } catch (_: Exception) { }
        }

        println("[GOG] Done — ${games.size} games loaded")
        return games
    }

    override suspend fun logout() {
        tokenStorage.clearTokens(store.name)
    }

    private suspend fun fetchLibraryReleases(
        accessToken: String,
        userId: String
    ): List<GogLibraryItem> {
        val items = mutableListOf<GogLibraryItem>()
        var pageToken: String? = null
        var page = 1

        do {
            println("[GOG] Fetching library page $page...")
            val response: HttpResponse = httpClient.get(
                "${GogConstants.GALAXY_LIBRARY_API}/$userId/releases"
            ) {
                header("Authorization", "Bearer $accessToken")
                if (pageToken != null) {
                    parameter("page_token", pageToken)
                }
            }

            if (!response.status.isSuccess()) {
                throw IllegalStateException("Failed to fetch library: ${response.status}")
            }

            val libraryResponse: GogLibraryResponse = response.body()
            val gogItems = libraryResponse.items.filter { it.platformId == "gog" }
            items.addAll(gogItems)
            println("[GOG] Page $page: ${gogItems.size} items (total: ${items.size})")
            pageToken = libraryResponse.nextPageToken
            page++
        } while (pageToken != null)

        return items
    }

    private suspend fun fetchGameMetadata(
        accessToken: String,
        externalId: String,
        certificate: String?
    ): GogGameMetadata? {
        val response: HttpResponse = httpClient.get(
            "${GogConstants.GAMESDB_API}/$externalId"
        ) {
            header("Authorization", "Bearer $accessToken")
            if (certificate != null) {
                header("X-GOG-Library-Cert", certificate)
            }
            header("User-Agent", "HeroicGamesLauncher/2.15.0")
        }

        if (!response.status.isSuccess()) return null

        return try {
            response.body<GogGameMetadata>()
        } catch (_: Exception) {
            null
        }
    }

    private fun normalizeMetadata(metadata: GogGameMetadata, externalId: String): GameInfo? {
        val gameInfo = metadata.game ?: return null
        if (gameInfo.type == "dlc" || gameInfo.type == "spam" || gameInfo.type == "mod") return null
        if (gameInfo.visibleInLibrary == false) return null

        val title = metadata.title["*"] ?: metadata.title["en-US"] ?: "Unknown"
        val description = metadata.summary?.get("*") ?: metadata.summary?.get("en-US")

        val releaseDate = gameInfo.firstReleaseDate ?: gameInfo.releaseDate
        val slug = extractSlug(metadata.id)

        val genres = gameInfo.genres?.mapNotNull { genre ->
            genre.name["*"] ?: genre.name["en-US"]
        }

        return GameInfo(
            store = Store.GOG,
            appName = externalId,
            title = title,
            developer = gameInfo.developers?.firstOrNull()?.name,
            description = description,
            artCover = formatGogImage(gameInfo.cover?.urlFormat),
            artSquare = formatGogImage(gameInfo.squareIcon?.urlFormat),
            artLogo = formatGogImage(gameInfo.logo?.urlFormat),
            artBackground = formatGogImage(gameInfo.background?.urlFormat)
                ?: formatGogImage(gameInfo.horizontalArtwork?.urlFormat),
            releaseDate = releaseDate,
            genres = genres,
            canRunOffline = true,
            storeUrl = "${GogConstants.STORE_BASE_URL}/$slug",
            isLinuxNative = metadata.supportedOperatingSystems?.any { it.slug == "linux" } ?: false,
            isMacNative = metadata.supportedOperatingSystems?.any { it.slug == "mac" } ?: false
        )
    }

    private fun extractSlug(id: String): String {
        return id.lowercase().replace(" ", "-")
    }

    private fun formatGogImage(urlFormat: String?): String? {
        if (urlFormat == null) return null
        val url = urlFormat
            .replace("{formatter}", "")
            .replace("{ext}", "webp")
        return if (url.startsWith("//")) "https:$url" else url
    }
}
