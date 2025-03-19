package eu.bsinfo.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector

val Reading.Kind.icon: ImageVector
    get() = when (this) {
        Reading.Kind.HEIZUNG -> Icons.Default.Thermostat
        Reading.Kind.WASSER -> Icons.Default.WaterDrop
        Reading.Kind.STROM -> Icons.Default.ElectricMeter
        Reading.Kind.UNBEKANNT -> Icons.Default.DeviceUnknown
    }
