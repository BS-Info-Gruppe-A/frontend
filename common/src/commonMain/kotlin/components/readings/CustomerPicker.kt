package eu.bsinfo.components.readings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.bsinfo.components.LoadingSpinner
import eu.bsinfo.components.customer.CustomerCreationFormState
import eu.bsinfo.components.customer.CustomerCreationInput
import eu.bsinfo.components.customer.rememberCustomerCreationFormState
import eu.bsinfo.data.Customer
import eu.bsinfo.rest.Client
import eu.bsinfo.util.focusable
import eu.bsinfo.util.matchingName
import eu.bsinfo.util.search
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class State(
    val customers: List<Customer>? = null,
    val customerCreationVisible: Boolean = false,
)

class CustomerPickerViewModel(private val client: Client) : ViewModel() {
    private val _uiState = MutableStateFlow(State())
    val uiState = _uiState.asStateFlow()

    suspend fun fetchCustomers() {
        _uiState.emit(
            _uiState.value.copy(
                customers = client.getCustomers().customers
            )
        )
    }

    fun openCustomerCreator() = _uiState.tryEmit(_uiState.value.copy(customerCreationVisible = true))
    fun closeCustomerCreator() = _uiState.tryEmit(_uiState.value.copy(customerCreationVisible = false))

    fun search(query: String) {
        _uiState.tryEmit(
            _uiState.value.copy(
                customers = uiState.value.customers?.search(query)
            )
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CustomerPicker(
    client: Client,
    search: String,
    onSelect: (Customer) -> Unit,
    hasCreator: Boolean = false,
    model: CustomerPickerViewModel = viewModel { CustomerPickerViewModel(client) },
    creationState: CustomerCreationFormState = rememberCustomerCreationFormState(),
    searchBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by model.uiState.collectAsState()
    val currentState = state

    Column(modifier = modifier) {
        SharedTransitionLayout {
            AnimatedContent(state.customerCreationVisible, modifier.fillMaxWidth()) { visible ->
                if (visible) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Kunden anlegen",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(15.dp).fillMaxWidth()
                        )
                        CustomerCreationInput(
                            state = creationState,
                            modifier = modifier.padding(vertical = 10.dp)
                        )
                    }
                } else {
                    Column(
                        Modifier
                            .sharedBounds(rememberSharedContentState("customer-picker"), this)
                            .fillMaxHeight(.55f)
                    ) {
                        searchBar()
                        if (currentState.customers == null) {
                            LaunchedEffect(Unit) { model.fetchCustomers() }
                            LoadingSpinner()
                        } else {
                            LazyColumn {
                                items(currentState.customers) { customer ->
                                    SelectableRow({ onSelect(customer) }) {
                                        Icon(Icons.Default.AccountCircle, "Customer")
                                        Text(customer.matchingName(search))
                                    }
                                }
                                if (hasCreator) {
                                    item("new-customer") {
                                        SelectableRow({ model.openCustomerCreator() }) {
                                            Icon(Icons.Default.Add, "Create customer")
                                            Text("Neuen Kunden anlegen")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerPickerSheet(
    client: Client,
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    onSelect: (Customer) -> Unit,
    model: CustomerPickerViewModel = viewModel { CustomerPickerViewModel(client) },
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var search by remember { mutableStateOf("") }

    if (isVisible) {
        Sheet("Kunde", onDismissRequest, state = sheetState, modifier = modifier.fillMaxHeight(.8f).focusable()) {
            CustomerPicker(
                client = client, search = search, onSelect = onSelect, model = model,
                searchBar = {
                    DockedSearchBar(
                        inputField = {
                            SearchBarDefaults.InputField(
                                search,
                                { search = it; model.search(it) },
                                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                                onSearch = {},
                                expanded = false,
                                onExpandedChange = {},
                                modifier = Modifier.fillMaxWidth()
                            )
                        }, expanded = false, onExpandedChange = {},
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp)
                    ) {}
                },
                modifier = modifier.onFocusChanged { if (it.isFocused) scope.launch { sheetState.expand() } }
            )
        }
    }
}
