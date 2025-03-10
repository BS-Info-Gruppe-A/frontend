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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.serialization.KSerializer

data class Filter(val name: String, val extension: String)

class FileDialogException(message: String?) : RuntimeException(message)
class FileDialogCancelException : RuntimeException()

expect suspend fun openSaveDialog(vararg filters: Filter): Path
expect fun openFile(path: Path)

@Composable
actual fun <T> HamburgerItems(items: List<T>, serializer: KSerializer<T>) {
    val scope = rememberCoroutineScope()

    var exportPath by remember { mutableStateOf<Path?>(null) }

    DropdownMenuItem({ Text("Export") }, {
        scope.launch {
            exportPath = openSaveDialog(*formats.keys.map {
                Filter(it, it)
            }.toTypedArray())
        }
    }, leadingIcon = { Icon(Icons.AutoMirrored.Default.Login, "import") })
    DropdownMenuItem({ Text("Import") }, {}, leadingIcon = { Icon(Icons.AutoMirrored.Default.Logout, "export") })

    Exporter(exportPath, items, serializer, { exportPath = null })
}

@Composable
private fun <T> Exporter(exportPath: Path?, items: List<T>, serializer: KSerializer<T>, onClose: () -> Unit) {
    var processing by remember(exportPath) { mutableStateOf(true) }
    var invalidFileExtension by remember(exportPath) { mutableStateOf(false) }

    if (exportPath != null) {
        LaunchedEffect(exportPath) {
            withContext(Dispatchers.IO) {
                val extension = exportPath.name.substringAfterLast('.')
                val format = formats[extension]
                if (format == null) {
                    invalidFileExtension = true
                } else {
                    items.export(format, exportPath, serializer)
                }
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
                    Text("Erfolgreich exportiert")
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
                        Text("Exportiere ...", textAlign = TextAlign.Center)
                    }
                } else {
                    Text(
                        exportPath.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.clickable { openFile(exportPath) })
                }
            }
        )
    }
}