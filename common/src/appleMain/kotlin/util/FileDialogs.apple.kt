package eu.bsinfo.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import eu.bsinfo.IO
import eu.bsinfo.file_dialog.FileDialogCancelException
import eu.bsinfo.file_dialog.Filter
import io.ktor.utils.io.core.writeText
import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.bytestring.decodeToString
import kotlinx.io.bytestring.toByteString
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import platform.Foundation.NSArray
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSMutableArray
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.stringWithUTF8String
import platform.Foundation.temporaryDirectory
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UniformTypeIdentifiers.UTTypeCommaSeparatedText
import platform.UniformTypeIdentifiers.UTTypeJSON
import platform.UniformTypeIdentifiers.UTTypeXML
import platform.darwin.NSObject

private val fs = SystemFileSystem

private fun NSURL.toPath() = Path(path!!)

actual class FileHandle(private val url: NSURL) {
    actual val name: String get() = url.lastPathComponent!!

    @Suppress("UnnecessaryOptInAnnotation")
    @OptIn(BetaInteropApi::class)
    actual suspend fun read(): String {
        url.startAccessingSecurityScopedResource()
        return try {
            val bytes = NSData.create(contentsOfURL = url)!!
            bytes.toByteString().decodeToString()
        } finally {
            url.stopAccessingSecurityScopedResource()
        }
    }
}

private class PickerCallback(
    val exportJob: ExportJob?,
    val result: CompletableDeferred<FileHandle> = CompletableDeferred()
) :
    NSObject(), UIDocumentPickerDelegateProtocol {
    private fun cleanup() {
        if (exportJob != null) {
            val tempDir = NSURL.fileURLWithPath(NSTemporaryDirectory())
            val fileUrl = NSURL.fileURLWithPath(exportJob.name, tempDir)
            fs.delete(fileUrl.toPath())
        }
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        result.completeExceptionally(FileDialogCancelException())
        cleanup()
    }

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentAtURL: NSURL
    ) {
        result.complete(FileHandle(didPickDocumentAtURL))
        cleanup()
    }
}

internal data class ExportJob(val name: String, val content: String)

internal class AppleFilePicker(
    private val uiViewController: UIViewController,
    private val export: ExportJob? = null
) : FilePicker {
    override suspend fun chooseFile(vararg filters: Filter): FileHandle {
        val callback = PickerCallback(export)
        val types = filters.flatMap(Filter::spec)
            .map {
                when (it) {
                    "xml" -> UTTypeXML
                    "json" -> UTTypeJSON
                    "csv" -> UTTypeCommaSeparatedText
                    else -> error("Unsupported")
                }
            }
        val picker = if (export != null) {
            val tempDir = NSURL.fileURLWithPath(NSTemporaryDirectory())
            val fileUrl = NSURL.fileURLWithPath(export.name, tempDir)
            val path = Path(fileUrl.path!!)

            withContext(Dispatchers.IO) {
                SystemFileSystem.sink(path).buffered().use {
                    it.writeText(export.content)
                }
            }
            UIDocumentPickerViewController(forExportingURLs = listOf(fileUrl))
        } else {
            UIDocumentPickerViewController(forOpeningContentTypes = types)
        }
        picker.apply {
            delegate = callback
            shouldShowFileExtensions = true
            allowsMultipleSelection = false
        }

        uiViewController.presentViewController(picker, true, null)
        return callback.result.await()
    }
}

@Composable
actual fun rememberFilePicker(): FilePicker {
    val uiController = LocalUIViewController.current

    return remember { AppleFilePicker(uiController) }
}
