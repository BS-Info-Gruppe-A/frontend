package eu.bsinfo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Expand
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import eu.bsinfo.data.Customer
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

@Composable
fun CustomersScreen() {
    LazyVerticalGrid(
        GridCells.Adaptive(250.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        items(customers) {
            CustomerCard(customer)
        }
    }
}

@Composable
private fun CustomerCard(customer: Customer) {
    ElevatedCard(
        modifier = Modifier
            .wrapContentHeight()
            .width(250.dp)
            .padding(vertical = 7.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(Modifier.padding(top = 3.dp, bottom = 10.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        customer.firstName + " " + customer.lastName,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                            .fillMaxWidth(fraction = .9f)
                    )
                    Spacer(Modifier.weight(1f))
                    CustomerDropDown()
                }
                Row(
                    horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth()
                ) {
                    CustomerDetail(
                        Icons.Filled.Person,
                        customer.gender.name
                    )
                    CustomerDetail(
                        icon = Icons.Filled.Cake,
                        text = customer.birthDate.toString()
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerDetail(icon: ImageVector, text: String) {
    Row(Modifier.padding(horizontal = 3.dp)) {
        Icon(
            imageVector = icon, contentDescription = null
        )
        Spacer(modifier = Modifier.padding(horizontal = 3.dp))
        Text(text)
    }
}

@Composable
private fun CustomerDropDown() {
    var expanded by remember { mutableStateOf(false) }

    Column(Modifier.padding(horizontal = 5.dp)) {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                { Text("Update") },
                {}
            )
            DropdownMenuItem(
                { Text("Delete") },
                {}
            )
        }
    }
}