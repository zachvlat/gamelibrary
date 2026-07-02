package com.zachvlat.gamelibrary.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import com.zachvlat.gamelibrary.R
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zachvlat.gamelibrary.library.GameLibrary
import com.zachvlat.gamelibrary.library.model.GameInfo
import com.zachvlat.gamelibrary.library.model.Store
import com.zachvlat.gamelibrary.ui.screens.GameScreen
import com.zachvlat.gamelibrary.ui.screens.HomeScreen
import com.zachvlat.gamelibrary.ui.screens.SettingsScreen
import com.zachvlat.gamelibrary.ui.screens.StoreScreen
import kotlinx.coroutines.launch

private sealed class DrawerItem(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit
) {
    data object Home : DrawerItem("home", "Home", { Icon(Icons.Default.Home, null) })
    data object Gog : DrawerItem("gog", "GOG", { Icon(painterResource(R.drawable.ic_gog), null, Modifier.size(24.dp)) })
    data object Epic : DrawerItem("epic", "Epic", { Icon(painterResource(R.drawable.ic_epic), null, Modifier.size(24.dp)) })
    data object Amazon : DrawerItem("amazon", "Amazon", { Icon(painterResource(R.drawable.ic_amazon), null, Modifier.size(24.dp)) })
    data object Steam : DrawerItem("steam", "Steam", { Icon(painterResource(R.drawable.ic_steam), null, Modifier.size(24.dp)) })
    data object Itch : DrawerItem("itch", "itch.io", { Icon(painterResource(R.drawable.ic_itch), null, Modifier.size(24.dp)) })
    data object Settings : DrawerItem("settings", "Settings", { Icon(Icons.Default.Settings, null) })
}

private val drawerItems = listOf(
    DrawerItem.Home,
    DrawerItem.Gog,
    DrawerItem.Epic,
    DrawerItem.Amazon,
    DrawerItem.Steam,
    DrawerItem.Itch
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(library: GameLibrary) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    var gogGames by remember { mutableStateOf<List<GameInfo>>(emptyList()) }
    var epicGames by remember { mutableStateOf<List<GameInfo>>(emptyList()) }
    var amazonGames by remember { mutableStateOf<List<GameInfo>>(emptyList()) }
    var steamGames by remember { mutableStateOf<List<GameInfo>>(emptyList()) }
    var itchGames by remember { mutableStateOf<List<GameInfo>>(emptyList()) }
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(refreshKey) {
        for (store in Store.entries) {
            try {
                val cached = library.getGamesForStore(store, forceRefresh = false)
                if (cached.isNotEmpty()) {
                    when (store) {
                        Store.GOG -> gogGames = cached
                        Store.EPIC -> epicGames = cached
                        Store.AMAZON -> amazonGames = cached
                        Store.STEAM -> steamGames = cached
                        Store.ITCH -> itchGames = cached
                    }
                }
            } catch (_: Exception) { }
        }
    }

    val allGames = mapOf(
        Store.GOG to gogGames,
        Store.EPIC to epicGames,
        Store.AMAZON to amazonGames,
        Store.STEAM to steamGames,
        Store.ITCH to itchGames
    )

    val isGameDetail = currentRoute.startsWith("game/")
    val title = when {
        isGameDetail -> "Game Details"
        currentRoute == "home" -> "GameShelf"
        currentRoute == "gog" -> "GOG"
        currentRoute == "epic" -> "Epic Games"
        currentRoute == "amazon" -> "Amazon Gaming"
        currentRoute == "steam" -> "Steam"
        currentRoute == "itch" -> "itch.io"
        currentRoute == "settings" -> "Settings"
        else -> "GameShelf"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.fillMaxHeight()) {
                    Text(
                        "GameShelf",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                    drawerItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationDrawerItem(
                            icon = item.icon,
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                scope.launch { drawerState.close() }
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo("home") { inclusive = false }
                                        launchSingleTop = true
                                    }
                                }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    NavigationDrawerItem(
                        icon = DrawerItem.Settings.icon,
                        label = { Text(DrawerItem.Settings.label) },
                        selected = currentRoute == DrawerItem.Settings.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (currentRoute != DrawerItem.Settings.route) {
                                navController.navigate(DrawerItem.Settings.route) {
                                    popUpTo("home") { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        if (isGameDetail) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = "Menu"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { padding ->
            val onGameClick: (GameInfo) -> Unit = { game ->
                navController.navigate("game/${game.store.name}/${Uri.encode(game.appName)}")
            }

            NavHost(
                navController = navController,
                startDestination = DrawerItem.Home.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(DrawerItem.Home.route) {
                    HomeScreen(games = allGames)
                }
                composable(DrawerItem.Gog.route) {
                    StoreScreen(
                        store = Store.GOG,
                        library = library,
                        games = gogGames,
                        onGamesUpdated = { gogGames = it },
                        onGameClick = onGameClick
                    )
                }
                composable(DrawerItem.Epic.route) {
                    StoreScreen(
                        store = Store.EPIC,
                        library = library,
                        games = epicGames,
                        onGamesUpdated = { epicGames = it },
                        onGameClick = onGameClick
                    )
                }
                composable(DrawerItem.Amazon.route) {
                    StoreScreen(
                        store = Store.AMAZON,
                        library = library,
                        games = amazonGames,
                        onGamesUpdated = { amazonGames = it },
                        onGameClick = onGameClick
                    )
                }
                composable(DrawerItem.Steam.route) {
                    StoreScreen(
                        store = Store.STEAM,
                        library = library,
                        games = steamGames,
                        onGamesUpdated = { steamGames = it },
                        onGameClick = onGameClick
                    )
                }
                composable(DrawerItem.Itch.route) {
                    StoreScreen(
                        store = Store.ITCH,
                        library = library,
                        games = itchGames,
                        onGamesUpdated = { itchGames = it },
                        onGameClick = onGameClick
                    )
                }
                composable(DrawerItem.Settings.route) {
                    SettingsScreen(
                        library = library,
                        onDatabaseImported = { refreshKey++ }
                    )
                }
                composable(
                    route = "game/{store}/{appName}",
                    arguments = listOf(
                        navArgument("store") { type = NavType.StringType },
                        navArgument("appName") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val storeName = backStackEntry.arguments?.getString("store") ?: return@composable
                    val appName = Uri.decode(backStackEntry.arguments?.getString("appName") ?: return@composable)
                    val store = try { Store.valueOf(storeName.uppercase()) } catch (_: Exception) { return@composable }
                    val game = allGames[store]?.find { it.appName == appName }
                    if (game != null) {
                        GameScreen(game = game)
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Game not found")
                        }
                    }
                }
            }
        }
    }
}
