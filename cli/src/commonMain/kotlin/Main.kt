package eu.bsinfo.cli

import com.github.ajalt.clikt.completion.CompletionCommand
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.obj
import com.github.ajalt.clikt.core.registerCloseable
import com.github.ajalt.clikt.core.subcommands
import eu.bsinfo.cli.commands.CustomerCommand
import eu.bsinfo.cli.commands.ReadingCommand
import eu.bsinfo.data.Client

fun main(args: Array<String>) = HausFix
    .subcommands(CustomerCommand, ReadingCommand, CompletionCommand())
    .main(args)

object HausFix : CliktCommand(name = "hausfix") {
    private val client = Client()

    override fun run() {
        currentContext.obj = currentContext.registerCloseable(client)
    }
}
