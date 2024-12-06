package eu.bsinfo.rest

import eu.bsinfo.data.Customers
import eu.bsinfo.data.Readings
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.plugins.resources.Resources
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.serialization.kotlinx.json.*
import kotlin.uuid.Uuid

class Route {
    @Resource("/customers")
    class Customers {
        @Resource("{id}")
        data class Specific(val id: Uuid, val parent: Customers = Customers())
    }

    @Resource("/readings")
    class Readings {
        @Resource("{id}")
        data class Specific(val id: Uuid, val parent: Readings = Readings())
    }
}

class Client {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }

        install(Resources)

        defaultRequest {
            url.takeFrom("https://api.hausfix.devs-from.asia")
        }
    }

    suspend fun getCustomers() = client.get(Route.Customers()).body<Customers>()
    suspend fun deleteCustomer(id: Uuid) = client.delete(Route.Customers.Specific(id)).body<Unit>()
    suspend fun getReadings() = client.get(Route.Readings()).body<Readings>()
    suspend fun deleteReading(id: Uuid) = client.delete(Route.Readings.Specific(id)).body<Unit>()
}
