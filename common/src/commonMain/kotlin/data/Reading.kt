package eu.bsinfo.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
) {
    enum class Kind(override val humanName: String) : ReadableEnum {
        HEIZUNG("Heizung"),
        WASSER("Wasser"),
        STROM("Strom"),
        UNBEKANNT("Unbekannt")
    }
}

val Reading.Kind.icon: ImageVector
    get() = when (this) {
        Reading.Kind.HEIZUNG -> Icons.Default.Thermostat
        Reading.Kind.WASSER -> Icons.Default.WaterDrop
        Reading.Kind.STROM -> Icons.Default.ElectricMeter
        Reading.Kind.UNBEKANNT -> Icons.Default.DeviceUnknown
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
