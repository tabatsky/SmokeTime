package jatx.common

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
        .map { it.second }
        .filter { it > 0 }
        .average()
        .roundToInt()

val List<SmokeEventEntity>.averageCountByDayLastTime: Int
    get() {
        val theLastDays = lastDays
        return theLastDays
            .map { date ->
                this.countsByDay.find { it.first == date }?.second ?: 0
            }
            .dropLast(1)
            .filter { it > 0 }
            .average()
            .roundToInt()
    }

val List<SmokeEventEntity>.countsByDayLastTime: List<Pair<Date, Int>>
    get() {
        val theLastDays = lastDays
        return theLastDays
            .map { date ->
                val count = this.countsByDay.find { it.first == date }?.second ?: 0
                date to count
            }
    }
private val List<SmokeEventEntity>.countsByDay: List<Pair<Date, Int>>
    get() = this
        .map { Date(it.time).dayStart() }
        .distinct()
        .sorted()
        .map {  dayStart ->
            val count = this.count { Date(it.time).dayStart() == dayStart }
            dayStart to count
        }

val List<SmokeEventEntity>.averageOfAverageMinutesForDayAllTime: Int
    get() = this
        .averageMinutesForDay
        .map { it.second }
        .filter { it > 0 }
        .average()
        .roundToInt()

val List<SmokeEventEntity>.averageOfAverageMinutesForDayLastTime: Int
    get() {
        val theLastDays = lastDays
        return theLastDays
            .map { date ->
                this.averageMinutesForDay.find { it.first == date }?.second ?: 0
            }
            .filter { it > 0 }
            .average()
            .roundToInt()
    }

val List<SmokeEventEntity>.averageMinutesForDayLastTime: List<Pair<Date, Int>>
    get() {
        val theLastDays = lastDays
        return theLastDays
            .map {  date ->
                val average = this.averageMinutesForDay.find { it.first == date }?.second ?: 0
                date to average
            }
    }

private val List<SmokeEventEntity>.averageMinutesForDay: List<Pair<Date, Int>>
    get() = this
        .map { Date(it.time).dayStart() }
        .distinct()
        .sorted()
        .map {  dayStart ->
            val allByDay = this
                .filter { Date(it.time).dayStart() == dayStart }
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

val List<SmokeEventEntity>.firstSmokingTimeForToday: String
    get() {
        val time = this
            .filter { Date(it.time).dayStart() == Date().dayStart() }
            .takeIf { it.isNotEmpty() }
            ?.minBy { it.time }
            ?.let { Date(it.time) }
            ?.formattedTime() ?: formattedMidnight
        return "First today:\n$time"
    }

val List<SmokeEventEntity>.countForCurrentMonth: String
    get() = this
        .count { Date(it.time).monthStart() == Date().monthStart() }
        .formattedCigaretteCount

val Int.formattedCigaretteCount: String
    get() = this
        .let {
            val packs = it / 20
            val units = it % 20
            "Current month:\n$packs packs + $units"
        }

