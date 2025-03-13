package eu.bsinfo.util

import kotlinx.datetime.LocalDate

expect fun formatLocalDate(context: PlatformContext, date: LocalDate): String
