package jatx.common

import android.util.Log
import java.util.Date
import kotlin.math.roundToInt

const val lastDaysCount = 10

private val lastDays: List<Date>
    get() {
        val result = arrayListOf<Date>()
        val todayStart = Date().dayStart()
        val millisPerDay = 24 * 60 * 60 * 1000L
        for (ago in 0 until lastDaysCount) {
            val date = Date()
            date.time = todayStart.time - millisPerDay * ago
            result.add(date)
        }
        return result.reversed()
    }

val List<SmokeEventEntity>.averageCountByDayAllTime: Int
    get() = this
        .countsByDay
        .values
        .filter { it > 0 }
        .takeIf { it.isNotEmpty() }
        ?.average()
        ?.roundToInt() ?: 0

val List<SmokeEventEntity>.averageCountByDayLastTime: Int
    get() {
        val theLastDays = lastDays
        val theCountsByDay = this.countsByDay
        return theLastDays
            .map { date ->
                theCountsByDay[date] ?: 0
            }
            .dropLast(1)
            .filter { it > 0 }
            .takeIf { it.size > 1 }
            ?.average()
            ?.roundToInt() ?: 0
    }

val List<SmokeEventEntity>.countsByDayLastTime: List<Pair<Date, Int>>
    get() {
        val theLastDays = lastDays
        val theCountsByDay = this.countsByDay
        return theLastDays
            .map { date ->
                val count = theCountsByDay[date] ?: 0
                date to count
            }
    }
private val List<SmokeEventEntity>.countsByDay: Map<Date, Int>
    get() = this
        .groupBy { Date(it.time).dayStart() }
        .toList()
        .associate {
            val dayStart = it.first
            val count = it.second.count()
            dayStart to count
        }

val List<SmokeEventEntity>.averageOfAverageMinutesForDayAllTime: Int
    get() = this
        .averageMinutesForDay
        .values
        .filter { it > 0 }
        .takeIf { it.isNotEmpty() }
        ?.average()
        ?.roundToInt() ?: 0

val List<SmokeEventEntity>.averageOfAverageMinutesForDayLastTime: Int
    get() {
        val theLastDays = lastDays
        val theAverageMinutesForDay = this.averageMinutesForDay
        return theLastDays
            .map { date ->
                theAverageMinutesForDay[date] ?: 0
            }
            .filter { it > 0 }
            .takeIf { it.isNotEmpty() }
            ?.average()
            ?.roundToInt() ?: 0
    }

val List<SmokeEventEntity>.averageMinutesForDayLastTime: List<Pair<Date, Int>>
    get() {
        val theLastDays = lastDays
        val theAverageMinutesForDay = this.averageMinutesForDay
        return theLastDays
            .map {  date ->
                val average = theAverageMinutesForDay[date] ?: 0
                date to average
            }
    }

private val List<SmokeEventEntity>.averageMinutesForDay: Map<Date, Int>
    get() = this
        .groupBy { Date(it.time).dayStart() }
        .toList()
        .associate {
            val dayStart = it.first
            val allByDay = it.second
            val averageMinutes = allByDay
                .indices
                .takeIf {
                    it.count() >= 2
                }
                ?.drop(1)
                ?.map { index ->
                    allByDay[index - 1].time - allByDay[index].time
                }
                ?.filter {
                    it < 6 * 60 * 60 * 1000L
                }
                ?.takeIf {
                    it.isNotEmpty()
                }
                ?.average()
                ?.toLong()
                ?.let { (it / (1000 * 60)).toInt() } ?: 0
            dayStart to averageMinutes
        }

fun List<SmokeEventEntity>.firstSmokingTimeForToday(firstToday: String): String {
        val time = this
            .filter { Date(it.time).dayStart() == Date().dayStart() }
            .takeIf { it.isNotEmpty() }
            ?.minBy { it.time }
            ?.let { Date(it.time) }
            ?.formattedTime() ?: formattedMidnight
        return "$firstToday\n$time"
    }

fun List<SmokeEventEntity>.countForCurrentMonth(currentMonthLabel: String, packsLabel: String) = this
        .count { Date(it.time).monthStart() == Date().monthStart() }
        .formattedCigaretteCount(currentMonthLabel, packsLabel)

fun Int.formattedCigaretteCount(currentMonthLabel: String, packsLabel: String) = this
        .let {
            val packs = it / 20
            val units = it % 20
            "$currentMonthLabel\n$packs $packsLabel + $units"
        }

