package jatx.common

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Alert
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val labelUp = "▲"
const val labelDown = "▼"
const val labelSquare = "◼"

val Purple200 = Color(0xFFBB86FC)

@Composable
fun Content(
    appearance: Appearance
) {
    val viewModel: MainViewModel = viewModel()

    LaunchedEffect(Unit) {
        while (true) {
            delay(10000L)
            viewModel.updateBasicState()
        }
    }

    with(viewModel) {
        MainScreen(
            appearance = appearance,
            agoLast = basicState.agoLast,
            totalCountForToday = basicState.totalCountForToday,
            averageCountByDayAllTime = advancedState.averageCountByDayAllTime,
            averageCountByDayLastTime = advancedState.averageCountByDayLastTime,
            averageOfAverageMinutesForDayAllTime = advancedState.averageOfAverageMinutesForDayAllTime,
            averageOfAverageMinutesForDayLastTime = advancedState.averageOfAverageMinutesForDayLastTime,
            firstSmokingTimeForToday = advancedState.firstSmokingTimeForToday,
            countForCurrentMonth = advancedState.countForCurrentMonth,
            countsByDayLastTime = advancedState.countsByDayLastTime,
            averageMinutesForDayLastTime = advancedState.averageMinutesForDayLastTime,
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
                    Text(text = appearance.areYouSureAdd)
                },
                positiveButton = {
                    Button(
                        onClick = {
                            showAddConfirm = false
                            newEvent()
                        }) {
                        Text(appearance.yes)
                    }
                },
                negativeButton = {
                    Button(
                        onClick = {
                            showAddConfirm = false
                        }) {
                        Text(appearance.no)
                    }
                }
            )
        }
        if (showDeleteConfirm) {
            Alert(
                title = {
                    Text(text = appearance.areYouSureDelete)
                },
                positiveButton = {
                    Button(
                        onClick = {
                            showDeleteConfirm = false
                            deleteLastEvent()
                        }) {
                        Text(appearance.yes)
                    }
                },
                negativeButton = {
                    Button(
                        onClick = {
                            showDeleteConfirm = false
                        }) {
                        Text(appearance.no)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainScreen(
    appearance: Appearance,
    agoLast: String,
    totalCountForToday: String,
    averageCountByDayAllTime: Int,
    averageCountByDayLastTime: Int,
    averageOfAverageMinutesForDayAllTime: Int,
    averageOfAverageMinutesForDayLastTime: Int,
    firstSmokingTimeForToday: String,
    countForCurrentMonth: String,
    countsByDayLastTime: List<Pair<Date, Int>>,
    averageMinutesForDayLastTime: List<Pair<Date, Int>>,
    onSmokeClick: () -> Unit,
    onSmokeLongClick: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 })
    VerticalPager(state = pagerState) { page ->
        when (page) {
            0 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "${appearance.lastLabel}\n$agoLast", textAlign = TextAlign.Center)
                    Text(
                        text = appearance.buttonLabel,
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
                    Text(text = "${appearance.todayLabel}\n$totalCountForToday", textAlign = TextAlign.Center)
                }
            }

            1 -> {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    val W = this.maxWidth
                    val H = this.maxHeight
                    Log.e("W x H", "$W x $H")

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val sdf = SimpleDateFormat("dd.MM", Locale.getDefault())
                        val dailyBars = averageMinutesForDayLastTime
                            .takeLast(10).map {
                                val label = sdf.format(it.first)
                                BarChartItem(
                                    value = it.second.toFloat(),
                                    label = label,
                                    color = Color.Blue
                                )
                            }
                        val maxValue =
                            (averageMinutesForDayLastTime.takeLast(10).maxOf { it.second } / 5 + 1) * 5

                        Spacer(
                            modifier = Modifier
                                .weight(1.0f)
                        )

                        JatxBarChart(
                            modifier = Modifier
                                .width(W * 180f / 233f)
                                .height(H * 140f / 233f),
                            backgroundColor = Color.Black,
                            lineColor = Color.White,
                            textColor = Color.White,
                            textSize = 6.sp,
                            maxValue = maxValue.toFloat(),
                            valueStep = 5f,
                            items = dailyBars
                        )

                        Spacer(
                            modifier = Modifier
                                .weight(0.5f)
                        )

                        val (label, color) = if (averageOfAverageMinutesForDayLastTime > averageOfAverageMinutesForDayAllTime) {
                            labelUp to Color.Green
                        } else if (averageOfAverageMinutesForDayLastTime < averageOfAverageMinutesForDayAllTime) {
                            labelDown to Color.Red
                        } else {
                            labelSquare to Color.Gray
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text(text = "${averageOfAverageMinutesForDayLastTime}m", color = color)
                            Text(text = label, color = color)
                            Text(text = "${averageOfAverageMinutesForDayAllTime}m")
                        }

                        Spacer(
                            modifier = Modifier
                                .weight(1.0f)
                        )
                    }
                }
            }

            2 -> {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    val W = this.maxWidth
                    val H = this.maxHeight
                    Log.e("W x H", "$W x $H")

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val sdf = SimpleDateFormat("dd.MM", Locale.getDefault())
                        val dailyBars = countsByDayLastTime
                            .takeLast(10).map {
                                val label = sdf.format(it.first)
                                BarChartItem(
                                    value = it.second.toFloat(),
                                    label = label,
                                    color = Color.Blue
                                )
                            }
                        val maxValue =
                            (countsByDayLastTime.takeLast(10).maxOf { it.second } / 5 + 1) * 5

                        Spacer(
                            modifier = Modifier
                                .weight(1.0f)
                        )

                        JatxBarChart(
                            modifier = Modifier
                                .width(W * 180f / 233f)
                                .height(H * 140f / 233f),
                            backgroundColor = Color.Black,
                            lineColor = Color.White,
                            textColor = Color.White,
                            textSize = 6.sp,
                            maxValue = maxValue.toFloat(),
                            valueStep = 5f,
                            items = dailyBars
                        )

                        Spacer(
                            modifier = Modifier
                                .weight(0.5f)
                        )

                        val (label, color) = if (averageCountByDayLastTime > averageCountByDayAllTime) {
                            labelUp to Color.Red
                        } else if (averageCountByDayLastTime < averageCountByDayAllTime) {
                            labelDown to Color.Green
                        } else {
                            labelSquare to Color.Gray
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text(text = averageCountByDayLastTime.toString(), color = color)
                            Text(text = label, color = color)
                            Text(text = averageCountByDayAllTime.toString())
                        }

                        Spacer(
                            modifier = Modifier
                                .weight(1.0f)
                        )
                    }
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
                    Text(text = firstSmokingTimeForToday, textAlign = TextAlign.Center)
                    Text(text = countForCurrentMonth, textAlign = TextAlign.Center)
                }
            }
        }
    }
}
