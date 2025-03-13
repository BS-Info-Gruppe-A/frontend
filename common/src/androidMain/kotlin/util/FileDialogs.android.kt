package eu.bsinfo.util

import android.content.Context
import android.net.Uri
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
import java.io.BufferedReader

actual class FileHandle(private val context: Context, private val path: Uri) {
    actual val name: String get() = path.lastPathSegment!!

    actual suspend fun read(): String = with(Dispatchers.IO) {
        return context.contentResolver.openInputStream(path)!!
            .bufferedReader().use(BufferedReader::readText)
    }
}

private class AndroidFilePicker(
    private val launcher: ManagedActivityResultLauncher<Array<String>, Uri?>,
    private val result: CompletableDeferred<FileHandle>
) : FilePicker {

    override suspend fun chooseFile(vararg filters: Filter): FileHandle? {
        launcher.launch(filters.flatMap(Filter::spec).toTypedArray())

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
