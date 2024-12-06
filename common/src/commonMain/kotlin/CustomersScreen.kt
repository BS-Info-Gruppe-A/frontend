package eu.bsinfo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.bsinfo.components.DeleteDialog
import eu.bsinfo.data.Customer
import eu.bsinfo.data.readableFormat
import eu.bsinfo.rest.Client
import eu.bsinfo.rest.LocalClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

data class CustomersScreenState(
    val isLoading: Boolean = true,
    val customers: List<Customer> = emptyList(),
)

class CustomersScreenModel(private val client: Client) : ViewModel() {
    private val _uiState = MutableStateFlow(CustomersScreenState())
    val uiState = _uiState.asStateFlow()

    suspend fun refreshCustomers() = withContext(Dispatchers.IO) {
        _uiState.emit(uiState.value.copy(customers = client.getCustomers().customers, isLoading = false))
    }

    suspend fun deleteCustomer(customerId: Uuid) = withContext(Dispatchers.IO) {
        client.deleteCustomer(customerId)
        _uiState.emit(uiState.value.copy(customers = uiState.value.customers.filter { it.id != customerId }))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    client: Client = LocalClient.current,
    model: CustomersScreenModel = viewModel { CustomersScreenModel(client) }
) {
    val state by model.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    if (state.isLoading) {
        LaunchedEffect(state) { model.refreshCustomers() }
    }

    PullToRefreshBox(
        state.isLoading,
        { scope.launch { model.refreshCustomers() } },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyVerticalGrid(
            GridCells.Adaptive(260.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.customers) { customer ->
                CustomerCard(customer, model)
            }
        }
    }
}

@Composable
private fun CustomerCard(customer: Customer, model: CustomersScreenModel) {
    ElevatedCard(
        modifier = Modifier
            .width(260.dp)
            .height(100.dp)
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
                        customer.fullName,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                            .fillMaxWidth(fraction = .9f)
                    )
                    Spacer(Modifier.weight(1f))
                    CustomerDropDown(customer, model)
                }
                Row(
                    horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth()
                ) {
                    CustomerDetail(
                        Icons.Filled.Person,
                        customer.gender.humanName
                    )
                    CustomerDetail(
                        icon = Icons.Filled.Cake,
                        text = readableFormat.format(customer.birthDate)
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
private fun CustomerDropDown(customer: Customer, model: CustomersScreenModel) {
    var expanded by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    if (isDeleting) {
        DeleteDialog(
            isDeleting,
            { isDeleting = false },
            { model.deleteCustomer(customer.id) },
            title = { Text("Kunden löschen?", color = MaterialTheme.colorScheme.onError) },
            text = { Text("Möchten Sie den Kunden wirklich löschen?", color = MaterialTheme.colorScheme.onError) },
        )
    }

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
                { expanded = false; isDeleting = true }
            )
        }
    }
}