package eu.bsinfo.rest

import eu.bsinfo.data.*
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlin.uuid.Uuid

class Route {
    @Resource("/customers")
    class Customers {
        @Resource("{id}")
        data class Specific(val id: Uuid, val parent: Customers = Customers())
    }

    @Resource("/readings")
    class Readings {
        @Resource("")
        data class Search(
            @SerialName("start") val from: SerializableDate? = null,
            @SerialName("end") val to: SerializableDate? = null,
            @SerialName("kindOfMeter") val kind: Reading.Kind? = null,
            @SerialName("customer") val customerId: Uuid? = null,
            val parent: Readings = Readings()
        )
        @Resource("{id}")
        data class Specific(val id: Uuid, val parent: Readings = Readings())
    }
}

val LOG = KotlinLogging.logger {}

class Client {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) = LOG.debug { message }
            }
            level = LogLevel.ALL
        }

        install(Resources)

        defaultRequest {
            url.takeFrom("https://api.hausfix.devs-from.asia")
        }
    }

    suspend fun getCustomers() = client.get(Route.Customers()).body<Customers>()
    suspend fun deleteCustomer(id: Uuid) = client.delete(Route.Customers.Specific(id)).body<Unit>()
    suspend fun updateCustomer(request: UpdatableCustomer) = client.put(Route.Customers()) {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body<Unit>()

    suspend fun createCustomer(request: Customer) = client.post(Route.Customers()) {
        contentType(ContentType.Application.Json)
        setBody(request)
    }

    suspend fun getReadings(
        from: SerializableDate? = null,
        to: SerializableDate? = null,
        kind: Reading.Kind? = null,
        customerId: Uuid? = null
    ) = client.get(Route.Readings.Search(from, to, kind, customerId)).body<Readings>()

    suspend fun updateReading(request: UpdatableReading) = client.put(Route.Readings()) {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body<Unit>()

    suspend fun createReading(request: Reading) = client.post(Route.Readings()) {
        contentType(ContentType.Application.Json)
        setBody(request)
    }
    suspend fun deleteReading(id: Uuid) = client.delete(Route.Readings.Specific(id)).body<Unit>()
}
