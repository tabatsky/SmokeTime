@file:OptIn(ExperimentalFoundationApi::class, ExperimentalFoundationApi::class)

package jatx.common

import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Alert
import jatx.common.theme.Purple200
import jatx.common.theme.SmokeTimeTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val lastDays = 10

const val labelUp = "▲"
const val labelDown = "▼"
const val labelSquare = "◼"

open class CommonMainActivity : ComponentActivity() {
    private var ago by mutableStateOf(formattedZeroTime)
    private var totalCount by mutableStateOf("0")
    private var averageCountPerDayAllTime by mutableStateOf(0)
    private var averageCountPerDayLastTime by mutableStateOf(0)
    private var averageMinutesPerDayAllTime by mutableStateOf(0)
    private var averageMinutesPerDayLastTime by mutableStateOf(0)
    private var firstSmokingTimeForToday by mutableStateOf(formattedMidnight)
    private var countForCurrentMonth by mutableStateOf(0.formattedCigaretteCount)
    private var showAddConfirm by mutableStateOf(false)
    private var showDeleteConfirm by mutableStateOf(false)
    private var countsByDay by mutableStateOf(listOf(Date() to 0))
    private var averageMinutesByDay by mutableStateOf(listOf(Date() to 0))

    override fun onResume() {
        super.onResume()
        updateFromDB()
    }

    @Composable
    protected fun Content(
        buttonLabel: String
    ) {
        MainScreen(
            buttonLabel = buttonLabel,
            ago = ago,
            totalCount = totalCount,
            averageCountAllTime = averageCountPerDayAllTime,
            averageCountLastTime = averageCountPerDayLastTime,
            averageMinutesAllTime = averageMinutesPerDayAllTime,
            averageMinutesLastTime = averageMinutesPerDayLastTime,
            firstSmokingTime = firstSmokingTimeForToday,
            perCurrentMonth = countForCurrentMonth,
            countsByDay = countsByDay,
            averageMinutesByDay = averageMinutesByDay,
            onSmokeClick = {
                showAddConfirm = true
            },
            onSmokeLongClick = {
                showDeleteConfirm = true
            }
        )
        if (showAddConfirm) {
            Alert(
                title = {
                    Text(text = areYouSureAdd)
                },
                positiveButton = {
                    Button(
                        onClick = {
                            showAddConfirm = false
                            newEvent()
                        }) {
                        Text(yes)
                    }
                },
                negativeButton = {
                    Button(
                        onClick = {
                            showAddConfirm = false
                        }) {
                        Text(no)
                    }
                }
            )
        }
        if (showDeleteConfirm) {
            Alert(
                title = {
                    Text(text = areYouSureDelete)
                },
                positiveButton = {
                    Button(
                        onClick = {
                            showDeleteConfirm = false
                            deleteLastEvent()
                        }) {
                        Text(yes)
                    }
                },
                negativeButton = {
                    Button(
                        onClick = {
                            showDeleteConfirm = false
                        }) {
                        Text(no)
                    }
                }
            )
        }
    }

    private fun updateFromDB() {
        lifecycleScope.launch {
            val date = Date()
            totalCount = AppDatabase
                .invoke(applicationContext)
                .smokingDao()
                .getEventCountForTimeInterval(date.dayStart().time, date.dayEnd().time)
                .toString()
            ago = AppDatabase
                .invoke(applicationContext)
                .smokingDao()
                .getLastEvent()
                ?.let { Date(it.time) }
                ?.format() ?: formattedZeroTime
            val allEvents = AppDatabase
                .invoke(applicationContext)
                .smokingDao()
                .getAllEvents()
            averageCountPerDayAllTime = allEvents.averageCountPerDayAllTime
            averageCountPerDayLastTime = allEvents.averageCountPerDayLastTime(lastDays)
            averageMinutesPerDayAllTime = allEvents.averageMinutesPerDayAllTime
            averageMinutesPerDayLastTime = allEvents.averageMinutesPerDayLastTime(lastDays)
            firstSmokingTimeForToday = allEvents.firstSmokingTimeForToday
            countForCurrentMonth = allEvents.countForCurrentMonth
            countsByDay = allEvents.countsByDay.takeLast(lastDays)
            averageMinutesByDay = allEvents.averageMinutes.takeLast(lastDays)
        }
    }

    private fun newEvent() {
        lifecycleScope.launch {
            AppDatabase
                .invoke(applicationContext)
                .smokingDao()
                .addEvent(SmokeEventEntity(time = System.currentTimeMillis()))
            updateFromDB()
        }
    }

    private fun deleteLastEvent() {
        lifecycleScope.launch {
            with (AppDatabase.invoke(applicationContext).smokingDao()) {
                val lastEvent = getLastEvent()
                lastEvent?.let {
                    deleteById(it.id)
                }
            }
            updateFromDB()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    buttonLabel: String,
    ago: String,
    totalCount: String,
    averageCountAllTime: Int,
    averageCountLastTime: Int,
    averageMinutesAllTime: Int,
    averageMinutesLastTime: Int,
    firstSmokingTime: String,
    perCurrentMonth: String,
    countsByDay: List<Pair<Date, Int>>,
    averageMinutesByDay: List<Pair<Date, Int>>,
    onSmokeClick: () -> Unit,
    onSmokeLongClick: () -> Unit
) {
    SmokeTimeTheme {
        VerticalPager(pageCount = 4) { page ->
            when (page) {
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = totalCount)
                        Text(
                            text = buttonLabel,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .size(width = 64.dp, height = 64.dp)
                                .combinedClickable(
                                    onClick = {
                                        onSmokeClick()
                                    },
                                    onLongClick = {
                                        onSmokeLongClick()
                                    }
                                )
                                .padding(16.dp)
                                .wrapContentHeight()
                                .drawBehind {
                                    drawCircle(
                                        color = Purple200,
                                        radius = this.size.maxDimension
                                    )
                                },
                        )
                        Text(text = ago)
                    }
                }
                1 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val sdf = SimpleDateFormat("dd.MM", Locale.getDefault())
                        val dailyBars = averageMinutesByDay
                            .takeLast(10).map {
                                val label = sdf.format(it.first)
                                BarChartItem(value = it.second.toFloat(), label = label, color = Color.Blue)
                            }
                        val maxValue =
                            (averageMinutesByDay.takeLast(10).maxOf { it.second } / 5 + 1) * 5

                        Spacer(modifier = Modifier
                            .weight(1.0f))

                        JatxBarChart(
                            modifier = Modifier
                                .width(180.dp)
                                .height(140.dp),
                            backgroundColor = Color.Black,
                            lineColor = Color.White,
                            textColor = Color.White,
                            textSize = 6.sp,
                            maxValue = maxValue.toFloat(),
                            valueStep = 5f,
                            items = dailyBars
                        )

                        Spacer(modifier = Modifier
                            .weight(0.5f))

                        val (label, color) = if (averageMinutesLastTime > averageMinutesAllTime) {
                            labelUp to Color.Green
                        } else if (averageMinutesLastTime < averageMinutesAllTime) {
                            labelDown to Color.Red
                        } else {
                            labelSquare to Color.Gray
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text(text = "${averageMinutesLastTime}m", color = color)
                            Text(text = label, color = color)
                            Text(text = "${averageMinutesAllTime}m")
                        }

                        Spacer(modifier = Modifier
                            .weight(1.0f))
                    }
                }
                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val sdf = SimpleDateFormat("dd.MM", Locale.getDefault())
                        val dailyBars = countsByDay
                            .takeLast(10).map {
                                val label = sdf.format(it.first)
                                BarChartItem(value = it.second.toFloat(), label = label, color = Color.Blue)
                            }
                        val maxValue =
                            (countsByDay.takeLast(10).maxOf { it.second } / 5 + 1) * 5

                        Spacer(modifier = Modifier
                            .weight(1.0f))

                        JatxBarChart(
                            modifier = Modifier
                                .width(180.dp)
                                .height(140.dp),
                            backgroundColor = Color.Black,
                            lineColor = Color.White,
                            textColor = Color.White,
                            textSize = 6.sp,
                            maxValue = maxValue.toFloat(),
                            valueStep = 5f,
                            items = dailyBars
                        )

                        Spacer(modifier = Modifier
                            .weight(0.5f))

                        val (label, color) = if (averageCountLastTime > averageCountAllTime) {
                            labelUp to Color.Red
                        } else if (averageCountLastTime < averageCountAllTime) {
                            labelDown to Color.Green
                        } else {
                            labelSquare to Color.Gray
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text(text = averageCountLastTime.toString(), color = color)
                            Text(text = label, color = color)
                            Text(text = averageCountAllTime.toString())
                        }

                        Spacer(modifier = Modifier
                            .weight(1.0f))
                    }
                }
                3 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = firstSmokingTime, textAlign = TextAlign.Center)
                        Text(text = perCurrentMonth, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}
