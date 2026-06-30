package com.zachvlat.gamelibrary.library.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface TokenStorage {
    suspend fun saveToken(store: String, key: String, value: String)
    suspend fun getToken(store: String, key: String): String?
    suspend fun clearTokens(store: String)
    suspend fun clearAll()
}

class EncryptedTokenStorage(context: Context) : TokenStorage {

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "heroic_library_tokens",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override suspend fun saveToken(store: String, key: String, value: String) {
        withContext(Dispatchers.IO) {
            prefs.edit().putString("${store}_${key}", value).apply()
        }
    }

    override suspend fun getToken(store: String, key: String): String? {
        return withContext(Dispatchers.IO) {
            prefs.getString("${store}_${key}", null)
        }
    }

    override suspend fun clearTokens(store: String) {
        withContext(Dispatchers.IO) {
            val keysToRemove = prefs.all.keys.filter { it.startsWith("${store}_") }
            prefs.edit().apply {
                keysToRemove.forEach { remove(it) }
                apply()
            }
        }
    }

    override suspend fun clearAll() {
        withContext(Dispatchers.IO) {
            prefs.edit().clear().apply()
        }
    }
}
