package eu.bsinfo.components.readings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.bsinfo.MainScreen
import eu.bsinfo.ReadingsScreenModel
import eu.bsinfo.components.CreationSheet
import eu.bsinfo.components.DatePickerInputField
import eu.bsinfo.components.EnumInputField
import eu.bsinfo.components.Labeled
import eu.bsinfo.components.customer.rememberCustomerCreationFormState
import eu.bsinfo.data.Customer
import eu.bsinfo.data.Reading
import eu.bsinfo.rest.Client
import eu.bsinfo.rest.LocalClient
import eu.bsinfo.util.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.uuid.Uuid

@Composable
fun ReadingCreationForm(model: ReadingsScreenModel, route: MainScreen.Readings) {
    val state by model.uiState.collectAsState()
    var comment by remember(state) { mutableStateOf("") }
    var customer by remember(state) { mutableStateOf<Customer?>(route.createForCustomer) }
    var customerIsError by remember(state) { mutableStateOf(false) }
    var date by remember(state) { mutableStateOf<Instant>(Clock.System.now()) }
    var dateIsError by remember(state) { mutableStateOf(false) }
    var kind by remember(state) { mutableStateOf<Reading.Kind?>(null) }
    var kindIsError by remember(state) { mutableStateOf(false) }
    var value by remember(state) { mutableStateOf<Double?>(null) }
    var valueIsError by remember(state) { mutableStateOf(false) }
    var meterId by remember(state) { mutableStateOf<Int?>(null) }
    var meterIdIsError by remember(state) { mutableStateOf(false) }
    var substitute by remember(state) { mutableStateOf(false) }
    val client = LocalClient.current

    LaunchedEffect(route) {
        if (route.createForCustomer != null) {
            model.openCreationForm()
        }
    }

    CreationSheet(model, "Ablesung erstellen", {
        customerIsError = customer == null
        dateIsError = date > Clock.System.now()
        kindIsError = kind == null
        valueIsError = value == null
        meterIdIsError = meterId == null

        !customerIsError && !dateIsError && !kindIsError && !valueIsError && !meterIdIsError
    }, {
        client.createReading(
            Reading(
                Uuid.random(),
                comment.ifBlank { null },
                customer,
                date.toLocalDateTime(TimeZone.currentSystemDefault()).date,
                kind!!,
                value!!,
                meterId.toString(),
                substitute
            )
        )

        model.refresh()
        model.closeCreationForm()
    }) { loading ->
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(25.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            Labeled("Zählertyp") {
                EnumInputField<Reading.Kind>(
                    kind, { kind = it; kindIsError = false },
                    placeholder = { Text("Zählertyp") },
                    enabled = !loading,
                    isError = kindIsError,
                    modifier = Modifier.fillMaxWidth(.3f)
                )
            }
            Labeled("Zählernummer") {
                OutlinedTextField(
                    meterId?.toString() ?: "",
                    {
                        meterIdIsError = false
                        meterId = if (it.isBlank()) null else it.toIntOrNull() ?: return@OutlinedTextField
                    },
                    isError = meterIdIsError,
                    placeholder = { Text("12345") },
                    modifier = Modifier.fillMaxWidth(.3f),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                )
            }

            Labeled("Ersatzzähler", alignment = Alignment.CenterHorizontally) {
                Switch(substitute, { substitute = it })
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(25.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            val context = LocalPlatformContext.current
            Labeled("Zählerstand") {
                OutlinedTextField(
                    value?.format(context) ?: "",
                    {
                        value =
                            if (it.isBlank()) null else parseDecimal(context, it) ?: return@OutlinedTextField; valueIsError =
                        false
                    },
                    isError = valueIsError,
                    placeholder = { Text(formatDecimal(context, 420.69)) },
                    modifier = Modifier.fillMaxWidth(.2f),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
                )
            }
            Labeled("Ablesedatum") {
                DatePickerInputField(
                    date,
                    { date = it; dateIsError = false },
                    selectableDates = PastDates,
                    modifier = Modifier.fillMaxWidth(.3f),
                    isError = dateIsError,
                )
            }

            Labeled("Kunde") {
                CustomerInputField(
                    customer, { customer = it; customerIsError = false }, isError = customerIsError,
                    enabled = route.createForCustomer == null,
                    modifier = Modifier.fillMaxWidth(.3f)
                )
            }
        }

        Row {
            Labeled("Kommentar", modifier = Modifier.padding(horizontal = 25.dp)) {
                OutlinedTextField(
                    comment, { comment = it },
                    placeholder = { Text("Der Kunde hat den Zähler pink angemalt") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        }
    }
}

@Composable
private fun CustomerInputField(
    current: Customer?, setCustomer: (Customer) -> Unit,
    client: Client = LocalClient.current,
    model: CustomerPickerViewModel = viewModel { CustomerPickerViewModel(client) },
    enabled: Boolean = true,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {

    var expanded by remember { mutableStateOf(false) }
    val state by model.uiState.collectAsState()

    OutlinedTextField(
        current?.fullName ?: "",
        { },
        placeholder = { Text("Tom Mot") },
        enabled = enabled,
        readOnly = true,
        isError = isError,
        singleLine = true,
        trailingIcon = {
            IconButton({ expanded = true }) { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        },
        modifier = modifier
    )

    if (expanded) {
        val creatorState = rememberCustomerCreationFormState(state)
        val scope = rememberCoroutineScope()
        var loading by remember { mutableStateOf(false) }

        DatePickerDialog(
            { expanded = false },
            dismissButton = {
                if (state.customerCreationVisible) {
                    TextButton({ model.closeCustomerCreator() }) { Text("Zurück") }
                }
            },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            confirmButton = {
                if (state.customerCreationVisible) {
                    TextButton({
                        creatorState.enabled = false
                        loading = true
                        scope.launch {
                            val customer = creatorState.toCustomer()
                            client.createCustomer(customer)
                            setCustomer(customer)
                            expanded = false
                            loading = false
                        }
                    }, enabled = creatorState.isValid) { Text("Weiter") }
                } else if (!loading) {
                    TextButton({ expanded = false }) { Text("Abbrechen") }
                } else {
                    CircularProgressIndicator()
                }
            }
        ) {
            var search by remember { mutableStateOf("") }

            CustomerPicker(
                LocalClient.current,
                search,
                { setCustomer(it); expanded = false },
                hasCreator = true,
                creationState = creatorState,
                searchBar = {
                    SearchBarDefaults.InputField(
                        search, { search = it; model.search(it) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                        onSearch = {}, expanded = false, onExpandedChange = {}, modifier = Modifier.fillMaxWidth()
                    )
                },
                model = model,
            )
        }
    }
}
