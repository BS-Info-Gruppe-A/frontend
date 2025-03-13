package eu.bsinfo.util

import java.text.DecimalFormat

private val format = DecimalFormat.getInstance().apply { minimumFractionDigits = 1; maximumFractionDigits = 2 }

actual fun formatDecimal(value: Double): String = format.format(value)
actual fun parseDecimal(value: String): Double? = try {
    format.parse(value).toDouble()
} catch (_: NumberFormatException) {
    null
}
