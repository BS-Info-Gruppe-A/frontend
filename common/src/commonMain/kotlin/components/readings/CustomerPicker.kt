package eu.bsinfo.components.readings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.bsinfo.components.LoadingSpinner
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

    fun search(query: String) {
        _uiState.tryEmit(
            _uiState.value.copy(
                customers = uiState.value.customers?.search(query)
            )
        )
    }
}

@Composable
fun CustomerPicker(
    client: Client,
    search: String,
    onSelect: (Customer) -> Unit,
    model: CustomerPickerViewModel = viewModel { CustomerPickerViewModel(client) },
    searchBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by model.uiState.collectAsState()

    val currentState = state

    Column(modifier = modifier) {
        searchBar()
        if (currentState.customers == null) {
            LaunchedEffect(Unit) { model.fetchCustomers() }
            LoadingSpinner()
        } else {
            currentState.customers.forEach { customer ->
                SelectableRow({ onSelect(customer) }) {
                    Icon(Icons.Default.AccountCircle, "Customer")
                    Text(customer.matchingName(search))
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
                client, search, onSelect, model,
                {
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
                modifier.onFocusChanged { if (it.isFocused) scope.launch { sheetState.expand() } }
            )
        }
    }
}
