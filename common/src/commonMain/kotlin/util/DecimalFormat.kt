package eu.bsinfo.util

fun Double.format(context: PlatformContext) = formatDecimal(context, this)

expect fun formatDecimal(context: PlatformContext, value: Double): String
expect fun parseDecimal(context: PlatformContext, value: String): Double?
