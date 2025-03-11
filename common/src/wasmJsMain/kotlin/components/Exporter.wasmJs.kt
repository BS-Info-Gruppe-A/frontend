package eu.bsinfo.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import app.softwork.serialization.csv.CSVFormat
import eu.bsinfo.util.FileHandle
import eu.bsinfo.util.formats
import io.ktor.util.*
import io.ktor.utils.io.core.*
import kotlinx.browser.document
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.StringFormat
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.serialization.XML
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.File

@OptIn(ExperimentalSerializationApi::class)
val StringFormat.extension: String
    get() = when(this) {
        is Json -> "json"
        is CSVFormat -> "csv"
        is XML -> "xml"
        else -> error("Unknown format: $this")
    }

fun downloadFile(content: String, filename: String) {
    val mimeType = when (filename.substringAfterLast('.')) {
        "json" -> "application/json"
        "csv" -> "text/csv"
        "xml" -> "application/xml"
        else -> error("Unknown file type: $filename")
    }

    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    val blob = Blob(arrayOf(content.toByteArray().toJsArray() as JsAny?).toJsArray(), BlobPropertyBag(type = mimeType))
    val url = URL.createObjectURL(blob)

    val anchor = document.createElement("a") as HTMLAnchorElement
    anchor.href = url
    anchor.download = filename
    anchor.click()
    URL.revokeObjectURL(url)
    anchor.remove()
}

@Composable
actual fun rememberExporterState(): ExporterState = remember { ExporterState() }

actual class ExporterState {
    var handle: FileHandle? by mutableStateOf(null)
}

@Composable
actual fun ExporterDropdownEntry(state: ExporterState, onClose: () -> Unit) {
    var isOpen by remember { mutableStateOf(false) }

    Row {
        DropdownMenuItem(
            { Text("Export") }, { isOpen = true },
            leadingIcon = { Icon(Icons.AutoMirrored.Default.Logout, "export") },
            trailingIcon = { Icon(Icons.Default.ChevronRight, "export options") },
        )

        DropdownMenu(isOpen, { isOpen = false }) {
            formats.forEach { (name, format) ->
                DropdownMenuItem(
                    { Text(name) },
                    onClick = { state.handle = FileHandle(File(JsArray(), "output.${format.extension}")) },
                    leadingIcon = { Icon(Icons.Default.ChevronRight, "export options") },
                )
            }
        }
    }
}


@Composable
actual fun <T> Exporter(state: ExporterState, items: List<T>, serializer: KSerializer<T>, onClose: () -> Unit) {
    DataProcessor(
        state.handle, serializer, { onClose(); state.handle = null },
        done = { Text("Exportvorgang abgeschlossen!") },
        running = { Text("Exportiere ...") },
        processor = { format, file, kSerializer ->
            val out = format.encodeToString(ListSerializer(kSerializer), items)
            downloadFile(out, file.name)
        }
    )
}
