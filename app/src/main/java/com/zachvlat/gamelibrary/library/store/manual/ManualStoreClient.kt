package com.zachvlat.gamelibrary.library.store.manual

import com.zachvlat.gamelibrary.library.cache.LibraryCache
import com.zachvlat.gamelibrary.library.model.GameInfo
import com.zachvlat.gamelibrary.library.model.LoginData
import com.zachvlat.gamelibrary.library.model.Store
import com.zachvlat.gamelibrary.library.store.StoreClient

class ManualStoreClient(
    private val cache: LibraryCache
) : StoreClient {

    override val store: Store get() = Store.MANUAL

    override suspend fun isLoggedIn(): Boolean = true

    override suspend fun getLoginData(): LoginData {
        return LoginData(url = "", state = emptyMap())
    }

    override suspend fun completeLogin(authCode: String): Boolean = false

    override suspend fun refreshLibrary(): List<GameInfo> {
        return cache.getCachedGames(Store.MANUAL, ttlMs = Long.MAX_VALUE) ?: emptyList()
    }

    override suspend fun logout() {
        // Manual games persist — do not delete on logout
    }
}
