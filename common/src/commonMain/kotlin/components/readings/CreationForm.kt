package eu.bsinfo.components.readings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
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
import eu.bsinfo.components.*
import eu.bsinfo.components.customer.rememberCustomerCreationFormState
import eu.bsinfo.data.Client
import eu.bsinfo.data.Customer
import eu.bsinfo.data.Reading
import eu.bsinfo.rest.LocalClient
import eu.bsinfo.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlin.uuid.Uuid

@Composable
fun rememberReadingUpdateFormState(reading: Reading) = remember(reading.toString()) { ReadingCreationFormState(reading = reading) }

@Composable
fun rememberReadingCreationFormState(route: MainScreen.Readings? = null) =
    remember(route) { ReadingCreationFormState(route) }

class ReadingCreationFormState(route: MainScreen.Readings? = null, val reading: Reading? = null) {
    var comment by mutableStateOf(reading?.comment ?: "")
    var customer by mutableStateOf<Customer?>(reading?.customer ?: route?.createForCustomer)
    var customerIsError by mutableStateOf(false)
    var date by mutableStateOf<Instant>(
        reading?.date?.atStartOfDayIn(TimeZone.currentSystemDefault()) ?: Clock.System.now()
    )
    var dateIsError by mutableStateOf(false)
    var kind by mutableStateOf<Reading.Kind?>(reading?.kind)
    var kindIsError by mutableStateOf(false)
    var value by mutableStateOf<Double?>(reading?.meterCount)
    var valueIsError by mutableStateOf(false)
    var meterId by mutableStateOf<Int?>(reading?.meterId?.toIntOrNull())
    var meterIdIsError by mutableStateOf(false)
    var substitute by mutableStateOf(reading?.substitute ?: false)

    fun validate(): Boolean {
        customerIsError = customer == null && customer != reading?.customer // customer of reading might got deleted
        dateIsError = date > Clock.System.now()
        kindIsError = kind == null
        valueIsError = value == null
        meterIdIsError = meterId == null

        return !customerIsError && !dateIsError && !kindIsError && !valueIsError && !meterIdIsError
    }

    fun toReading() = Reading(
        reading?.id ?: Uuid.random(),
        comment,
        customer,
        date.toLocalDateTime(TimeZone.currentSystemDefault()).date,
        kind!!,
        value!!,
        meterId.toString(),
        substitute
    )
}

@Composable
fun ReadingCreationSheet(
    model: ReadingsScreenModel,
    route: MainScreen.Readings
) {
    val client = LocalClient.current
    val formState = rememberReadingCreationFormState(route)

    CreationSheet(model, "Ablesung erstellen", formState::validate, {
        client.createReading(formState.toReading())

        model.refresh()
        model.closeCreationForm()
    }) { loading ->
        ReadingCreationInput(model, route, formState, loading)
    }
}

@Composable
fun ReadingCreationForm(
    model: ReadingsScreenModel,
    state: ReadingCreationFormState,
    saveButtonIcon: @Composable () -> Unit = { Icon(Icons.Default.Save, "Save") },
    saveButtonText: @Composable () -> Unit,
    onSave: suspend CoroutineScope.() -> Unit
) {
    CreationForm(state::validate, onSave, saveButtonIcon, saveButtonText) { loading ->
        ReadingCreationInput(model, null, state, loading)
    }
}

@Composable
private fun ReadingCreationInput(
    model: ReadingsScreenModel,
    route: MainScreen.Readings?,
    formState: ReadingCreationFormState,
    loading: Boolean
) {
    LaunchedEffect(route) {
        if (route?.createForCustomer != null) {
            model.openCreationForm()
        }
    }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(25.dp, Alignment.CenterHorizontally),
        modifier = Modifier.fillMaxWidth()
    ) {
        Labeled("Zählertyp") {
            EnumInputField<Reading.Kind>(
                formState.kind, { formState.kind = it; formState.kindIsError = false },
                placeholder = { Text("Zählertyp") },
                enabled = !loading,
                isError = formState.kindIsError,
                modifier = Modifier.fillMaxWidth(.3f)
            )
        }
        Labeled("Zählernummer") {
            OutlinedTextField(
                formState.meterId?.toString() ?: "",
                {
                    formState.meterIdIsError = false
                    formState.meterId = if (it.isBlank()) null else it.toIntOrNull() ?: return@OutlinedTextField
                },
                isError = formState.meterIdIsError,
                placeholder = { Text("12345") },
                modifier = Modifier.fillMaxWidth(.3f),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            )
        }

        Labeled("Ersatzzähler", alignment = Alignment.CenterHorizontally) {
            Switch(formState.substitute, { formState.substitute = it })
        }
    }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(25.dp, Alignment.CenterHorizontally),
        modifier = Modifier.fillMaxWidth()
    ) {
        val context = LocalPlatformContext.current
        Labeled("Zählerstand") {
            OutlinedTextField(
                formState.value?.format(context) ?: "",
                {
                    formState.value =
                        if (it.isBlank()) null else parseDecimal(context, it)
                            ?: return@OutlinedTextField; formState.valueIsError =
                    false
                },
                isError = formState.valueIsError,
                placeholder = { Text(formatDecimal(context, 420.69)) },
                modifier = Modifier.fillMaxWidth(.2f),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
            )
        }
        Labeled("Ablesedatum") {
            DatePickerInputField(
                formState.date,
                { formState.date = it; formState.dateIsError = false },
                selectableDates = PastDates,
                modifier = Modifier.fillMaxWidth(.3f),
                isError = formState.dateIsError,
            )
        }

        Labeled("Kunde") {
            CustomerInputField(
                formState.customer,
                { formState.customer = it; formState.customerIsError = false },
                isError = formState.customerIsError,
                enabled = route?.createForCustomer == null && formState.reading == null,
                modifier = Modifier.fillMaxWidth(.3f)
            )
        }
    }

    Row {
        Labeled("Kommentar", modifier = Modifier.padding(horizontal = 25.dp)) {
            OutlinedTextField(
                formState.comment, { formState.comment = it },
                placeholder = { Text("Der Kunde hat den Zähler pink angemalt") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
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
