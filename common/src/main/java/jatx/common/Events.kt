package jatx.common

import java.util.Date

val List<SmokeEventEntity>.averageCountPerDay: String
    get() = this
        .groupBy { Date(it.time).dayStart() }
        .values
        .map { it.count() }
        .average()
        .toInt()
        .toString()

val List<SmokeEventEntity>.averageTimePerDay: String
    get() = this
        .indices
        .takeIf {
            it.count() >= 2
        }
        ?.drop(1)
        ?.map { index ->
            this[index - 1].time - this[index].time
        }
        ?.filter {
            it < 6 * 60 * 60 * 1000L
        }
        ?.takeIf {
            it.isNotEmpty()
        }
        ?.average()
        ?.toLong()
        ?.format() ?: formattedZeroTime

val List<SmokeEventEntity>.firstSmokingTimeForToday: String
    get() = this
        .filter { Date(it.time).dayStart() == Date().dayStart() }
        .takeIf { it.isNotEmpty() }
        ?.minBy { it.time }
        ?.let { Date(it.time) }
        ?.formattedTime() ?: formattedMidnight

val List<SmokeEventEntity>.countForCurrentMonth: String
    get() = this
        .count { Date(it.time).monthStart() == Date().monthStart() }
        .formattedCigaretteCount

val Int.formattedCigaretteCount: String
    get() = this
        .let {
            val packs = it / 20
            val units = it % 20
            "$packs; $units"
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

val List<SmokeEventEntity>.averageMinutesByDay: List<Pair<Date, Int>>
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