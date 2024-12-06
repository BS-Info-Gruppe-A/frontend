package eu.bsinfo.rest

import eu.bsinfo.data.Customers
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

class Client {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }

        defaultRequest {
            url.takeFrom("http://localhost:8080")
        }
    }

    suspend fun getCustomers() = client.get("/customers")
        .body<Customers>()
}
