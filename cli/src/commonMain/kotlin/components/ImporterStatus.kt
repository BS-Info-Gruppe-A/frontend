package eu.bsinfo.cli.components

import androidx.compose.runtime.*
import com.jakewharton.mosaic.layout.background
import com.jakewharton.mosaic.layout.height
import com.jakewharton.mosaic.layout.padding
import com.jakewharton.mosaic.layout.size
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.text.SpanStyle
import com.jakewharton.mosaic.text.buildAnnotatedString
import com.jakewharton.mosaic.text.withStyle
import com.jakewharton.mosaic.ui.*
import com.jakewharton.mosaic.ui.Color.Companion.Black
import com.jakewharton.mosaic.ui.Color.Companion.Green
import com.jakewharton.mosaic.ui.Color.Companion.Red
import com.jakewharton.mosaic.ui.Color.Companion.White
import com.jakewharton.mosaic.ui.Color.Companion.Yellow
import eu.bsinfo.cli.command.ImportCommand
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

@Composable
fun ImporterStatus(total: Int?, jobs: List<ImportCommand.Job>) {
    Column {
        Log(jobs)
        Status(jobs)
        if (total != null) {
            Summary(total, jobs)
        } else {
            Loading {
                Text("Initializing ...")
            }
        }
    }
}

@Composable
fun Log(jobs: List<ImportCommand.Job>) {
    if (jobs.isNotEmpty()) {
        for (job in jobs) {
            JobRow(job)
        }

        Spacer(Modifier.height(1)) // Blank line
    }
}

@Composable
fun Status(jobs: List<ImportCommand.Job>) {
    val running = jobs.filter { it.state == ImportCommand.Job.State.Running }
    if (running.isNotEmpty()) {
        for (job in running) {
            JobRow(job)
        }

        Spacer(Modifier.height(1)) // Blank line
    }
}

@Composable
fun JobRow(job: ImportCommand.Job) {
    Row {
        Text(
            job.state.displayName,
            modifier = Modifier
                .background(job.state.color)
                .padding(horizontal = 1),
            color = Black,
        )

        Text(" " + job.name)
        if (job.failure != null) {
            Text(" ‣ ${job.failure}")
        }
    }
}

@Composable
private fun Summary(total: Int, jobs: List<ImportCommand.Job>) {
    val counts = jobs.groupingBy { it.state }.eachCount()
    val failed = counts[ImportCommand.Job.State.Failed] ?: 0
    val succeeded = counts[ImportCommand.Job.State.Succeeded] ?: 0
    val running = counts[ImportCommand.Job.State.Running] ?: 0

    var start by remember { mutableStateOf(TimeSource.Monotonic.markNow()) }

    Column {
        Text(
            buildAnnotatedString {
                append("Jobs: ")

                if (failed > 0) {
                    withStyle(SpanStyle(color = Red)) {
                        append("$failed failed")
                    }
                    append(", ")
                }

                if (succeeded > 0) {
                    withStyle(SpanStyle(color = Green)) {
                        append("$succeeded passed")
                    }
                    append(", ")
                }

                if (running > 0) {
                    withStyle(SpanStyle(color = Yellow)) {
                        append("$running running")
                    }
                    append(", ")
                }

                append("$total total")
            },
        )

        Elapsed(start)

        Spacer(Modifier.height(2))

        if (total > 0) {
            TestProgress(total, succeeded, failed, running)
        }
    }
}

@Composable
fun TestProgress(total: Int, succeeded: Int, failed: Int, running: Int) {
    var showRunning by remember { mutableStateOf(true) }

    val totalWidth = 40
    val failedWidth = (failed.toDouble() * totalWidth / total).toInt()
    val passedWidth = (succeeded.toDouble() * totalWidth / total).toInt()
    val runningWidth = if (showRunning) (running.toDouble() * totalWidth / total).toInt() else 0

    Row {
        TestProgressPart(Red, failedWidth)
        TestProgressPart(Green, passedWidth)
        TestProgressPart(Yellow, runningWidth)
        TestProgressPart(White, totalWidth - failedWidth - passedWidth - runningWidth)
    }
}

@Composable
fun TestProgressPart(color: Color, width: Int) {
    Spacer(Modifier.background(color).size(width, 1))
}

@Composable
private fun Elapsed(since: TimeMark) {
    var state by remember { mutableStateOf(since.elapsedNow()) }
    LaunchedEffect(state) {
        delay(1.seconds)
        state = since.elapsedNow()
    }
    Text("Time: $state")
}
