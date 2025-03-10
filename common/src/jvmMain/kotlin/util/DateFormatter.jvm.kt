package eu.bsinfo.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)

actual fun formatLocalDate(date: LocalDate): String = formatter.format(date.toJavaLocalDate())
