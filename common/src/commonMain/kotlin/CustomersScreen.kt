package eu.bsinfo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.bsinfo.components.DeleteDialog
import eu.bsinfo.components.EntityContainer
import eu.bsinfo.components.EntityViewModel
import eu.bsinfo.components.EntityViewState
import eu.bsinfo.components.customer.CustomerCreationForm
import eu.bsinfo.data.Customer
import eu.bsinfo.rest.Client
import eu.bsinfo.rest.LocalClient
import eu.bsinfo.util.formatLocalDate
import eu.bsinfo.util.matchingName
import eu.bsinfo.util.search
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

data class CustomersScreenState(
    val isLoading: Boolean = true,
    val customers: List<Customer> = emptyList(),
    override val query: String = "",
    override val creationFormVisible: Boolean = false
) : EntityViewState

class CustomersScreenModel(private val client: Client) : ViewModel(), EntityViewModel {
    private val _uiState = MutableStateFlow(CustomersScreenState())
    override val uiState = _uiState.asStateFlow()

    override fun setSearchQuery(text: String) {
        _uiState.tryEmit(_uiState.value.copy(query = text, customers = _uiState.value.customers.search(text)))
    }

    override suspend fun refresh() = withContext(Dispatchers.IO) {
        _uiState.emit(uiState.value.copy(customers = client.getCustomers().customers, isLoading = false))
    }

    suspend fun deleteCustomer(customerId: Uuid) = withContext(Dispatchers.IO) {
        client.deleteCustomer(customerId)
        _uiState.emit(uiState.value.copy(customers = uiState.value.customers.filter { it.id != customerId }))
    }

    override fun openCreationForm() {
        _uiState.tryEmit(uiState.value.copy(creationFormVisible = true))
    }

    override fun closeCreationForm() {
        _uiState.tryEmit(uiState.value.copy(creationFormVisible = false))
    }
}

@Composable
fun CustomersScreen(
    client: Client = LocalClient.current,
    model: CustomersScreenModel = viewModel { CustomersScreenModel(client) }
) {
    val state by model.uiState.collectAsState()

    if (state.isLoading) {
        LaunchedEffect(state) { model.refresh() }
    }

    EntityContainer(
        model,
        state.customers,
        client::createCustomer,
        addButtonIcon = { Icon(Icons.Default.Add, "Create") },
        addButtonText = { Text("Kunden erstellen") },
        searchPlaceholder = { Text("Suche nach Kundenname") }
    ) {
        CustomerCreationForm(model)
        LazyVerticalGrid(
            GridCells.Adaptive(260.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.customers) { customer ->
                CustomerCard(customer, state.query, model)
            }
        }
    }
}

@Composable
private fun CustomerCard(customer: Customer, query: String, model: CustomersScreenModel) {
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
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
                        tooltip = { RichTooltip { Text(customer.fullName) } },
                        state = rememberTooltipState()
                    ) {
                        Text(
                            customer.matchingName(query),
                            style = MaterialTheme.typography.headlineSmall,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                                .fillMaxWidth(fraction = .9f)
                        )
                    }
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
                        text = formatLocalDate(customer.birthDate)
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