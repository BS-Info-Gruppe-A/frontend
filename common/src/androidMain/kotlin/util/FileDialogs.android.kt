package eu.bsinfo.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import eu.bsinfo.file_dialog.FileDialogCancelException
import eu.bsinfo.file_dialog.Filter
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import java.io.BufferedReader

actual class FileHandle(private val context: Context? = null, private val path: Uri) {
    actual val name: String
        get() = context?.let {
            val query = it.contentResolver.query(path, null, null, null, null)
                ?: return@let null
            query.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = query.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.getString(index)
                } else {
                    null
                }
            }
        } ?: path.lastPathSegment ?: path.host!!

    actual suspend fun read(): String = withContext(Dispatchers.IO) {
        requireNotNull(context) { "This is a name only file" }
        context.contentResolver.openInputStream(path)!!
            .bufferedReader().use(BufferedReader::readText)
    }

    suspend fun write(content: String) = withContext(Dispatchers.IO) {
        requireNotNull(context) { "This is a name only file" }
        context.contentResolver.openOutputStream(path)!!
            .bufferedWriter().use { it.write(content) }
    }
}

private fun String.toMimeType() = when (this) {
    "csv" -> "text/csv"
    "xml" -> "application/xml"
    "json" -> "application/json"
    else -> error("Unknown type")
}

private class AndroidFilePicker(
    private val launcher: ManagedActivityResultLauncher<Array<String>, Uri?>,
    private val result: CompletableDeferred<FileHandle>
) : FilePicker {

    override suspend fun chooseFile(vararg filters: Filter): FileHandle {
        launcher.launch(
            filters.asSequence().flatMap(Filter::spec).map(String::toMimeType).toList()
                .toTypedArray()
        )

        return result.await()
    }
}

@Composable
actual fun rememberFilePicker(): FilePicker {
    var deferred by remember { mutableStateOf(CompletableDeferred<FileHandle>()) }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        if (it == null) {
            deferred.completeExceptionally(FileDialogCancelException())
        } else {
            deferred.complete(FileHandle(context, it))
        }
        deferred = CompletableDeferred()
    }

    return remember(context, launcher) { AndroidFilePicker(launcher, deferred) }
}
