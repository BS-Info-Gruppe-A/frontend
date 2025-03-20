package eu.bsinfo.components.readings

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import eu.bsinfo.ReadingsScreenModel
import eu.bsinfo.components.DeleteDialog
import eu.bsinfo.components.Detail
import eu.bsinfo.data.Reading
import eu.bsinfo.rest.LocalClient
import eu.bsinfo.util.LocalPlatformContext
import eu.bsinfo.util.format
import eu.bsinfo.util.formatLocalDate
import kotlinx.coroutines.launch

@Composable
fun ReadingPopup(focusedReading: Reading?, model: ReadingsScreenModel) {
    var isDeleteDialogPresent by remember(focusedReading) { mutableStateOf(false) }
    var isEditMode by remember(focusedReading) { mutableStateOf(false) }

    if (focusedReading != null) {
        BasicAlertDialog(
            { model.unfocusEntity() }, properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(.8f)
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(15.dp))
                    .fillMaxWidth()
                    .fillMaxHeight(.85f)
                    .shadow(8.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 10.dp, vertical = 10.dp)
            ) {
                AnimatedContent(isEditMode) {
                    if (it) {
                        EditForm(focusedReading, model) { isEditMode = false }
                    } else {
                        ReadingDetails(focusedReading, { isEditMode = true }, { isDeleteDialogPresent = true })
                    }
                }
            }
        }

        DeleteDialog(
            isDeleteDialogPresent,
            { isDeleteDialogPresent = false },
            { model.deleteReading(focusedReading.id) },
            title = { Text("Ablesung löschen?", color = MaterialTheme.colorScheme.onError) },
            text = { Text("Möchten Sie die Ablesung wirklich löschen?", color = MaterialTheme.colorScheme.onError) },
        )
    }
}

@Composable
private fun EditForm(reading: Reading, model: ReadingsScreenModel, close: () -> Unit) = Column {
    val state = rememberReadingUpdateFormState(reading)
    val scope = rememberCoroutineScope()
    val client = LocalClient.current

    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        IconButton(close) {
            Icon(Icons.AutoMirrored.Default.ArrowBack, "Close")
        }

        Text("${reading.displayName} bearbeiten")

        Spacer(Modifier.weight(1f))
    }

    ReadingCreationForm(model, state, saveButtonText = { Text("Speichern") }) {
        scope.launch {
            model.updateReading(client, state)
        }
    }
}

@Composable
private fun ReadingDetails(
    reading: Reading,
    openEditMode: () -> Unit,
    openDeleteMode: () -> Unit
) {
    val context = LocalPlatformContext.current

    Column {
        Text(
            reading.displayName, style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth().fillMaxHeight()
            ) {
                Text(
                    "Daten",
                    style = MaterialTheme.typography.titleLarge,
                )
                Column {
                    Detail(
                        Icons.Filled.Person,
                        reading.customer?.fullName ?: "Unbekannt",
                    )
                    Detail(
                        icon = Icons.Filled.GasMeter,
                        text = reading.meterCount.format(context)
                    )

                    Detail(
                        icon = Icons.Filled.CalendarToday,
                        text = formatLocalDate(context, reading.date)
                    )
                    Detail(
                        icon = Icons.Filled.ElectricMeter,
                        text = reading.kind.humanName
                    )
                }

                HorizontalDivider(Modifier.fillMaxWidth().padding(horizontal = 10.dp))

                Column {
                    Text("Kommentar", style = MaterialTheme.typography.titleLarge)
                    Text(reading.comment ?: "Kein Kommentar vorhanden", style = MaterialTheme.typography.bodyMedium)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                ) {
                    Button(openDeleteMode) {
                        Icon(Icons.Filled.Delete, "Löschen")
                        Text("Löschen")
                    }
                    Button(openEditMode) {
                        Icon(Icons.Filled.Edit, "Bearbeiten")
                        Text("Bearbeiten")
                    }
                }
            }
        }
    }
}

