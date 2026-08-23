package com.example.aasra.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.aasra.auth.AuthManager
import com.example.aasra.ui.*
import com.example.aasra.ui.theme.AasraGreen
import com.example.aasra.ui.theme.AasraGreenDark
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.padding

object AasraDestinations {
    const val LOGIN = "login"

    // Citizen ("I need help") graph
    const val CITIZEN_HOME = "citizen_home"
    const val CITIZEN_LOCALITY = "citizen_locality"
    const val CITIZEN_RESCUE = "citizen_rescue"
    const val CITIZEN_SAFE = "citizen_safe"

    // Authority / Volunteer graph
    const val AUTHORITY_QUEUE = "authority_queue"
    const val AUTHORITY_UPDATE = "authority_update"
}

private val citizenRoutes = setOf(
    AasraDestinations.CITIZEN_HOME,
    AasraDestinations.CITIZEN_LOCALITY,
    AasraDestinations.CITIZEN_RESCUE,
    AasraDestinations.CITIZEN_SAFE
)

private val authorityRoutes = setOf(
    AasraDestinations.AUTHORITY_QUEUE,
    AasraDestinations.AUTHORITY_UPDATE
)

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authManager: AuthManager = remember { AuthManager() }
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val scope = rememberCoroutineScope()

    fun logout() {
        scope.launch {
            authManager.signOutAuthority()
            navController.navigate(AasraDestinations.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        bottomBar = {
            when (currentRoute) {
                in citizenRoutes -> CitizenBottomBar(currentRoute, navController, onLogout = ::logout)
                in authorityRoutes -> AuthorityBottomBar(currentRoute, navController, onLogout = ::logout)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AasraDestinations.LOGIN,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AasraDestinations.LOGIN) {
                LoginScreen(
                    onCitizenReady = {
                        navController.navigate(AasraDestinations.CITIZEN_HOME) {
                            popUpTo(AasraDestinations.LOGIN) { inclusive = true }
                        }
                    },
                    onAuthorityLoginSuccess = {
                        navController.navigate(AasraDestinations.AUTHORITY_QUEUE) {
                            popUpTo(AasraDestinations.LOGIN) { inclusive = true }
                        }
                    },
                    authManager = authManager
                )
            }

            // ---- Citizen graph ----
            composable(AasraDestinations.CITIZEN_HOME) {
                HomeScreen(
                    onFindShelter = { navController.navigate(AasraDestinations.CITIZEN_LOCALITY) },
                    onRequestRescue = { navController.navigate(AasraDestinations.CITIZEN_RESCUE) },
                    onMarkSafe = { navController.navigate(AasraDestinations.CITIZEN_SAFE) }
                )
            }
            composable(AasraDestinations.CITIZEN_LOCALITY) { LocalityScreen() }
            composable(AasraDestinations.CITIZEN_RESCUE) { RescueScreen() }
            composable(AasraDestinations.CITIZEN_SAFE) { SafeStatusScreen() }

            // ---- Authority graph ----
            composable(AasraDestinations.AUTHORITY_QUEUE) {
                AuthorityScreen(
                    onNotAuthorized = {
                        navController.navigate(AasraDestinations.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    authManager = authManager
                )
            }
            composable(AasraDestinations.AUTHORITY_UPDATE) {
                AuthorityShelterUpdateScreen(authManager = authManager)
            }
        }
    }
}

@Composable
private fun CitizenBottomBar(currentRoute: String?, navController: NavHostController, onLogout: () -> Unit) {
    NavigationBar(containerColor = AasraGreenDark, contentColor = Color.White) {
        val items = listOf(
            Triple(AasraDestinations.CITIZEN_HOME, "Home", Icons.Default.Home),
            Triple(AasraDestinations.CITIZEN_LOCALITY, "Locality", Icons.Default.Map),
            Triple(AasraDestinations.CITIZEN_RESCUE, "Rescue", Icons.Default.Warning),
            Triple(AasraDestinations.CITIZEN_SAFE, "I'm Safe", Icons.Default.CheckCircle)
        )
        items.forEach { (route, label, icon) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = {
                    navController.navigate(route) {
                        popUpTo(AasraDestinations.CITIZEN_HOME)
                        launchSingleTop = true
                    }
                },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, fontSize = 11.sp) },
                colors = navBarItemColors()
            )
        }
        NavigationBarItem(
            selected = false,
            onClick = onLogout,
            icon = { Icon(Icons.Default.Logout, contentDescription = "Exit") },
            label = { Text("Exit", fontSize = 11.sp) },
            colors = navBarItemColors()
        )
    }
}

@Composable
private fun AuthorityBottomBar(currentRoute: String?, navController: NavHostController, onLogout: () -> Unit) {
    NavigationBar(containerColor = AasraGreenDark, contentColor = Color.White) {
        val items = listOf(
            Triple(AasraDestinations.AUTHORITY_QUEUE, "Rescue Queue", Icons.Default.ListAlt),
            Triple(AasraDestinations.AUTHORITY_UPDATE, "Update Shelters", Icons.Default.Inventory)
        )
        items.forEach { (route, label, icon) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = {
                    navController.navigate(route) {
                        popUpTo(AasraDestinations.AUTHORITY_QUEUE)
                        launchSingleTop = true
                    }
                },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, fontSize = 11.sp) },
                colors = navBarItemColors()
            )
        }
        NavigationBarItem(
            selected = false,
            onClick = onLogout,
            icon = { Icon(Icons.Default.Logout, contentDescription = "Logout") },
            label = { Text("Logout", fontSize = 11.sp) },
            colors = navBarItemColors()
        )
    }
}

@Composable
private fun navBarItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Color.White,
    selectedTextColor = Color.White,
    unselectedIconColor = Color.White.copy(alpha = 0.6f),
    unselectedTextColor = Color.White.copy(alpha = 0.6f),
    indicatorColor = AasraGreen
)