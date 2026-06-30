package com.zachvlat.gamelibrary.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideogameAsset
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
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
import com.zachvlat.gamelibrary.ui.screens.StoreScreen
import kotlinx.coroutines.launch

private sealed class DrawerItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home : DrawerItem("home", "Home", Icons.Default.Home)
    data object Gog : DrawerItem("gog", "GOG", Icons.Default.Star)
    data object Epic : DrawerItem("epic", "Epic", Icons.Default.Extension)
    data object Amazon : DrawerItem("amazon", "Amazon", Icons.Default.Cloud)
    data object Steam : DrawerItem("steam", "Steam", Icons.Default.VideogameAsset)
}

private val drawerItems = listOf(
    DrawerItem.Home,
    DrawerItem.Gog,
    DrawerItem.Epic,
    DrawerItem.Amazon,
    DrawerItem.Steam
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

    LaunchedEffect(Unit) {
        for (store in Store.entries) {
            try {
                val cached = library.getGamesForStore(store, forceRefresh = false)
                if (cached.isNotEmpty()) {
                    when (store) {
                        Store.GOG -> gogGames = cached
                        Store.EPIC -> epicGames = cached
                        Store.AMAZON -> amazonGames = cached
                        Store.STEAM -> steamGames = cached
                    }
                }
            } catch (_: Exception) { }
        }
    }

    val allGames = mapOf(
        Store.GOG to gogGames,
        Store.EPIC to epicGames,
        Store.AMAZON to amazonGames,
        Store.STEAM to steamGames
    )

    val isGameDetail = currentRoute.startsWith("game/")
    val title = when {
        isGameDetail -> "Game Details"
        currentRoute == "home" -> "GameLibrary"
        currentRoute == "gog" -> "GOG"
        currentRoute == "epic" -> "Epic Games"
        currentRoute == "amazon" -> "Amazon Gaming"
        currentRoute == "steam" -> "Steam"
        else -> "GameLibrary"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "GameLibrary",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                drawerItems.forEach { item ->
                    val selected = currentRoute == item.route
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, item.label) },
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
