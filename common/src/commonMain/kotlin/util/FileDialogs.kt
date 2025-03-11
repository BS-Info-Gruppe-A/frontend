package eu.bsinfo.util

import eu.bsinfo.file_dialog.Filter


expect class FileHandle {
    val name: String
    suspend fun read(): String
}

val FileHandle.extension: String
    get() = name.substringAfterLast('.', )

expect suspend fun chooseFile(vararg filters: Filter): FileHandle?
