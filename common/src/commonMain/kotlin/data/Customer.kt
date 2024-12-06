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

    enum class Gender(val humanName: String) {
        M("Männlich"), D("Divers"), F("Weiblich"), U("Unbekannt")
    }
}

@Serializable
data class Customers(val customers: List<Customer>)
