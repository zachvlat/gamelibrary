package com.zachvlat.gamelibrary.library.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class AuthCallbackActivity : ComponentActivity() {

    companion object {
        const val EXTRA_AUTH_CODE = "auth_code"
        const val EXTRA_STORE = "store"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = intent.data
        val authCode = when {
            data != null -> data.getQueryParameter("code")
            intent.hasExtra(EXTRA_AUTH_CODE) -> intent.getStringExtra(EXTRA_AUTH_CODE)
            else -> null
        }

        val store = intent.getStringExtra(EXTRA_STORE)

        val resultIntent = Intent()
        authCode?.let { resultIntent.putExtra(EXTRA_AUTH_CODE, it) }
        store?.let { resultIntent.putExtra(EXTRA_STORE, it) }

        if (authCode != null) {
            setResult(RESULT_OK, resultIntent)
        } else {
            setResult(RESULT_CANCELED, resultIntent)
        }
        finish()
    }
}
