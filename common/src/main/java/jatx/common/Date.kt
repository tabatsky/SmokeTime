package jatx.common

import java.util.Calendar
import java.util.Date

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

fun Date.format(): String {
    val millis = System.currentTimeMillis() - this.time
    return millis.format()
}

fun Long.format(): String {
    val seconds = this / 1000
    val minutesTotal = seconds / 60
    val minutes = minutesTotal % 60
    val hours = minutesTotal / 60
    return "$hours h $minutes m"
}