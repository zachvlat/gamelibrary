package com.zachvlat.gamelibrary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.zachvlat.gamelibrary.library.GameLibrary
import com.zachvlat.gamelibrary.ui.navigation.AppNavigation
import com.zachvlat.gamelibrary.ui.theme.GameLibraryTheme

class MainActivity : ComponentActivity() {
    private var library: GameLibrary? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameLibraryTheme {
                val lib = remember { GameLibrary.create(applicationContext) }
                library = lib
                AppNavigation(library = lib)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        library?.destroy()
    }
}
