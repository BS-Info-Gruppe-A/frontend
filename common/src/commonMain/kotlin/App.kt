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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

enum class MainScreen {
    Customers, Readings
}

@Composable
fun BSInfoApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = MainScreen.valueOf(backStackEntry?.destination?.route ?: MainScreen.Customers.name)

    AppTheme {
        Scaffold(bottomBar = {
            BottomAppBar {
                NavigationBar {
                    NavigationBarItem(
                        currentScreen == MainScreen.Customers,
                        { navController.navigate(MainScreen.Customers.name) },
                        {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        label = { Text("Customers") })
                    NavigationBarItem(
                        currentScreen == MainScreen.Readings,
                        { navController.navigate(MainScreen.Readings.name) },
                        {
                            Icon(Icons.Default.GasMeter, contentDescription = null)
                        },
                        label = { Text("Readings") })
                }
            }
        }) {padding ->
            NavHost(navController, startDestination = MainScreen.Customers.name,
                modifier = Modifier.padding(padding).clickable(remember { MutableInteractionSource() }, indication = null) {}) {
                composable(route = MainScreen.Customers.name) {
                    CustomersScreen()
                }

                composable(route = MainScreen.Readings.name) {
                    ReadingsScreen()
                }
            }
        }
    }
}