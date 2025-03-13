package eu.bsinfo.util

fun Double.format() = formatDecimal(this)

expect fun formatDecimal(value: Double): String
expect fun parseDecimal(value: String): Double?
