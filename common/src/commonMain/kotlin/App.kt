package eu.bsinfo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GasMeter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import eu.bsinfo.data.Customer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

sealed interface MainScreen {
    @Serializable
    data object Customers : MainScreen

    @Serializable
    data class Readings(
        val createForCustomerRaw: String = "null",
    ) : MainScreen {
        constructor(customer: Customer?) : this(Json.encodeToString(customer))

        val createForCustomer: Customer?
            get() = Json.decodeFromString(createForCustomerRaw)
    }
}

val LocalNavController = staticCompositionLocalOf<NavHostController> { error("no default") }

@Composable
fun BSInfoApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()

    CompositionLocalProvider(LocalNavController provides navController) {
        AppTheme {
            Scaffold(
                bottomBar = {
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
                                { navController.navigate(MainScreen.Readings()) },
                                {
                                    Icon(Icons.Default.GasMeter, contentDescription = null)
                                },
                                label = { Text("Readings") })
                        }
                    }
                },
                modifier = Modifier.fillMaxSize(),
            ) { padding ->
                NavHost(
                    navController, startDestination = MainScreen.Customers,
                    modifier = Modifier.padding(padding)
                        .clickable(remember { MutableInteractionSource() }, indication = null) {}) {
                    composable<MainScreen.Customers> {
                        CustomersScreen()
                    }

                    composable<MainScreen.Readings> {
                        ReadingsScreen(it.toRoute())
                    }
                }
            }
        }
    }
}