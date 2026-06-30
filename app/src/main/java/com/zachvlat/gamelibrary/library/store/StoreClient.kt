package com.zachvlat.gamelibrary.library.store

import com.zachvlat.gamelibrary.library.model.GameInfo
import com.zachvlat.gamelibrary.library.model.LoginData
import com.zachvlat.gamelibrary.library.model.Store

interface StoreClient {
    val store: Store

    suspend fun isLoggedIn(): Boolean

    suspend fun getLoginData(): LoginData

    suspend fun completeLogin(authCode: String): Boolean

    suspend fun refreshLibrary(): List<GameInfo>

    suspend fun logout()
}
