package com.zachvlat.gamelibrary.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zachvlat.gamelibrary.library.model.GameInfo
import com.zachvlat.gamelibrary.library.model.Store

@Composable
fun HomeScreen(
    games: Map<Store, List<GameInfo>>
) {
    val totalGames = games.values.sumOf { it.size }
    val loggedInStores = games.filter { it.value.isNotEmpty() }.keys

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        Text(
            "GameLibrary",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(Modifier.height(8.dp))

        if (totalGames > 0) {
            Text(
                "$totalGames games across ${loggedInStores.size} store${if (loggedInStores.size != 1) "s" else ""}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StoreStat(
                    store = "GOG",
                    count = games[Store.GOG]?.size ?: 0,
                    icon = { Icon(Icons.Default.Star, null, Modifier.size(24.dp)) }
                )
                StoreStat(
                    store = "Epic",
                    count = games[Store.EPIC]?.size ?: 0,
                    icon = { Icon(Icons.Default.VideogameAsset, null, Modifier.size(24.dp)) }
                )
                StoreStat(
                    store = "Amazon",
                    count = games[Store.AMAZON]?.size ?: 0,
                    icon = { Icon(Icons.Default.Cloud, null, Modifier.size(24.dp)) }
                )
                StoreStat(
                    store = "Steam",
                    count = games[Store.STEAM]?.size ?: 0,
                    icon = { Icon(Icons.Default.VideogameAsset, null, Modifier.size(24.dp)) }
                )
            }
        } else {
            Text(
                "No games synced yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(32.dp))

        Text(
            "Ideas for this screen:",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        for (idea in listOf(
            "Recently played / added games",
            "Cross-store search",
            "Library statistics & charts",
            "Wishlist / backlog tracking",
            "Installation status per game",
            "News from each store"
        )) {
            Text(
                "\u2022 $idea",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StoreStat(
    store: String,
    count: Int,
    icon: @Composable () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        icon()
        Spacer(Modifier.height(4.dp))
        Text(store, style = MaterialTheme.typography.labelSmall)
        Text(
            "$count",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
