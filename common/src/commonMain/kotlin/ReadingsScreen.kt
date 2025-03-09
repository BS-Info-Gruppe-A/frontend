package eu.bsinfo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.bsinfo.components.DeleteDialog
import eu.bsinfo.data.Reading
import eu.bsinfo.data.readableFormat
import eu.bsinfo.rest.Client
import eu.bsinfo.rest.LocalClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

data class ReadingsScreenState(
    val isLoading: Boolean = true,
    val readings: List<Reading> = emptyList(),
)

class ReadingsScreenModel(private val client: Client) : ViewModel() {
    private val _uiState = MutableStateFlow(ReadingsScreenState())
    val uiState = _uiState.asStateFlow()

    suspend fun refreshReadings() = withContext(Dispatchers.IO) {
        _uiState.emit(uiState.value.copy(readings = client.getReadings().readings, isLoading = false))
    }

    suspend fun deleteReading(readingId: Uuid) = withContext(Dispatchers.IO) {
        client.deleteReading(readingId)
        _uiState.emit(uiState.value.copy(readings = uiState.value.readings.filter { it.id != readingId }))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingsScreen(
    client: Client = LocalClient.current,
    model: ReadingsScreenModel = viewModel { ReadingsScreenModel(client) }
) {
    val state by model.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    if (state.isLoading) {
        LaunchedEffect(state) { model.refreshReadings() }
    }

    PullToRefreshBox(
        state.isLoading,
        { scope.launch { model.refreshReadings() } },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyVerticalGrid(
            GridCells.Adaptive(260.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.readings) { reading ->
                ReadingCard(reading, model)
            }
        }
    }
}

@Composable
private fun ReadingCard(reading: Reading, model: ReadingsScreenModel) {
    ElevatedCard(
        modifier = Modifier
            .width(260.dp)
            .height(150.dp)
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
                        "Ablesung von ${reading.meterId}",
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
                            text = readableFormat.format(reading.date)
                        )
                        ReadingDetail(
                            icon = Icons.Filled.ElectricMeter,
                            text = reading.kind.readableName
                        )
                    }
                    Row {
                        ReadingDetail(
                            icon = Icons.AutoMirrored.Filled.Comment,
                            text = reading.comment
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
