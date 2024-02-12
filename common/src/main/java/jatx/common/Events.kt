package jatx.common

import java.util.Date
import kotlin.math.roundToInt

const val lastDaysCount = 10

val lastDays: List<Date>
    get() {
        val result = arrayListOf<Date>()
        val todayStart = Date().dayStart()
        val millisPerDay = 24 * 60 * 60 * 1000L
        for (ago in 0 until 10) {
            val date = Date()
            date.time = todayStart.time - millisPerDay * ago
            result.add(date)
        }
        return result.reversed()
    }

val List<SmokeEventEntity>.averageCountPerDayAllTime: Int
    get() = this
        .countsByDay
        .map { it.second }
        .average()
        .roundToInt()

val List<SmokeEventEntity>.averageCountPerDayLastTime: Int
    get() = lastDays
        .map {  date ->
            this.countsByDay.find { it.first == date }?.second ?: 0
        }
        .filter { it > 0 }
        .average()
        .roundToInt()

val List<SmokeEventEntity>.countsByDayLastTime: List<Pair<Date, Int>>
    get() = lastDays
        .map {  date ->
            val count = this.countsByDay.find { it.first == date }?.second ?: 0
            date to count
        }

val List<SmokeEventEntity>.averageMinutesPerDayAllTime: Int
    get() = this
        .averageMinutes
        .map { it.second }
        .average()
        .roundToInt()

fun List<SmokeEventEntity>.averageMinutesPerDayLastTime(lastDays: Int) = this
    .averageMinutes
    .takeLast(lastDays)
    .map { it.second }
    .average()
    .roundToInt()

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

val List<SmokeEventEntity>.countsByDay: List<Pair<Date, Int>>
    get() = this
        .map { Date(it.time).dayStart() }
        .distinct()
        .sorted()
        .map {  dayStart ->
            val count = this
                .filter { Date(it.time).dayStart() == dayStart }
                .count()
            dayStart to count
        }

val List<SmokeEventEntity>.averageMinutes: List<Pair<Date, Int>>
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