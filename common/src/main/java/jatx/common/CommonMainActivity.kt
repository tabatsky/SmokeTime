@file:OptIn(ExperimentalFoundationApi::class, ExperimentalFoundationApi::class)

package jatx.common

import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Alert
import jatx.common.theme.Purple200
import jatx.common.theme.SmokeTimeTheme
import kotlinx.coroutines.launch
import java.util.Date

open class CommonMainActivity : ComponentActivity() {
    private var ago by mutableStateOf(formattedZeroTime)
    private var totalCount by mutableStateOf("0")
    private var averageCountPerDay by mutableStateOf("0")
    private var averageTimePerDay by mutableStateOf(formattedZeroTime)
    private var firstSmokingTimeForToday by mutableStateOf(formattedMidnight)
    private var countForCurrentMonth by mutableStateOf(0.formattedCigaretteCount)
    private var showAddConfirm by mutableStateOf(false)
    private var showDeleteConfirm by mutableStateOf(false)

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
            averageCount = averageCountPerDay,
            averageTime = averageTimePerDay,
            firstSmokingTime = firstSmokingTimeForToday,
            perCurrentMonth = countForCurrentMonth,
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
            averageCountPerDay = allEvents.averageCountPerDay
            averageTimePerDay = allEvents.averageTimePerDay
            firstSmokingTimeForToday = allEvents.firstSmokingTimeForToday
            countForCurrentMonth = allEvents.countForCurrentMonth
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

@Composable
fun MainScreen(
    buttonLabel: String,
    ago: String,
    totalCount: String,
    averageCount: String,
    averageTime: String,
    firstSmokingTime: String,
    perCurrentMonth: String,
    onSmokeClick: () -> Unit,
    onSmokeLongClick: () -> Unit
) {
    SmokeTimeTheme {
        VerticalPager(pageCount = 3) { page ->
            when (page) {
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colors.background),
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
                            .background(MaterialTheme.colors.background),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = averageCount)
                        Text(text = averageTime)
                    }
                }
                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colors.background),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = firstSmokingTime)
                        Text(text = perCurrentMonth)
                    }
                }
            }
        }
    }
}
