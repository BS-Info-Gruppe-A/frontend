package eu.bsinfo.components

import androidx.compose.runtime.Composable
import eu.bsinfo.util.FileHandle
import io.ktor.util.toJsArray
import io.ktor.utils.io.core.toByteArray
import kotlinx.browser.document
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.File

actual fun FileHandle(filename: String): FileHandle = FileHandle(File(JsArray(), filename))

private object WasmJsFileSaver : FileSaver {
    override suspend fun saveFile(content: String, filename: String) {
        val mimeType = when (filename.substringAfterLast('.')) {
            "json" -> "application/json"
            "csv" -> "text/csv"
            "xml" -> "application/xml"
            else -> error("Unknown file type: $filename")
        }

        @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
        val blob = Blob(
            arrayOf(content.toByteArray().toJsArray() as JsAny?).toJsArray(),
            BlobPropertyBag(type = mimeType)
        )
        val url = URL.createObjectURL(blob)

        val anchor = document.createElement("a") as HTMLAnchorElement
        anchor.href = url
        anchor.download = filename
        anchor.click()
        URL.revokeObjectURL(url)
        anchor.remove()
    }
}

@Composable
actual fun rememberFileSaver(): FileSaver = WasmJsFileSaver
