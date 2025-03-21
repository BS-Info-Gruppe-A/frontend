package eu.bsinfo.components.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eu.bsinfo.CustomersScreenModel
import eu.bsinfo.LocalNavController
import eu.bsinfo.MainScreen
import eu.bsinfo.ReadingCard
import eu.bsinfo.data.Customer
import eu.bsinfo.data.Reading
import eu.bsinfo.rest.LocalClient

@Composable
fun ReadingList(customerModel: CustomersScreenModel,forCustomer: Customer) {
    var loading by remember(forCustomer) { mutableStateOf(true) }
    var readings by remember(forCustomer) { mutableStateOf(emptyList<Reading>()) }
    val client = LocalClient.current
    val navHost = LocalNavController.current

    fun createReading() {
        customerModel.unfocusEntity()
        navHost.navigate(MainScreen.Readings(customer = forCustomer))
    }

    if (loading) {
        LaunchedEffect(forCustomer) {
            readings = client.getReadings(customerId = forCustomer.id).readings
            loading = false
        }
    } else {
        if (readings.isEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Text("Es gibt keine Ablesungen für diesen Kunden", textAlign = TextAlign.Center)
                CreateReadingButton(::createReading)
            }
        } else {
            LazyColumn(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                items(readings) {
                    ReadingCard(it, modifier = Modifier.fillMaxWidth())
                }

                item {
                    CreateReadingButton(::createReading)
                }
            }
        }

    }
}

@Composable
fun CreateReadingButton(onClick: () -> Unit) {
    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
        Button(onClick) {
            Icon(Icons.Default.Add, "Create reading")
            Text("Neue Ablesung erstellen")
        }
    }
}
