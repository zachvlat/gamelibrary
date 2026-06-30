package com.zachvlat.gamelibrary.library.store.epic

import android.util.Log
import com.zachvlat.gamelibrary.library.auth.TokenStorage
import com.zachvlat.gamelibrary.library.model.GameInfo
import com.zachvlat.gamelibrary.library.model.LoginData
import com.zachvlat.gamelibrary.library.model.Store
import com.zachvlat.gamelibrary.library.store.StoreClient
import com.zachvlat.gamelibrary.library.util.EpicConstants
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Parameters
import io.ktor.http.isSuccess
import io.ktor.util.encodeBase64
import kotlinx.coroutines.delay

class EpicStoreClient(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage
) : StoreClient {

    override val store: Store = Store.EPIC

    companion object {
        private const val TAG = "EpicClient"
    }

    override suspend fun isLoggedIn(): Boolean {
        return tokenStorage.getToken(store.name, "access_token") != null
    }

    /**
     * Returns the Epic Games login URL.
     * User logs in via browser, then pastes the redirect URL they were sent to.
     */
    override suspend fun getLoginData(): LoginData {
        val redirectUrl = buildString {
            append(EpicConstants.REDIRECT_URL)
            append("?clientId=${EpicConstants.CLIENT_ID}")
            append("&responseType=code")
        }
        val loginUrl = buildString {
            append(EpicConstants.LOGIN_URL)
            append("?redirectUrl=")
            append(java.net.URLEncoder.encode(redirectUrl, "UTF-8"))
        }
        return LoginData(url = loginUrl)
    }

    /**
     * Exchanges the authorization code from the redirect URL for tokens.
     * The authCode should be the full redirect URL containing ?authorizationCode=...
     * or just the code itself.
     *
     * Uses HTTP Basic Auth with the legendary client credentials.
     */
    override suspend fun completeLogin(authCode: String): Boolean {
        val code = if (authCode.contains("authorizationCode=")) {
            val pairs = authCode.split("?").lastOrNull()?.split("&") ?: listOf(authCode)
            pairs.firstOrNull { it.startsWith("authorizationCode=") }
                ?.removePrefix("authorizationCode=") ?: authCode
        } else {
            authCode
        }

        val basicAuth = "${EpicConstants.CLIENT_ID}:${EpicConstants.CLIENT_SECRET}"
            .encodeBase64()

        val response: HttpResponse = httpClient.submitForm(
            url = EpicConstants.TOKEN_URL,
            formParameters = Parameters.build {
                append("grant_type", "authorization_code")
                append("code", code)
                append("token_type", "eg1")
            }
        ) {
            header("Authorization", "Basic $basicAuth")
            header("User-Agent", "UELauncherClient/3.0.0")
        }

        if (!response.status.isSuccess()) {
            throw Exception("Epic token exchange: HTTP ${response.status}")
        }

        val tokenResponse: EpicTokenResponse = response.body()
        tokenStorage.saveToken(store.name, "access_token", tokenResponse.accessToken)
        tokenStorage.saveToken(store.name, "expires_in", tokenResponse.expiresIn.toString())
        if (tokenResponse.refreshToken != null) {
            tokenStorage.saveToken(store.name, "refresh_token", tokenResponse.refreshToken)
        }
        if (tokenResponse.accountId != null) {
            tokenStorage.saveToken(store.name, "account_id", tokenResponse.accountId)
        }
        return true
    }

    /**
     * Fetches library items from Epic's library service, then enriches with catalog metadata.
     *
     * Based on legendary's implementation:
     * https://github.com/derrod/legendary/blob/master/legendary/api/egs.py
     */
    override suspend fun refreshLibrary(): List<GameInfo> {
        val accessToken = tokenStorage.getToken(store.name, "access_token")
            ?: throw IllegalStateException("Not authenticated with Epic")

        Log.d(TAG, "Fetching library records...")
        val records = fetchLibraryRecords(accessToken)
        Log.d(TAG, "Found ${records.size} library records, fetching metadata...")

        val byNamespace = records
            .filter { it.catalogItemId != null }
            .groupBy { it.namespace }

        var processed = 0
        val total = records.size
        val games = mutableListOf<GameInfo>()

        Log.d(TAG, "Namespaces: ${byNamespace.size}")
        var nsIndex = 0
        for ((namespace, nsRecords) in byNamespace) {
            nsIndex++
            val ids = nsRecords.mapNotNull { it.catalogItemId }
            Log.d(TAG, "[$nsIndex/${byNamespace.size}] Namespace '$namespace': ${ids.size} items")
            val metadataMap = fetchCatalogMetadataBatch(accessToken, namespace, ids)
            Log.d(TAG, "   -> got ${metadataMap.size} metadata entries")
            if (nsIndex < byNamespace.size) delay(250)

            for (record in nsRecords) {
                val catalogItemId = record.catalogItemId ?: continue
                processed++
                val label = record.title.ifBlank { record.appName.ifBlank { catalogItemId } }
                Log.d(TAG, "[$processed/$total] $label")
                val metadata = metadataMap[catalogItemId]
                games.add(if (metadata != null) normalizeGameInfo(record, metadata) else gameInfoFromRecord(record))
            }
        }

        val nullIdRecords = records.filter { it.catalogItemId == null }
        for (record in nullIdRecords) {
            processed++
            Log.d(TAG, "[$processed/$total] ${record.title.ifBlank { record.appName }}")
            games.add(gameInfoFromRecord(record))
        }

        val filtered = games.filter { !isHexId(it.title) }
        val removed = games.size - filtered.size
        if (removed > 0) Log.d(TAG, "Filtered out $removed non-game items (hex IDs)")
        Log.d(TAG, "Done — ${filtered.size} games loaded")
        return filtered
    }

    private fun isHexId(s: String): Boolean =
        s.length >= 20 && s.matches(Regex("^[0-9a-fA-F]+$"))

    override suspend fun logout() {
        tokenStorage.clearTokens(store.name)
    }

    private suspend fun fetchLibraryRecords(
        accessToken: String
    ): List<EpicLibraryRecord> {
        val allRecords = mutableListOf<EpicLibraryRecord>()
        var cursor: String? = null
        var page = 1

        do {
            Log.d(TAG, "Fetching library page $page...")
            val response: HttpResponse = httpClient.get(EpicConstants.LIBRARY_URL) {
                header("Authorization", "Bearer $accessToken")
                parameter("includeMetadata", true)
                if (cursor != null) {
                    parameter("cursor", cursor)
                }
            }

            if (!response.status.isSuccess()) {
                throw IllegalStateException("Failed to fetch library: ${response.status}")
            }

            val libraryResponse: EpicLibraryResponse = response.body()
            allRecords.addAll(libraryResponse.records)
            Log.d(TAG, "Page $page: ${libraryResponse.records.size} records (total: ${allRecords.size})")
            cursor = libraryResponse.responseMetadata?.nextCursor
            page++
        } while (cursor != null)

        return allRecords
    }

    private suspend fun fetchCatalogMetadataBatch(
        accessToken: String,
        namespace: String,
        catalogItemIds: List<String>
    ): Map<String, EpicCatalogResponse> {
        if (catalogItemIds.isEmpty()) return emptyMap()

        val hosts = listOf(
            EpicConstants.CATALOG_HOST,
            "catalog-public-service-prod05.ol.epicgames.com",
            "catalog-public-service-prod07.ol.epicgames.com"
        )

        for ((attempt, host) in hosts.withIndex()) {
            try {
                val url = "https://$host/catalog/api/shared/namespace/$namespace/bulk/items"
                val response: HttpResponse = httpClient.get(url) {
                    header("Authorization", "Bearer $accessToken")
                    catalogItemIds.forEach { parameter("id", it) }
                    parameter("includeDLCDetails", true)
                    parameter("includeMainGameDetails", true)
                    parameter("country", "US")
                    parameter("locale", "en-US")
                }

                if (!response.status.isSuccess()) {
                    Log.w(TAG, "catalog API HTTP ${response.status} on $host")
                    continue
                }

                val result = response.body<Map<String, EpicCatalogResponse>>()
                if (result.isNotEmpty()) {
                    Log.d(TAG, "catalog returned ${result.size} keys from $host")
                    return result
                }
            } catch (e: Exception) {
                Log.w(TAG, "catalog error on $host: ${e.message}")
            }
        }

        Log.e(TAG, "catalog metadata failed for namespace $namespace after ${hosts.size} hosts")
        return emptyMap()
    }

    private fun gameInfoFromRecord(record: EpicLibraryRecord): GameInfo {
        return GameInfo(
            store = Store.EPIC,
            appName = record.appName.ifBlank { record.catalogItemId ?: record.namespace },
            title = record.title.ifBlank { record.appName.ifBlank { record.catalogItemId ?: "Unknown" } },
            developer = null,
            description = null,
            artCover = null,
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

    private fun normalizeGameInfo(
        record: EpicLibraryRecord,
        metadata: EpicCatalogResponse
    ): GameInfo {
        val keyImages = metadata.keyImages?.associateBy { it.type } ?: emptyMap()
        val customAttrs = metadata.customAttributes ?: emptyMap()
        val canRunOffline = customAttrs["CanRunOffline"]?.value?.toBooleanStrictOrNull() ?: false

        val macReqs = metadata.technicalRequirements?.macos
        val isMacNative = macReqs != null && macReqs.isNotEmpty()

        val genres = metadata.categories?.mapNotNull { category ->
            category.path?.removePrefix("games/")?.removePrefix("editions/")
                ?.takeIf { it.isNotBlank() }
        }

        return GameInfo(
            store = Store.EPIC,
            appName = record.appName.ifBlank { record.catalogItemId ?: record.namespace },
            title = (metadata.title?.takeIf { it.isNotBlank() } ?: record.title).ifBlank { "Unknown" },
            developer = metadata.developer,
            description = metadata.description,
            artCover = keyImages["DieselGameBox"]?.url
                ?: keyImages["DieselGameBoxWide"]?.url,
            artSquare = keyImages["DieselGameBoxTall"]?.url,
            artLogo = keyImages["DieselGameBoxLogo"]?.url,
            artBackground = keyImages["DieselStoreFrontTall"]?.url,
            releaseDate = metadata.releaseDate,
            genres = genres?.takeIf { it.isNotEmpty() },
            canRunOffline = canRunOffline,
            storeUrl = null,
            isLinuxNative = false,
            isMacNative = isMacNative
        )
    }
}
