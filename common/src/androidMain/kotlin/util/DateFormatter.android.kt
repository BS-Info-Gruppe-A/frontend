package eu.bsinfo.util

import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import java.util.Date


actual fun formatLocalDate(context: PlatformContext, date: LocalDate): String {
    val format = DateFormat.getDateFormat(context.applicationContext)
    return format.format(Date(date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()))
}
