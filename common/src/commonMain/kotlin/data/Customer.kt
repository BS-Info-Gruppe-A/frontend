package eu.bsinfo.data

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class Customer(
    val id: Uuid,
    val firstName: String,
    val lastName: String,
    val birthDate: SerializableDate,
    val gender: Gender
) {
    enum class Gender {
        M, D, F
    }
}

@Serializable
data class Customers(val customers: List<Customer>)
