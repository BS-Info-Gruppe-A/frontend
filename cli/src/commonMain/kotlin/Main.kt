package eu.bsinfo.cli

import com.github.ajalt.clikt.completion.CompletionCommand
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import eu.bsinfo.cli.commands.CustomerCommand
import eu.bsinfo.cli.commands.ReadingCommand

fun main(args: Array<String>) = HausFix
    .subcommands(CustomerCommand, ReadingCommand, CompletionCommand())
    .main(args)

object HausFix : CliktCommand(name = "hausfix") {
    override fun run() = Unit
}
