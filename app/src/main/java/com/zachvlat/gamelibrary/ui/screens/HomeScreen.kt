package com.zachvlat.gamelibrary.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.zachvlat.gamelibrary.R
import com.zachvlat.gamelibrary.library.GameLibrary
import com.zachvlat.gamelibrary.library.model.GameInfo
import com.zachvlat.gamelibrary.library.model.NowPlayingInfo
import com.zachvlat.gamelibrary.library.model.Store

private data class StoreEntry(
    val store: Store,
    val label: String,
    val iconRes: Int
)

private val stores = listOf(
    StoreEntry(Store.GOG, "GOG", R.drawable.ic_gog),
    StoreEntry(Store.EPIC, "Epic", R.drawable.ic_epic),
    StoreEntry(Store.AMAZON, "Amazon", R.drawable.ic_amazon),
    StoreEntry(Store.STEAM, "Steam", R.drawable.ic_steam),
    StoreEntry(Store.ITCH, "itch.io", R.drawable.ic_itch)
)

@Composable
fun HomeScreen(
    games: Map<Store, List<GameInfo>>,
    library: GameLibrary
) {
    val totalGames = games.values.sumOf { it.size }
    val loggedInStores = games.filter { it.value.isNotEmpty() }.keys
    val nowPlayingList = library.nowPlaying
    var editingNp by remember { mutableStateOf<NowPlayingInfo?>(null) }
    var editSliderValue by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        Text(
            "GameShelf",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(Modifier.height(8.dp))

        if (totalGames > 0) {
            Text(
                "$totalGames games across ${loggedInStores.size} store${if (loggedInStores.size != 1) "s" else ""}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                "No games synced yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (nowPlayingList.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text(
                "Now Playing",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))

            nowPlayingList.forEach { np ->
                val game = games[np.store]?.find { it.appName == np.appName }
                NowPlayingRow(
                    nowPlaying = np,
                    game = game,
                    onClick = {
                        editSliderValue = np.completionPercent / 100f
                        editingNp = np
                    }
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(stores, key = { it.store.name }) { entry ->
                StoreCard(
                    label = entry.label,
                    count = games[entry.store]?.size ?: 0,
                    iconRes = entry.iconRes
                )
            }
        }
    }

    val target = editingNp
    if (target != null) {
        val percent = (editSliderValue * 100).toInt()
        AlertDialog(
            onDismissRequest = { editingNp = null },
            title = { Text("Update Completion") },
            text = {
                Column {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "$percent%",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = editSliderValue,
                        onValueChange = { editSliderValue = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        library.updateNowPlaying(target.copy(completionPercent = percent))
                        editingNp = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingNp = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun NowPlayingRow(
    nowPlaying: NowPlayingInfo,
    game: GameInfo?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (game?.artCover != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(game.artCover)
                    .crossfade(true)
                    .build(),
                contentDescription = nowPlaying.appName,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = game?.title ?: nowPlaying.appName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { nowPlaying.completionPercent / 100f },
                modifier = Modifier.fillMaxWidth().height(4.dp),
            )
        }
    }
}

@Composable
private fun StoreCard(
    label: String,
    count: Int,
    iconRes: Int
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
