package com.zachvlat.gamelibrary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zachvlat.gamelibrary.library.GameLibrary
import com.zachvlat.gamelibrary.library.model.GameInfo
import com.zachvlat.gamelibrary.library.model.Store
import com.zachvlat.gamelibrary.library.ui.LoginWebViewDialog
import kotlinx.coroutines.launch

private enum class SortOption {
    ALPHA_ASC,
    ALPHA_DESC,
    RELEASE_DESC,
    RELEASE_ASC
}

@Composable
fun StoreScreen(
    store: Store,
    library: GameLibrary,
    games: List<GameInfo>,
    onGamesUpdated: (List<GameInfo>) -> Unit,
    onGameClick: (GameInfo) -> Unit = {}
) {
    var isLoggedIn by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isSyncing by remember { mutableStateOf(false) }
    var loginUrl by remember { mutableStateOf("") }
    var showLoginDialog by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var sortOption by remember { mutableStateOf(SortOption.ALPHA_ASC) }
    var showSortMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val displayedGames = remember(games, sortOption) {
        val sorted = games.toMutableList()
        when (sortOption) {
            SortOption.ALPHA_ASC -> sorted.sortBy { it.title.lowercase() }
            SortOption.ALPHA_DESC -> sorted.sortByDescending { it.title.lowercase() }
            SortOption.RELEASE_DESC -> sorted.sortByDescending { it.releaseDate }
            SortOption.RELEASE_ASC -> sorted.sortBy { it.releaseDate }
        }
        sorted
    }

    LaunchedEffect(store) {
        isLoggedIn = library.isLoggedIn(store)
        if (isLoggedIn && games.isEmpty()) {
            try {
                val cached = library.getGamesForStore(store, forceRefresh = false)
                if (cached.isNotEmpty()) onGamesUpdated(cached)
            } catch (_: Exception) { }
        }
        isLoading = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            !isLoggedIn && games.isEmpty() -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Not connected to ${store.name}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    FilledTonalButton(onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                val data = library.getClient(store).getLoginData()
                                loginUrl = data.url
                                showLoginDialog = true
                            } catch (e: Exception) {
                                statusMessage = "Error: ${e.message}"
                            }
                            isLoading = false
                        }
                    }) {
                        Text("Login with ${store.name}")
                    }
                    val msg = statusMessage
                    if (msg != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(msg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item(span = { GridItemSpan(3) }) {
                        FilledTonalButton(
                            onClick = {
                                scope.launch {
                                    isSyncing = true
                                    statusMessage = null
                                    try {
                                        val result = library.getGamesForStore(store, forceRefresh = true)
                                        onGamesUpdated(result)
                                        statusMessage = "${result.size} games"
                                    } catch (e: Exception) {
                                        statusMessage = "Sync failed: ${e.message}"
                                    }
                                    isSyncing = false
                                }
                            },
                            enabled = !isSyncing,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.height(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Sync new games")
                            }
                        }
                        val msg = statusMessage
                        if (msg != null) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                msg,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    items(displayedGames, key = { it.appName + it.store.name }) { game ->
                        GameCard(game = game, onClick = { onGameClick(game) })
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    FloatingActionButton(
                        onClick = { showSortMenu = !showSortMenu }
                    ) {
                        Icon(Icons.Default.Sort, "Sort")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Name A-Z") },
                            onClick = { sortOption = SortOption.ALPHA_ASC; showSortMenu = false },
                            leadingIcon = if (sortOption == SortOption.ALPHA_ASC) {
                                { Icon(Icons.Default.Check, null) }
                            } else null
                        )
                        DropdownMenuItem(
                            text = { Text("Name Z-A") },
                            onClick = { sortOption = SortOption.ALPHA_DESC; showSortMenu = false },
                            leadingIcon = if (sortOption == SortOption.ALPHA_DESC) {
                                { Icon(Icons.Default.Check, null) }
                            } else null
                        )
                        DropdownMenuItem(
                            text = { Text("Release newest") },
                            onClick = { sortOption = SortOption.RELEASE_DESC; showSortMenu = false },
                            leadingIcon = if (sortOption == SortOption.RELEASE_DESC) {
                                { Icon(Icons.Default.Check, null) }
                            } else null
                        )
                        DropdownMenuItem(
                            text = { Text("Release oldest") },
                            onClick = { sortOption = SortOption.RELEASE_ASC; showSortMenu = false },
                            leadingIcon = if (sortOption == SortOption.RELEASE_ASC) {
                                { Icon(Icons.Default.Check, null) }
                            } else null
                        )
                    }
                }
            }
        }
    }

    if (showLoginDialog && loginUrl.isNotEmpty()) {
        LoginWebViewDialog(
            store = store,
            authUrl = loginUrl,
            onDismiss = {
                showLoginDialog = false
                loginUrl = ""
            },
            onCodeReceived = { code ->
                showLoginDialog = false
                loginUrl = ""
                scope.launch {
                    isLoading = true
                    try {
                        val ok = library.getClient(store).completeLogin(code)
                        if (ok) {
                            isLoggedIn = true
                            val result = library.getGamesForStore(store, forceRefresh = true)
                            onGamesUpdated(result)
                            statusMessage = "${result.size} games loaded"
                        } else {
                            statusMessage = "Login failed"
                        }
                    } catch (e: Exception) {
                        statusMessage = "Error: ${e.message}"
                    }
                    isLoading = false
                }
            }
        )
    }
}

@Composable
private fun GameCard(game: GameInfo, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (game.artCover != null) {
                    AsyncImage(
                        model = game.artCover,
                        contentDescription = game.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Text(
                text = game.title,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
}
