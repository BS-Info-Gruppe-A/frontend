package eu.bsinfo.util

import kotlinx.io.IOException

open class FileDialogException(message: String?) : IOException(message)
class FileDialogCancelException : FileDialogException("File dialog cancelled by user")

data class Filter(val name: String, val spec: String)

expect class FileHandle {
    val name: String
    suspend fun read(): String
}

val FileHandle.extension: String
    get() = name.substringAfterLast('.', )

expect suspend fun chooseFile(vararg filters: Filter): FileHandle?
