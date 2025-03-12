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
    val fullName get() = "$firstName $lastName"

    enum class Gender(override val humanName: String): ReadableEnum {
        M("Männlich"), D("Divers"), W("Weiblich"), U("Unbekannt")
    }
}

@Serializable
data class UpdatableCustomer(
    val id: Uuid,
    val firstName: String? = null,
    val lastName: String? = null,
    val gender: Customer.Gender? = null
)

@Serializable
data class Customers(val customers: List<Customer>)
