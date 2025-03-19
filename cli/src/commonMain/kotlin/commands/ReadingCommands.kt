package eu.bsinfo.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands
import eu.bsinfo.cli.command.ExportCommand
import eu.bsinfo.cli.command.ImportCommand
import eu.bsinfo.data.Reading
import kotlinx.serialization.KSerializer

val ReadingCommand: CliktCommand = ReadingRootCommand.subcommands(
    ReadingImportCommand,
    ReadingExportCommand
)

private object ReadingRootCommand : CliktCommand("readings") {
    override fun help(context: Context): String = """Manages readings"""
    override fun run() = Unit
}

private object ReadingImportCommand : ImportCommand<Reading>() {
    override val serializer: KSerializer<Reading> = Reading.serializer()

    override fun help(context: Context): String = "Imports all readings from the desired format"

    override suspend fun importItem(item: Reading) = client.createReading(item)
}

private object ReadingExportCommand : ExportCommand<Reading>() {
    override val serializer: KSerializer<Reading> = Reading.serializer()

    override fun help(context: Context): String = "Exports all readings to the desired format"

    override suspend fun retrieveItems(): List<Reading> = client.getReadings().readings
}
