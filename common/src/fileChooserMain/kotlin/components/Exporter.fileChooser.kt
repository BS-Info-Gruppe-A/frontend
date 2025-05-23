package eu.bsinfo.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import eu.bsinfo.data.Format
import eu.bsinfo.file_dialog.FileDialogCancelException
import eu.bsinfo.file_dialog.Filter
import eu.bsinfo.util.FileHandle
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer

expect val supportsBrowseDirectory: Boolean
expect fun browseDirectory(path: FileHandle)
expect fun openFile(path: FileHandle)
expect suspend fun writeToFile(path: FileHandle, content: String)

expect suspend fun openSaveDialog(vararg filters: Filter): FileHandle

@Composable
actual fun rememberExporterState(): ExporterState = remember { ExporterState() }

actual class ExporterState {
    var exportPath: FileHandle? by mutableStateOf(null)
}

@Composable
actual fun ExporterDropdownEntry(state: ExporterState, onClose: () -> Unit) {
    val scope = rememberCoroutineScope()

    DropdownMenuItem({ Text("Export") }, {
        scope.launch {
            try {
                state.exportPath = openSaveDialog(*Format.entries.map {
                    Filter(it.extension, it.extension)
                }.toTypedArray())
            } catch (_: FileDialogCancelException) {
                onClose()
            }
        }
    }, leadingIcon = { Icon(Icons.AutoMirrored.Default.Logout, "export") })
}

@Composable
actual fun <T> Exporter(state: ExporterState, items: List<T>, serializer: KSerializer<T>, onClose: () -> Unit) =
    DataProcessor(
        state.exportPath, serializer, { onClose(); state.exportPath = null },
        done = { Text("Exportvorgang abgeschlossen!") },
        running = { Text("Exportiere ...") },
        doneDescription = {
            Text(
                state.exportPath.toString(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().clickable { openFile(state.exportPath!!) }
            )
        },
        additionalButtons = {
            if (supportsBrowseDirectory) {
                Button({ browseDirectory(state.exportPath!!) }) {
                    Icon(Icons.Default.FolderOpen, "Open folder")
                    Text("Open folder")
                }
            }
        },
        processor = { format, file, kSerializer ->
            val out = format.encodeToString(ListSerializer(kSerializer), items)
            writeToFile(file, out)
        }
    )
