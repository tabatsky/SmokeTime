package jatx.common

import java.util.Date

data class BasicAppState(
    val agoLast: String = formattedZeroTime,
    val totalCountForToday: String = "0"
)

data class AdvancedAppState(
    val averageCountByDayAllTime: Int = 0,
    val averageCountByDayLastTime: Int = 0,
    val averageOfAverageMinutesForDayAllTime: Int = 0,
    val averageOfAverageMinutesForDayLastTime: Int = 0,
    val firstSmokingTimeForToday: String = formattedMidnight,
    val countForCurrentMonth: String = 0.formattedCigaretteCount,
    val countsByDayLastTime: List<Pair<Date, Int>> = listOf(Date() to 0),
    val averageMinutesForDayLastTime: List<Pair<Date, Int>> = listOf(Date() to 0)
)
