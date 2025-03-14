package eu.bsinfo.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import app.softwork.serialization.csv.CSVFormat
import eu.bsinfo.common.generated.resources.Res
import eu.bsinfo.common.generated.resources.csv_24px
import eu.bsinfo.common.generated.resources.file_json_24px
import eu.bsinfo.common.generated.resources.twemoji_poop
import eu.bsinfo.util.FileHandle
import eu.bsinfo.util.formats
import io.ktor.util.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.StringFormat
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.serialization.XML
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalSerializationApi::class)
val StringFormat.extension: String
    get() = when (this) {
        is Json -> "json"
        is CSVFormat -> "csv"
        is XML -> "xml"
        else -> error("Unknown format: $this")
    }

@OptIn(ExperimentalSerializationApi::class)
val StringFormat.icon: Painter
    @Composable
    get() = when (this) {
        is Json -> painterResource(Res.drawable.file_json_24px)
        is CSVFormat -> painterResource(Res.drawable.csv_24px)
        is XML -> painterResource(Res.drawable.twemoji_poop)
        else -> error("Unknown format: $this")
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
            formats.forEach { (name, format) ->
                DropdownMenuItem(
                    { Text(name) },
                    onClick = { state.handle = FileHandle("output.${format.extension}") },
                    leadingIcon = {
                        Icon(
                            format.icon,
                            format.extension,
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
            fileSaver.saveFile(out, file.name)
        }
    )
}
