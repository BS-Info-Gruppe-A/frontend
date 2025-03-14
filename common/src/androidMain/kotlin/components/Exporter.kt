package eu.bsinfo.components

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
import androidx.core.app.ActivityOptionsCompat
import eu.bsinfo.file_dialog.FileDialogCancelException
import eu.bsinfo.file_dialog.Filter
import eu.bsinfo.util.FileHandle
import eu.bsinfo.util.FilePicker
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import androidx.core.net.toUri

private class AndroidFileSaver(
    private val launcher: ManagedActivityResultLauncher<String, Uri?>,
    private val result: CompletableDeferred<FileHandle>
) : FileSaver {

    override suspend fun saveFile(content: String, filename: String) {
        launcher.launch(filename)

        val file = result.await()
        file.write(content)
    }
}

actual fun FileHandle(filename: String): FileHandle = FileHandle(null, "file://$filename".toUri())

@Composable
actual fun rememberFileSaver(): FileSaver {
    var deferred by remember { mutableStateOf(CompletableDeferred<FileHandle>()) }
    val context = LocalContext.current
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) {
            if (it == null) {
                deferred.completeExceptionally(FileDialogCancelException())
            } else {
                deferred.complete(FileHandle(context, it))
            }
            deferred = CompletableDeferred()
        }

    return remember(context, launcher) { AndroidFileSaver(launcher, deferred) }
}
