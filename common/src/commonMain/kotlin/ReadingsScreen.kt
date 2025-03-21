package eu.bsinfo

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.bsinfo.components.*
import eu.bsinfo.components.readings.*
import eu.bsinfo.data.Client
import eu.bsinfo.data.Customer
import eu.bsinfo.data.Reading
import eu.bsinfo.rest.LocalClient
import eu.bsinfo.util.*
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
    override val loading: Boolean = true,
    val isDatePickerDialogVisible: Boolean = false,
    val isKindSheetVisible: Boolean = false,
    val isCustomerSheetVisible: Boolean = false,
    val selectedStartDate: LocalDate? = null,
    val selectedEndDate: LocalDate? = null,
    val selectedKind: Reading.Kind? = null,
    val selectedCustomer: Customer? = null,
    override val creationFormVisible: Boolean = false,
    override val query: String = "",
    val readings: List<Reading> = emptyList(),
    val focusedReading: Reading? = null
) : EntityViewState {
    fun formatDate(context: PlatformContext): String? {
        val startDate = selectedStartDate
        val endDate = selectedEndDate
        if (startDate == null) return null

        return buildString {
            append(formatLocalDate(context, startDate))
            if (endDate != null) {
                append(" - ")
                append(formatLocalDate(context, endDate))
            }
        }
    }
}

class ReadingsScreenModel(val client: Client) : ViewModel(), EntityViewModel<Reading> {
    private val _uiState = MutableStateFlow(ReadingsScreenState())
    override val uiState = _uiState.asStateFlow()

    fun openDateSheet() = _uiState.tryEmit(uiState.value.copy(isDatePickerDialogVisible = true))
    fun closeDateSheet() = _uiState.tryEmit(uiState.value.copy(isDatePickerDialogVisible = false))
    fun openKindPickerSheet() = _uiState.tryEmit(uiState.value.copy(isKindSheetVisible = true))
    fun closeKindPickerSheet() = _uiState.tryEmit(uiState.value.copy(isKindSheetVisible = false))
    fun openCustomerSheet() = _uiState.tryEmit(uiState.value.copy(isCustomerSheetVisible = true))
    fun closeCustomerSheet() = _uiState.tryEmit(uiState.value.copy(isCustomerSheetVisible = false))
    override fun openCreationForm() {
        _uiState.tryEmit(uiState.value.copy(creationFormVisible = true))
    }

    override fun closeCreationForm() {
        _uiState.tryEmit(uiState.value.copy(creationFormVisible = false))
    }

    override fun setSearchQuery(text: String) {
        _uiState.tryEmit(
            _uiState.value.copy(
                query = text,
                readings = _uiState.value.readings.search(text)
            )
        )
    }

    suspend fun setDateRange(from: Long?, to: Long?) {
        _uiState.tryEmit(
            uiState.value.copy(
                selectedStartDate = from?.toLocalDate(), selectedEndDate = to?.toLocalDate(),
                isDatePickerDialogVisible = false, loading = true
            )
        )

        refresh()
    }

    suspend fun setKind(kind: Reading.Kind?) {
        _uiState.tryEmit(
            uiState.value.copy(
                selectedKind = kind, isKindSheetVisible = false, loading = true
            )
        )

        refresh()
    }

    suspend fun setCustomer(customer: Customer?) {
        _uiState.tryEmit(
            uiState.value.copy(
                selectedCustomer = customer, isCustomerSheetVisible = false, loading = true
            )
        )

        refresh()
    }

    override fun setLoading(loading: Boolean) {
        _uiState.tryEmit(_uiState.value.copy(loading = loading))
    }

    override suspend fun refresh() = withContext(Dispatchers.IO) {
        val state = _uiState.value
        _uiState.emit(
            state.copy(
                readings = client.getReadings(
                    from = state.selectedStartDate,
                    to = state.selectedEndDate,
                    kind = state.selectedKind,
                    customerId = state.selectedCustomer?.id
                ).readings, loading = false
            )
        )
    }

    suspend fun deleteReading(readingId: Uuid) = withContext(Dispatchers.IO) {
        client.deleteReading(readingId)
        _uiState.emit(
            uiState.value.copy(
                readings = uiState.value.readings.filter { it.id != readingId },
                focusedReading = uiState.value.focusedReading?.takeIf { it.id != readingId })
        )
    }

    suspend fun updateReading(client: Client, state: ReadingCreationFormState) = withContext(Dispatchers.IO) {
        val reading = state.toReading()

        client.updateReading(reading.toUpdatableReading())
        focusEntity(reading)
    }

    override fun focusEntity(entity: Reading) {
        _uiState.tryEmit(uiState.value.copy(focusedReading = entity))
    }

    override fun unfocusEntity() {
        _uiState.tryEmit(uiState.value.copy(focusedReading = null))
    }

    private fun Long.toLocalDate(): LocalDate {
        val dateTime =
            Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault())
        return LocalDate(dateTime.year, dateTime.month, dateTime.dayOfMonth)
    }
}

@Composable
fun ReadingsScreen(
    route: MainScreen.Readings,
    client: Client = LocalClient.current,
    model: ReadingsScreenModel = viewModel { ReadingsScreenModel(client) }
) {
    val state by model.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    if (state.loading) {
        LaunchedEffect(state) { model.refresh() }
    }

    EntityContainer(
        model,
        state.readings,
        client::createReading,
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
                ReadingCard(reading, state.query, { model.focusEntity(reading) })
            }
        }
    }

    ReadingCreationSheet(model, route)
    ReadingDatePicker(state, model)
    KindPicker(state, model)
    ReadingPopup(state.focusedReading, model)
    CustomerPickerSheet(client, state.isCustomerSheetVisible, { model.closeCustomerSheet() }, {
        scope.launch { model.setCustomer(it) }
    })
}

@Composable
private fun Filters(model: ReadingsScreenModel, modifier: Modifier = Modifier) {
    val uiState by model.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalPlatformContext.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterHorizontally),
        modifier = modifier.fillMaxWidth().padding(horizontal = 10.dp)
            .horizontalScroll(rememberScrollState())
    ) {
        Filter(
            onClick = { model.openDateSheet() },
            onDismiss = { scope.launch { model.setDateRange(null, null) } },
            label = {
                val range = uiState.formatDate(context) ?: "Datum"
                Text(range)
            },
            enabled = uiState.selectedStartDate != null,
            leadingIcon = { Icon(Icons.Default.DateRange, "Date") }
        )
        Filter(
            onClick = { model.openKindPickerSheet() },
            onDismiss = { scope.launch { model.setKind(null) } },
            label = { Text(uiState.selectedKind?.humanName ?: "Ablesungsart") },
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
fun ReadingCard(reading: Reading, query: String = "", onClick: (() -> Unit)? = null) {
    EntityCard(reading, query, onClick) {
        val context = LocalPlatformContext.current

        Row {
            Detail(
                Icons.Filled.Person,
                reading.customer?.fullName ?: "Unbekannt",
                modifier = Modifier.fillMaxWidth(.6f)
            )
            Detail(
                icon = Icons.Filled.GasMeter,
                text = reading.meterCount.format(context)
            )
        }
        Row {
            Detail(
                icon = Icons.Filled.CalendarToday,
                text = formatLocalDate(context, reading.date)
            )
            Detail(
                icon = Icons.Filled.ElectricMeter,
                text = reading.kind.humanName
            )
        }
    }
}
