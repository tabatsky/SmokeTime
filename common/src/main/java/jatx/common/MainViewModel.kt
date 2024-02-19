package jatx.common

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class MainViewModel(
    private val applicationContext: Context
): ViewModel() {

    var showAddConfirm by mutableStateOf(false)
    var showDeleteConfirm by mutableStateOf(false)
    var basicState by mutableStateOf(BasicAppState())
    var advancedState by mutableStateOf(AdvancedAppState())

    var appearance: Appearance? by mutableStateOf(null)

    companion object {

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // Get the Application object from extras
                val application = checkNotNull(extras[APPLICATION_KEY])

                return MainViewModel(
                    application.applicationContext
                ) as T
            }
        }
    }

    fun updateFromDB() {
        updateBasicState()
        updateAdvancedState()
    }

    private fun updateBasicState() {
        viewModelScope.launch {
            val t0 = System.currentTimeMillis()
            withContext(Dispatchers.IO) {
                val date = Date()
                val totalCountForToday = AppDatabase
                    .invoke(applicationContext)
                    .smokingDao()
                    .getEventCountForTimeInterval(date.dayStart().time, date.dayEnd().time)
                    .toString()
                val agoLast = AppDatabase
                    .invoke(applicationContext)
                    .smokingDao()
                    .getLastEvent()
                    ?.let { Date(it.time) }
                    ?.format() ?: formattedZeroTime

                withContext(AndroidUiDispatcher.Main) {
                    basicState = BasicAppState(
                        agoLast = agoLast,
                        totalCountForToday = totalCountForToday
                    )
                    val t1 = System.currentTimeMillis()
                    Log.e("dt basic", (t1 - t0).toString())
                }
            }
        }
    }

    private fun updateAdvancedState() {
        viewModelScope.launch {
            val t0 = System.currentTimeMillis()
            withContext(Dispatchers.IO) {
                val allEvents = AppDatabase
                    .invoke(applicationContext)
                    .smokingDao()
                    .getAllEvents()
                val averageCountByDayAllTime = allEvents.averageCountByDayAllTime
                val averageCountByDayLastTime = allEvents.averageCountByDayLastTime
                val averageOfAverageMinutesForDayAllTime =
                    allEvents.averageOfAverageMinutesForDayAllTime
                val averageOfAverageMinutesForDayLastTime =
                    allEvents.averageOfAverageMinutesForDayLastTime
                val firstSmokingTimeForToday = allEvents.firstSmokingTimeForToday(
                    appearance?.firstTodayLabel ?: "")
                val countForCurrentMonth = allEvents.countForCurrentMonth(
                    appearance?.currentMonthLabel ?: "",
                    appearance?.packsLabel ?: "")
                val countsByDayLastTime = allEvents.countsByDayLastTime
                val averageMinutesForDayLastTime = allEvents.averageMinutesForDayLastTime

                withContext(AndroidUiDispatcher.Main) {
                    advancedState = AdvancedAppState(
                        averageCountByDayAllTime = averageCountByDayAllTime,
                        averageCountByDayLastTime = averageCountByDayLastTime,
                        averageOfAverageMinutesForDayAllTime = averageOfAverageMinutesForDayAllTime,
                        averageMinutesForDayLastTime = averageMinutesForDayLastTime,
                        firstSmokingTimeForToday = firstSmokingTimeForToday,
                        countForCurrentMonth = countForCurrentMonth,
                        countsByDayLastTime = countsByDayLastTime,
                        averageOfAverageMinutesForDayLastTime = averageOfAverageMinutesForDayLastTime
                    )
                    val t1 = System.currentTimeMillis()
                    Log.e("dt advanced", (t1 - t0).toString())
                }
            }
        }
    }

    fun newEvent() {
        viewModelScope.launch {
            AppDatabase
                .invoke(applicationContext)
                .smokingDao()
                .addEvent(SmokeEventEntity(time = System.currentTimeMillis()))
            updateFromDB()
        }
    }

    fun deleteLastEvent() {
        viewModelScope.launch {
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

