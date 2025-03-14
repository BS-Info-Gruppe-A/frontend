package eu.bsinfo.util

import androidx.compose.runtime.Composable
import eu.bsinfo.file_dialog.FileDialogCancelException
import eu.bsinfo.file_dialog.Filter
import kotlinx.browser.document
import kotlinx.dom.createElement
import org.w3c.dom.HTMLInputElement
import org.w3c.files.File
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

actual class FileHandle(private val delegate: File) {
    actual val name: String get() = delegate.name
    actual suspend fun read() = suspendCoroutine { cont ->
        val reader = FileReader()
        reader.onload = {
            cont.resume(reader.result?.toString()!!)
        }
        reader.readAsText(delegate)
    }
}

private object WasmFilePicker : FilePicker {
    override suspend fun chooseFile(vararg filters: Filter): FileHandle =
        suspendCoroutine { cont ->
            val input = document.createElement("input") {
                require(this is HTMLInputElement)
                type = "file"
                accept = ".json, .csv, .xml"
            } as HTMLInputElement

            input.oncancel = {
                input.remove()
                cont.resumeWithException(FileDialogCancelException())
            }
            input.onchange = {
                val file = input.files!![0]!!

                input.remove()
                cont.resume(FileHandle(file))
            }

            input.click()
        }
}

@Composable
actual fun rememberFilePicker(): FilePicker = WasmFilePicker
