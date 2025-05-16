package eu.bsinfo.cli.command

import androidx.compose.runtime.*
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.enum
import com.jakewharton.mosaic.NonInteractivePolicy
import com.jakewharton.mosaic.runMosaic
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.Text
import eu.bsinfo.cli.components.ImporterStatus
import eu.bsinfo.cli.components.Loading
import eu.bsinfo.cli.components.Success
import eu.bsinfo.data.Format
import eu.bsinfo.data.Client
import eu.bsinfo.data.Identifiable
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import io.ktor.util.*
import kotlinx.coroutines.*
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlin.time.Duration.Companion.milliseconds

abstract class ImportExportCommand(name: String) : CliktCommand(name) {
    protected val client by requireObject<Client>()
    protected val format by option("-f", "--format", help = "Format of the import file")
        .enum<Format>()
        .required()

    @Composable
    abstract fun execute()

    final override fun run(): Unit = runBlocking {
        runMosaic(NonInteractivePolicy.Ignore) { execute() }
    }
}

abstract class ExportCommand<T> : ImportExportCommand("export") {

    val destination by option("-o", "--output", help = "Path to the export file").path()
        .required()

    abstract val serializer: KSerializer<T>

    abstract suspend fun retrieveItems(): List<T>

    @Composable
    override fun execute() {
        var loading by remember { mutableStateOf(true) }

        if (loading) {
            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    val items = retrieveItems()
                    val string = format.encodeToString(ListSerializer(serializer), items)

                    SystemFileSystem.sink(destination).buffered().use {
                        it.writeString(string)
                    }

                    loading = false
                }
            }

            Loading { Text("Exporting ...") }
        } else {
            Success { Text("Exported!") }
        }
    }
}

abstract class ImportCommand<T : Identifiable> : ImportExportCommand("import") {
    protected val destination by option("-i", "--input", help = "Path to the input file")
        .path(validateExists = true)
        .required()

    protected abstract val serializer: KSerializer<T>

    protected abstract suspend fun importItem(item: T)

    data class Job(
        val id: String,
        val state: State,
        val name: String,
        val failure: String? = null
    ) {
        enum class State(val color: Color, val displayName: String) {
            Running(Color.Yellow, "RUN"),
            Succeeded(Color.Green, "SUCCESS"),
            Failed(Color.Red, "FAIL")
        }
    }

    @Composable
    override fun execute() {
        var total by remember { mutableStateOf<Int?>(null) }
        var jobs by remember { mutableStateOf(emptyList<Job>()) }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                val input = SystemFileSystem.source(destination).buffered().use(Source::readString)
                val items = format.decodeFromString(ListSerializer(serializer), input)
                total = items.size
                items.map {
                    async {
                        val job = Job(id = generateNonce(), state = Job.State.Running, name = it.displayName)
                        jobs = jobs.filter { it.id != job.id } + job
                        delay(200.milliseconds)
                        try {
                            importItem(it)
                            jobs = jobs.filter { it.id != job.id } + job.copy(state = Job.State.Succeeded)
                        } catch (e: Throwable) {
                            val message = (e as? ClientRequestException)?.response?.bodyAsText() ?: e.message
                            jobs = jobs.filter { it.id != job.id } + job.copy(state = Job.State.Failed, failure = message)
                        }
                    }.also { delay(150.milliseconds) }
                }.awaitAll()
            }
        }

        ImporterStatus(total, jobs)
    }
}
