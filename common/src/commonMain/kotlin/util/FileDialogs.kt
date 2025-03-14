package eu.bsinfo.util

import androidx.compose.runtime.Composable
import eu.bsinfo.file_dialog.Filter


expect class FileHandle {
    val name: String
    suspend fun read(): String
}

val FileHandle.extension: String
    get() = name.substringAfterLast('.')

interface FilePicker {
    suspend fun chooseFile(vararg filters: Filter): FileHandle
}

@Composable
expect fun rememberFilePicker(): FilePicker
