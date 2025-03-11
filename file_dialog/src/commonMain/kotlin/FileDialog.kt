package eu.bsinfo.file_dialog

import kotlinx.io.IOException

data class Filter(val name: String, val spec: String)

class FileDialogCancelException() : IOException()