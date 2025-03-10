package eu.bsinfo.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eu.bsinfo.util.export
import eu.bsinfo.util.formats
import eu.bsinfo.util.import
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.serialization.KSerializer
import kotlinx.serialization.StringFormat

data class Filter(val name: String, val extension: String)

class FileDialogException(message: String?) : RuntimeException(message)
class FileDialogCancelException : RuntimeException()

expect suspend fun openSaveDialog(vararg filters: Filter): Path
expect suspend fun openLoadDialog(vararg filters: Filter): Path
expect fun openFile(path: Path)

private val LOG = KotlinLogging.logger { }

@Composable
actual fun <T> HamburgerItems(
    importItem: suspend (T) -> Unit,
    onClose: () -> Unit,
    items: List<T>,
    serializer: KSerializer<T>,
    model: EntityViewModel
) {
    val scope = rememberCoroutineScope()

    var exportPath by remember { mutableStateOf<Path?>(null) }
    var importPath by remember { mutableStateOf<Path?>(null) }

    DropdownMenuItem({ Text("Export") }, {
        scope.launch {
            exportPath = openSaveDialog(*formats.keys.map {
                Filter(it, it)
            }.toTypedArray())
        }
    }, leadingIcon = { Icon(Icons.AutoMirrored.Default.Login, "import") })
    DropdownMenuItem({ Text("Import") }, {
        scope.launch {
            importPath = openLoadDialog(*formats.keys.map {
                Filter(it, it)
            }.toTypedArray())
        }
    }, leadingIcon = { Icon(Icons.AutoMirrored.Default.Logout, "export") })

    Exporter(exportPath, items, serializer) { exportPath = null; onClose() }
    Importer(importPath, importItem, serializer, { importPath = null; onClose() }, model)
}

@Composable
private fun <T> Exporter(exportPath: Path?, items: List<T>, serializer: KSerializer<T>, onClose: () -> Unit) =
    DataProcessor(
        exportPath, serializer, onClose,
        done = { Text("") },
        running = { Text("Exportiere ...") },
        doneDescription = {
            Text(
                exportPath.toString(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { openFile(exportPath!!) }
            )
        },
        processor = items::export
    )

@Composable
private fun <T> Importer(
    importPath: Path?,
    importItem: suspend (T) -> Unit,
    serializer: KSerializer<T>,
    onClose: () -> Unit,
    model: EntityViewModel
) {
    var successfulItems by remember(importPath) { mutableStateOf(0) }
    var failedItems by remember(importPath) { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    @Composable
    fun Report() {
        Text("Erfolgreich: $successfulItems. Fehlgeschlagen: $failedItems")
    }

    DataProcessor(
        importPath, serializer, onClose,
        done = { Text("Importvorgang abgeschlossen") },
        doneDescription = { Report() },
        running = { Report() },
        onDone = { scope.launch { model.refresh() } },
        processor = { format, path, kSerializer ->
            val items = path.import(format, kSerializer)
            items.forEach {
                try {
                    importItem(it)
                    successfulItems++
                } catch (e: Exception) {
                    LOG.warn(e) { "Failed to import item" }
                    failedItems++
                }
            }
        }
    )
}

@Composable
private fun <T> DataProcessor(
    path: Path?, serializer: KSerializer<T>, onClose: () -> Unit,
    done: @Composable () -> Unit,
    running: @Composable () -> Unit,
    doneDescription: @Composable () -> Unit,
    processor: suspend (StringFormat, Path, KSerializer<T>) -> Unit,
    onDone: () -> Unit = {}
) {
    var processing by remember(path) { mutableStateOf(true) }
    var invalidFileExtension by remember(path) { mutableStateOf(false) }

    if (path != null) {
        LaunchedEffect(path) {
            withContext(Dispatchers.IO) {
                val extension = path.name.substringAfterLast('.')
                val format = formats[extension]
                if (format == null) {
                    invalidFileExtension = true
                } else {
                    processor(format, path, serializer)
                }
                onDone()
                processing = false
            }
        }

        AlertDialog(
            { if (!processing) onClose() },
            {},
            icon = { Icon(Icons.AutoMirrored.Default.Logout, "Export") },
            title = {
                if (invalidFileExtension) {
                    Text("Ungültige Dateiendung!")
                } else if (!processing) {
                    done()
                }
            },
            dismissButton = {
                if (invalidFileExtension || !processing) {
                    Button(onClose) {
                        Icon(Icons.Default.Check, "Ok")
                        Text("OK")
                    }
                }
            },
            text = {
                if (invalidFileExtension) {
                    Text("Bitte verwende json, csv oder xml", textAlign = TextAlign.Center)
                } else if (processing) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator()
                        running()
                    }
                } else {
                    doneDescription()
                }
            }
        )
    }
}
