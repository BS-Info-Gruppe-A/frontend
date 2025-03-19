package eu.bsinfo.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands
import eu.bsinfo.cli.command.ExportCommand
import eu.bsinfo.cli.command.ImportCommand
import eu.bsinfo.data.Customer
import kotlinx.serialization.KSerializer

val CustomerCommand: CliktCommand = Command.subcommands(
    CustomerImportCommand,
    CustomerExportCommand
)

private object Command : CliktCommand("customers") {
    override fun help(context: Context): String = "M2anages customers"
    override fun run() = Unit
}

private object CustomerImportCommand : ImportCommand<Customer>() {
    override val serializer: KSerializer<Customer> = Customer.serializer()

    override fun help(context: Context): String = """Exports all customers to the desired format"""

    override suspend fun importItem(item: Customer) = client.createCustomer(item)
}

private object CustomerExportCommand : ExportCommand<Customer>() {
    override val serializer: KSerializer<Customer> = Customer.serializer()

    override fun help(context: Context): String = """Imports all customers from the desired format"""

    override suspend fun retrieveItems(): List<Customer> = client.getCustomers().customers
}
