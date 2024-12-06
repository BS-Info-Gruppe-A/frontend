package eu.bsinfo

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GasMeter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*

@Composable
fun BSInfoApp() {
    var showCustomers by remember { mutableStateOf(true) }
    var showReadings by remember { mutableStateOf(false) }

    Scaffold(bottomBar = {
        BottomAppBar {
            NavigationBar {
                NavigationBarItem(showCustomers, {showCustomers = true; showReadings = false}, {
                    Icon(Icons.Default.Person, contentDescription = null)
                }, label = { Text("Customers") })
                NavigationBarItem(showReadings, {showReadings = true; showCustomers = false}, {
                    Icon(Icons.Default.GasMeter, contentDescription = null)
                }, label = { Text("Readings") })
            }
        }
    }) {
        if (showCustomers) {
            CustomersScreen()
        }
    }
}