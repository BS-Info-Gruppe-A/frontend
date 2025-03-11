package eu.bsinfo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GasMeter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable

sealed interface MainScreen {
    @Serializable
    data object Customers : MainScreen
    @Serializable
    data object Readings : MainScreen
}

@Composable
fun BSInfoApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()

    AppTheme {
        Scaffold(bottomBar = {
            BottomAppBar {
                NavigationBar {
                    NavigationBarItem(
                        backStackEntry?.destination?.hasRoute<MainScreen.Customers>() == true,
                        { navController.navigate(MainScreen.Customers) },
                        {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        label = { Text("Customers") })
                    NavigationBarItem(
                        backStackEntry?.destination?.hasRoute<MainScreen.Readings>() == true,
                        { navController.navigate(MainScreen.Readings) },
                        {
                            Icon(Icons.Default.GasMeter, contentDescription = null)
                        },
                        label = { Text("Readings") })
                }
            }
        }) {padding ->
            NavHost(navController, startDestination = MainScreen.Customers,
                modifier = Modifier.padding(padding).clickable(remember { MutableInteractionSource() }, indication = null) {}) {
                composable<MainScreen.Customers> {
                    CustomersScreen()
                }

                composable<MainScreen.Readings> {
                    ReadingsScreen()
                }
            }
        }
    }
}