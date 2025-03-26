package eu.bsinfo.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.uuid.Uuid

@Serializable
data class Readings(val readings: List<Reading>)

@Serializable
data class Reading(
    val id: Uuid,
    val comment: String?,
    val customer: Customer?,
    @SerialName("dateOfReading")
    val date: SerializableDate,
    @SerialName("kindOfMeter")
    val kind: Kind,
    val meterCount: Double,
    val meterId: String,
    val substitute: Boolean
) : Identifiable {

    @Transient
    override val displayName: String = "Ablesung von $meterId"

    fun toUpdatableReading() = UpdatableReading(
        id, date, comment, meterId.toIntOrNull(), substitute, meterCount, kind
    )

    enum class Kind(override val humanName: String) : ReadableEnum {
        HEIZUNG("Heizung"),
        WASSER("Wasser"),
        STROM("Strom"),
        UNBEKANNT("Unbekannt")
    }
}

@Serializable
data class UpdatableReading(
    val id: Uuid,
    val dateOfReading: SerializableDate?,
    val comment: String?,
    val meterId: Int?,
    val substitute: Boolean?,
    @SerialName("metercount")
    val meterCount: Double?,
    val kindOfMeter: Reading.Kind?
)
