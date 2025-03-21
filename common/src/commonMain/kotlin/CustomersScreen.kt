package eu.bsinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.bsinfo.components.*
import eu.bsinfo.components.customer.CustomerCreationFormState
import eu.bsinfo.components.customer.CustomerCreationSheet
import eu.bsinfo.components.customer.CustomerPopup
import eu.bsinfo.data.Client
import eu.bsinfo.data.Customer
import eu.bsinfo.rest.LocalClient
import eu.bsinfo.util.LocalPlatformContext
import eu.bsinfo.util.formatLocalDate
import eu.bsinfo.util.search
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

data class CustomersScreenState(
    override val loading: Boolean = true,
    val customers: List<Customer> = emptyList(),
    override val query: String = "",
    override val creationFormVisible: Boolean = false,
    val focusedCustomer: Customer? = null
) : EntityViewState

class CustomersScreenModel(private val client: Client) : ViewModel(), EntityViewModel<Customer> {
    private val _uiState = MutableStateFlow(CustomersScreenState())
    override val uiState = _uiState.asStateFlow()

    override fun setSearchQuery(text: String) {
        _uiState.tryEmit(_uiState.value.copy(query = text, customers = _uiState.value.customers.search(text)))
    }

    override suspend fun refresh() = withContext(Dispatchers.IO) {
        _uiState.emit(uiState.value.copy(customers = client.getCustomers().customers, loading = false))
    }

    override fun setLoading(loading: Boolean) {
        _uiState.tryEmit(_uiState.value.copy(loading = loading))
    }

    suspend fun deleteCustomer(customerId: Uuid) = withContext(Dispatchers.IO) {
        client.deleteCustomer(customerId)
        _uiState.emit(
            uiState.value.copy(
                customers = uiState.value.customers.filter { it.id != customerId },
                focusedCustomer = uiState.value.focusedCustomer?.takeIf { it.id != customerId })
        )
    }

    override fun focusEntity(entity: Customer) {
        _uiState.tryEmit(_uiState.value.copy(focusedCustomer = entity))
    }

    override fun unfocusEntity() {
        _uiState.tryEmit(_uiState.value.copy(focusedCustomer = null))
    }

    override fun openCreationForm() {
        _uiState.tryEmit(uiState.value.copy(creationFormVisible = true))
    }

    override fun closeCreationForm() {
        _uiState.tryEmit(uiState.value.copy(creationFormVisible = false))
    }

    suspend fun updateCustomer(client: Client, state: CustomerCreationFormState) = withContext(Dispatchers.IO) {
        val customer = state.toCustomer()

        client.updateCustomer(customer.toUpdatableCustomer())

        focusEntity(customer)
    }
}

@Composable
fun CustomersScreen(
    client: Client = LocalClient.current,
    model: CustomersScreenModel = viewModel { CustomersScreenModel(client) }
) {
    val state by model.uiState.collectAsState()

    if (state.loading) {
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
        CustomerCreationSheet(model)
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

    CustomerPopup(state.focusedCustomer, model)
}

@Composable
fun CustomerCard(customer: Customer, query: String, model: CustomersScreenModel) {
    val context = LocalPlatformContext.current
    EntityCard(customer, query, { model.focusEntity(customer) }, { RichTooltip { Text(customer.fullName) } }) {
        Row(horizontalArrangement = Arrangement.SpaceAround) {
            Detail(
                Icons.Filled.Person,
                customer.gender.humanName
            )
            Spacer(Modifier.weight(1f))
            Detail(
                icon = Icons.Filled.Cake,
                text = formatLocalDate(context, customer.birthDate)
            )
        }
    }
}
