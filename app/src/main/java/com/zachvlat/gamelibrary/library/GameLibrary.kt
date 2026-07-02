package com.zachvlat.gamelibrary.library

import android.content.Context
import com.zachvlat.gamelibrary.library.auth.EncryptedTokenStorage
import com.zachvlat.gamelibrary.library.auth.TokenStorage
import com.zachvlat.gamelibrary.library.cache.AppDatabase
import com.zachvlat.gamelibrary.library.cache.LibraryCache
import com.zachvlat.gamelibrary.library.model.GameInfo
import com.zachvlat.gamelibrary.library.model.Store
import com.zachvlat.gamelibrary.library.store.StoreClient
import com.zachvlat.gamelibrary.library.store.amazon.AmazonStoreClient
import com.zachvlat.gamelibrary.library.store.epic.EpicStoreClient
import com.zachvlat.gamelibrary.library.store.gog.GogStoreClient
import com.zachvlat.gamelibrary.library.store.itch.ItchStoreClient
import com.zachvlat.gamelibrary.library.store.steam.SteamStoreClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class GameLibrary private constructor(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage,
    private var cache: LibraryCache,
    val epic: EpicStoreClient,
    val gog: GogStoreClient,
    val amazon: AmazonStoreClient,
    val steam: SteamStoreClient,
    val itch: ItchStoreClient
) {
    val allClients: Map<Store, StoreClient> = mapOf(
        Store.EPIC to epic,
        Store.GOG to gog,
        Store.AMAZON to amazon,
        Store.STEAM to steam,
        Store.ITCH to itch
    )

    fun getClient(store: Store): StoreClient = allClients[store]
        ?: throw IllegalArgumentException("Unknown store: $store")

    suspend fun getAllGames(forceRefresh: Boolean = false): Map<Store, List<GameInfo>> {
        if (!forceRefresh) {
            val cached = cache.getCachedAllGames()
            if (cached != null) return cached
        }

        val result = mutableMapOf<Store, List<GameInfo>>()
        for ((store, client) in allClients) {
            try {
                if (client.isLoggedIn()) {
                    val games = client.refreshLibrary()
                    cache.cacheGames(store, games)
                    result[store] = games
                }
            } catch (_: Exception) {
            }
        }
        return result
    }

    suspend fun getGamesForStore(
        store: Store,
        forceRefresh: Boolean = false
    ): List<GameInfo> {
        if (!forceRefresh) {
            val cached = cache.getCachedGames(store)
            if (cached != null) return cached
        }

        val client = getClient(store)
        val games = client.refreshLibrary()
        if (games.isNotEmpty()) {
            cache.cacheGames(store, games)
            return games
        }

        val cached = cache.getCachedGames(store)
        return cached ?: games
    }

    suspend fun isLoggedIn(store: Store): Boolean {
        return getClient(store).isLoggedIn()
    }

    suspend fun logoutAll() {
        for (client in allClients.values) {
            try {
                client.logout()
            } catch (_: Exception) { }
        }
        cache.invalidateAll()
    }

    suspend fun logout(store: Store) {
        getClient(store).logout()
        cache.invalidate(store)
    }

    fun destroy() {
        httpClient.close()
    }

    fun recreateCache(context: Context) {
        AppDatabase.closeAndClearInstance()
        val database = AppDatabase.getInstance(context)
        cache = LibraryCache(database)
    }

    companion object {
        fun create(context: Context): GameLibrary {
            val json = Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            }

            val httpClient = HttpClient {
                install(HttpTimeout) {
                    requestTimeoutMillis = 30_000
                    connectTimeoutMillis = 15_000
                    socketTimeoutMillis = 15_000
                }
                install(HttpRequestRetry) {
                    retryOnServerErrors(maxRetries = 3)
                    retryOnException(maxRetries = 3, retryOnTimeout = true)
                    exponentialDelay()
                }
                install(ContentNegotiation) {
                    json(json)
                }
                install(Logging) {
                    level = LogLevel.NONE
                }
            }

            val tokenStorage: TokenStorage = EncryptedTokenStorage(context)
            val database = AppDatabase.getInstance(context)
            val cache = LibraryCache(database)

            return GameLibrary(
                httpClient = httpClient,
                tokenStorage = tokenStorage,
                cache = cache,
                epic = EpicStoreClient(httpClient, tokenStorage),
                gog = GogStoreClient(httpClient, tokenStorage),
                amazon = AmazonStoreClient(httpClient, tokenStorage),
                steam = SteamStoreClient(httpClient, tokenStorage),
                itch = ItchStoreClient(httpClient, tokenStorage)
            )
        }
    }
}
