package eu.bsinfo.util

import java.text.DecimalFormat

private val format = DecimalFormat.getInstance()

actual fun formatDecimal(value: Double): String = format.format(value)
