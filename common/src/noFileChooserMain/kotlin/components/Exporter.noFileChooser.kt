package eu.bsinfo.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import eu.bsinfo.common.generated.resources.Res
import eu.bsinfo.common.generated.resources.csv_24px
import eu.bsinfo.common.generated.resources.file_json_24px
import eu.bsinfo.common.generated.resources.twemoji_poop
import eu.bsinfo.data.Format
import eu.bsinfo.file_dialog.FileDialogCancelException
import eu.bsinfo.util.FileHandle
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import org.jetbrains.compose.resources.painterResource


@OptIn(ExperimentalSerializationApi::class)
val Format.icon: Painter
    @Composable
    get() = when (this) {
        Format.JSON -> painterResource(Res.drawable.file_json_24px)
        Format.CSV -> painterResource(Res.drawable.csv_24px)
        Format.XML -> painterResource(Res.drawable.twemoji_poop)
    }

interface FileSaver {
    suspend fun saveFile(content: String, filename: String)
}

@Composable
expect fun rememberFileSaver(): FileSaver

expect fun FileHandle(filename: String): FileHandle

@Composable
actual fun rememberExporterState(): ExporterState = remember { ExporterState() }

actual class ExporterState {
    var handle: FileHandle? by mutableStateOf(null)
}

@Composable
actual fun ExporterDropdownEntry(state: ExporterState, onClose: () -> Unit) {
    var isOpen by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(isOpen, { isOpen = false }) {
        DropdownMenuItem(
            { Text("Export") }, { isOpen = true },
            leadingIcon = { Icon(Icons.AutoMirrored.Default.Logout, "export") },
            trailingIcon = { Icon(Icons.Default.ChevronRight, "export options") },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )

        ExposedDropdownMenu(isOpen, { isOpen = false }) {
            Format.entries.forEach {
                DropdownMenuItem(
                    { Text(it.name) },
                    onClick = { state.handle = FileHandle("output.${it.extension}") },
                    leadingIcon = {
                        Icon(
                            it.icon,
                            it.extension,
                            tint = LocalContentColor.current,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                    },
                )
            }
        }
    }
}


@Composable
actual fun <T> Exporter(state: ExporterState, items: List<T>, serializer: KSerializer<T>, onClose: () -> Unit) {
    val fileSaver = rememberFileSaver()
    DataProcessor(
        state.handle, serializer, { onClose(); state.handle = null },
        done = { Text("Exportvorgang abgeschlossen!") },
        running = { Text("Exportiere ...") },
        processor = { format, file, kSerializer ->
            val out = format.encodeToString(ListSerializer(kSerializer), items)
            try {
                fileSaver.saveFile(out, file.name)
            } catch (_: FileDialogCancelException) {
                state.handle = null
                onClose()
            }
        }
    )
}
