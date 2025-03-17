package eu.bsinfo.components.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.bsinfo.ReadingCard
import eu.bsinfo.ReadingsScreenModel
import eu.bsinfo.data.Reading
import eu.bsinfo.rest.LocalClient
import kotlin.uuid.Uuid

@Composable
fun ReadingList(forCustomer: Uuid) {
    var loading by remember(forCustomer) { mutableStateOf(true) }
    var readings by remember(forCustomer) { mutableStateOf(emptyList<Reading>()) }
    val client = LocalClient.current
    val model = viewModel { ReadingsScreenModel(client) }


    if (loading) {
        LaunchedEffect(forCustomer) {
            readings = client.getReadings(customerId = forCustomer).readings
            loading = false
        }
    } else {
        if (readings.isEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Text("Es gibt keine Ablesungen für diesen Kunden")
                CreateReadingButton {

                }
            }
        } else {
            LazyColumn {
                items(readings) {
                    ReadingCard(it, "", model)
                }

                item {
                    CreateReadingButton {  }
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
