package eu.bsinfo.cli.command

import com.github.ajalt.clikt.completion.CompletionCandidates
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

fun RawOption.path(validateParent: Boolean = true, validateExists: Boolean = false, fs: FileSystem = SystemFileSystem) =
    convert("path", CompletionCandidates.Path) {
        Path(it).also {
            if (validateParent && it.parent != null && fs.metadataOrNull(it.parent!!)?.isDirectory != true) fail("File ${it.parent} does not exist")
            if (validateExists && fs.metadataOrNull(it)?.isRegularFile != true) fail("File $it does not exist")
        }
    }
