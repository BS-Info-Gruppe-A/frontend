package eu.bsinfo.util

import platform.Foundation.NSDecimalNumber
import platform.Foundation.NSNumberFormatter

private val format = NSNumberFormatter().apply {
    minimumFractionDigits = 1u
    maximumFractionDigits = 2u
}

actual fun formatDecimal(context: PlatformContext, value: Double): String =
    format.stringFromNumber(NSDecimalNumber(double = value))!!

actual fun parseDecimal(context: PlatformContext, value: String): Double? = try {
    val decimal = format.numberFromString(value) as NSDecimalNumber
    decimal.doubleValue()
} catch (_: NumberFormatException) {
    null
}
