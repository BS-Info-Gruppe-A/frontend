package eu.bsinfo.data

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class Readings(val readings: List<Reading>)

@Serializable
data class Reading(
    val id: Uuid,
    val comment: String,
    val customer: Customer?,
    val dateOfReading: SerializableDate,
    val kindOfMeter: Kind,
    val meterCount: Double,
    val meterId: String,
    val substitute: Boolean
) {
    enum class Kind(val readableName: String) {
        HEIZUNG("Heizung"),
        WASSER("Wasser"),
        STROM("Strom"),
        UNBEKANNT("Unbekannt")
    }
}
