package eu.bsinfo

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.bsinfo.components.DeleteDialog
import eu.bsinfo.components.EntityContainer
import eu.bsinfo.components.EntityViewModel
import eu.bsinfo.components.EntityViewState
import eu.bsinfo.components.readings.CustomerPicker
import eu.bsinfo.components.readings.KindPicker
import eu.bsinfo.components.readings.ReadingDatePicker
import eu.bsinfo.data.Customer
import eu.bsinfo.data.Reading
import eu.bsinfo.rest.Client
import eu.bsinfo.rest.LocalClient
import eu.bsinfo.util.formatLocalDate
import eu.bsinfo.util.matching
import eu.bsinfo.util.search
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.uuid.Uuid

data class ReadingsScreenState(
    val isLoading: Boolean = true,
    val isDatePickerDialogVisible: Boolean = false,
    val isKindSheetVisible: Boolean = false,
    val isCustomerSheetVisible: Boolean = false,
    val selectedStartDate: LocalDate? = null,
    val selectedEndDate: LocalDate? = null,
    val selectedKind: Reading.Kind? = null,
    val selectedCustomer: Customer? = null,
    override val query: String = "",
    val readings: List<Reading> = emptyList(),
) : EntityViewState {
    val dateRangeFormatted: String?
        get() {
            val startDate = selectedStartDate
            val endDate = selectedEndDate
            if (startDate == null) return null

            return buildString {
                append(formatLocalDate(startDate))
                if (endDate != null) {
                    append(" - ")
                    append(formatLocalDate(endDate))
                }
            }
        }
}

class ReadingsScreenModel(val client: Client) : ViewModel(), EntityViewModel {
    private val _uiState = MutableStateFlow(ReadingsScreenState())
    override val uiState = _uiState.asStateFlow()

    fun openDateSheet() = _uiState.tryEmit(uiState.value.copy(isDatePickerDialogVisible = true))
    fun closeDateSheet() = _uiState.tryEmit(uiState.value.copy(isDatePickerDialogVisible = false))
    fun openKindPickerSheet() = _uiState.tryEmit(uiState.value.copy(isKindSheetVisible = true))
    fun closeKindPickerSheet() = _uiState.tryEmit(uiState.value.copy(isKindSheetVisible = false))
    fun openCustomerSheet() = _uiState.tryEmit(uiState.value.copy(isCustomerSheetVisible = true))
    fun closeCustomerSheet() = _uiState.tryEmit(uiState.value.copy(isCustomerSheetVisible = false))

    override fun setSearchQuery(text: String) {
        _uiState.tryEmit(_uiState.value.copy(query = text, readings = _uiState.value.readings.search(text)))
    }

    suspend fun setDateRange(from: Long?, to: Long?) {
        _uiState.tryEmit(
            uiState.value.copy(
                selectedStartDate = from?.toLocalDate(), selectedEndDate = to?.toLocalDate(),
                isDatePickerDialogVisible = false, isLoading = true
            )
        )

        refresh()
    }

    suspend fun setKind(kind: Reading.Kind?) {
        _uiState.tryEmit(
            uiState.value.copy(
                selectedKind = kind, isKindSheetVisible = false, isLoading = true
            )
        )

        refresh()
    }

    suspend fun setCustomer(customer: Customer?) {
        _uiState.tryEmit(
            uiState.value.copy(
                selectedCustomer = customer, isCustomerSheetVisible = false, isLoading = true
            )
        )

        refresh()
    }

    override suspend fun refresh() = withContext(Dispatchers.IO) {
        val state = _uiState.value
        _uiState.emit(
            state.copy(
                readings = client.getReadings(
                    from = state.selectedStartDate, to = state.selectedEndDate, kind = state.selectedKind,
                    customerId = state.selectedCustomer?.id
                ).readings, isLoading = false
            )
        )
    }

    suspend fun deleteReading(readingId: Uuid) = withContext(Dispatchers.IO) {
        client.deleteReading(readingId)
        _uiState.emit(uiState.value.copy(readings = uiState.value.readings.filter { it.id != readingId }))
    }

    private fun Long.toLocalDate(): LocalDate {
        val dateTime = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault())
        return LocalDate(dateTime.year, dateTime.month, dateTime.dayOfMonth)
    }
}

@Composable
fun ReadingsScreen(
    client: Client = LocalClient.current,
    model: ReadingsScreenModel = viewModel { ReadingsScreenModel(client) }
) {
    val state by model.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    if (state.isLoading) {
        LaunchedEffect(state) { model.refresh() }
    }

    EntityContainer(
        model,
        state.readings,
        addButtonIcon = { Icon(Icons.Default.Add, "Create") },
        addButtonText = { Text("Ablesung erstellen") },
        searchPlaceholder = { Text("Suche nach meter id") }
    ) {
        Filters(model, modifier = Modifier.padding(vertical = 5.dp))
        LazyVerticalGrid(
            GridCells.Adaptive(260.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.readings) { reading ->
                ReadingCard(reading, state.query, model)
            }
        }
    }

    ReadingDatePicker(state, model)
    KindPicker(state, model)
    CustomerPicker(client, state.isCustomerSheetVisible, { model.closeCustomerSheet() }, {
        scope.launch { model.setCustomer(it) }
    })
}

@Composable
private fun Filters(model: ReadingsScreenModel, modifier: Modifier = Modifier) {
    val uiState by model.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    Row(
        horizontalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterHorizontally),
        modifier = modifier.fillMaxWidth().padding(horizontal = 10.dp).horizontalScroll(rememberScrollState())
    ) {
        Filter(
            onClick = { model.openDateSheet() },
            onDismiss = { scope.launch { model.setDateRange(null, null) } },
            label = {
                val range = uiState.dateRangeFormatted ?: "Datum"
                Text(range)
            },
            enabled = uiState.selectedStartDate != null,
            leadingIcon = { Icon(Icons.Default.DateRange, "Date") }
        )
        Filter(
            onClick = { model.openKindPickerSheet() },
            onDismiss = { scope.launch { model.setKind(null) } },
            label = { Text(uiState.selectedKind?.readableName ?: "Ablesungsart") },
            enabled = uiState.selectedKind != null,
            leadingIcon = { Icon(Icons.Default.ElectricMeter, "Kind") }
        )

        Filter(
            onClick = { model.openCustomerSheet() },
            onDismiss = { scope.launch { model.setCustomer(null) } },
            label = { Text(uiState.selectedCustomer?.fullName ?: "Kunde") },
            enabled = uiState.selectedCustomer != null,
            leadingIcon = { Icon(Icons.Default.AccountBox, "Account") }
        )
    }
}

@Composable
fun Filter(
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    label: @Composable () -> Unit,
    enabled: Boolean,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    FilterChip(
        onClick = onClick,
        label = label,
        selected = enabled,
        leadingIcon = leadingIcon,
        trailingIcon = {
            if (enabled) {
                IconButton(onDismiss, Modifier.size(25.dp)) { Icon(Icons.Default.Close, "Dismiss") }
            } else {
                Icon(Icons.Default.ArrowDropDown, "Kunde")
            }
        })
}

@Composable
private fun ReadingCard(reading: Reading, query: String, model: ReadingsScreenModel) {
    ElevatedCard(
        modifier = Modifier
            .width(260.dp).wrapContentHeight()
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
                    val meterId = reading.matching(query)

                    Text(
                        AnnotatedString("Ablesung von ") + meterId,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                            .fillMaxWidth(fraction = .9f)
                    )
                    Spacer(Modifier.weight(1f))
                    ReadingDropDown(reading, model)
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()
                ) {
                    Row {
                        ReadingDetail(
                            Icons.Filled.Person,
                            reading.customer?.fullName ?: "Unbekannt"
                        )
                        ReadingDetail(
                            icon = Icons.Filled.GasMeter,
                            text = reading.meterCount.toString()
                        )
                    }
                    Row {
                        ReadingDetail(
                            icon = Icons.Filled.CalendarToday,
                            text = formatLocalDate(reading.date)
                        )
                        ReadingDetail(
                            icon = Icons.Filled.ElectricMeter,
                            text = reading.kind.readableName
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadingDetail(icon: ImageVector, text: String) {
    Row(Modifier.padding(horizontal = 3.dp)) {
        Icon(
            imageVector = icon, contentDescription = null
        )
        Spacer(modifier = Modifier.padding(horizontal = 3.dp))
        Text(text)
    }
}

@Composable
private fun ReadingDropDown(customer: Reading, model: ReadingsScreenModel) {
    var expanded by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    if (isDeleting) {
        DeleteDialog(
            isDeleting,
            { isDeleting = false },
            { model.deleteReading(customer.id) },
            title = { Text("Ablesung löschen?", color = MaterialTheme.colorScheme.onError) },
            text = { Text("Möchten Sie die Ablesung wirklich löschen?", color = MaterialTheme.colorScheme.onError) },
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