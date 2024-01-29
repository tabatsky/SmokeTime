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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
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
    private val ago = mutableStateOf("0 h 0 m")
    private val totalCount = mutableStateOf("0")
    private val averageCount = mutableStateOf("0")
    private val averageTime = mutableStateOf("0 h 0 m")
    private val showAddConfirm = mutableStateOf(false)
    private val showDeleteConfirm = mutableStateOf(false)

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
            averageCount = averageCount,
            averageTime = averageTime,
            onSmokeClick = {
                showAddConfirm.value = true
            },
            onSmokeLongClick = {
                showDeleteConfirm.value = true
            }
        )
        if (showAddConfirm.value) {
            Alert(
                title = {
                    Text(text = areYouSureAdd)
                },
                positiveButton = {
                    Button(
                        onClick = {
                            showAddConfirm.value = false
                            newEvent()
                        }) {
                        Text(yes)
                    }
                },
                negativeButton = {
                    Button(
                        onClick = {
                            showAddConfirm.value = false
                        }) {
                        Text(no)
                    }
                }
            )
        }
        if (showDeleteConfirm.value) {
            Alert(
                title = {
                    Text(text = areYouSureDelete)
                },
                positiveButton = {
                    Button(
                        onClick = {
                            showDeleteConfirm.value = false
                            deleteLastEvent()
                        }) {
                        Text(yes)
                    }
                },
                negativeButton = {
                    Button(
                        onClick = {
                            showDeleteConfirm.value = false
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
            totalCount.value = AppDatabase
                .invoke(applicationContext)
                .smokingDao()
                .getEventCountForTimeInterval(date.dayStart().time, date.dayEnd().time)
                .toString()
            ago.value = (AppDatabase
                .invoke(applicationContext)
                .smokingDao()
                .getLastEvent()
                ?.let { Date(it.time) }
                ?: date).format()
            val allEvents = AppDatabase
                .invoke(applicationContext)
                .smokingDao()
                .getAllEvents()
            averageCount.value = allEvents
                .groupBy { Date(it.time).dayStart() }
                .values
                .map { it.count() }
                .average()
                .toInt()
                .toString()
            averageTime.value = (allEvents
                .indices
                .takeIf {
                    it.count() >= 2
                }
                ?.drop(1)
                ?.map { index ->
                    allEvents[index - 1].time - allEvents[index].time
                }
                ?.filter {
                    it < 6 * 60 * 60 * 1000L
                }
                ?.takeIf {
                    it.isNotEmpty()
                }
                ?.average()
                ?.toLong() ?: 0L)
                .format()
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
    ago: MutableState<String>,
    totalCount: MutableState<String>,
    averageCount: MutableState<String>,
    averageTime: MutableState<String>,
    onSmokeClick: () -> Unit,
    onSmokeLongClick: () -> Unit
) {
    SmokeTimeTheme {
        VerticalPager(pageCount = 2) { page ->
            when (page) {
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colors.background),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = totalCount.value)
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
                        Text(text = ago.value)
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
                        Text(text = averageCount.value)
                        Text(text = averageTime.value)
                    }
                }
            }
        }
    }
}
