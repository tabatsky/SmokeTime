package jatx.common

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun formattedZeroTime(hourLetter: String, minuteLetter: String) = 0L.format(hourLetter, minuteLetter)

val formattedMidnight: String by lazy {
    val midnight = Date().dayStart()
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    sdf.format(midnight)
}

fun Date.dayStart(): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}

fun Date.dayEnd(): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    return calendar.time
}

fun Date.monthStart(): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}

fun Date.format(hourLetter: String, minuteLetter: String): String {
    val millis = System.currentTimeMillis() - this.time
    return millis.format(hourLetter, minuteLetter)
}

fun Date.formattedTime(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(this)
}

fun Long.format(hourLetter: String, minuteLetter: String): String = this.let {
    val seconds = it / 1000
    val minutesTotal = seconds / 60
    val minutes = minutesTotal % 60
    val hours = minutesTotal / 60
    "$hours $hourLetter $minutes $minuteLetter"
}
