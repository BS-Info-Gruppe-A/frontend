package eu.bsinfo.file_dialog

import kotlinx.io.IOException

data class Filter(val name: String, val spec: Collection<String>) {
    constructor(name: String, vararg spec: String) : this(name, spec.toList())
}

class FileDialogCancelException() : IOException()