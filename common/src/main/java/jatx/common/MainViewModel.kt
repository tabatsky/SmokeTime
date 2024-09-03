package jatx.common

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity.ALARM_SERVICE
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
import java.util.Calendar
import java.util.Date
import kotlin.properties.Delegates

class MainViewModel(
    private val applicationContext: Context
): ViewModel() {

    var showAddConfirm by mutableStateOf(false)
    var showDeleteConfirm by mutableStateOf(false)
    var basicState by mutableStateOf(BasicAppState())
    var advancedState by mutableStateOf(AdvancedAppState())

    var appearance by Delegates.notNull<Appearance>()

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

    fun updateBasicState() {
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
                    ?.format(appearance.hourLetter, appearance.minuteLetter)
                    ?: formattedZeroTime(appearance.hourLetter, appearance.minuteLetter)

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
                    appearance.firstTodayLabel)
                val countForCurrentMonth = allEvents.countForCurrentMonth(
                    appearance.currentMonthLabel,
                    appearance.packsLabel)
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
            resetAlarm(35)
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

    fun resetAlarm(minutes: Int) {
        cancelAlarm(applicationContext)
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hour < 18) {
            setAlarm(applicationContext, minutes)
        }
    }
}

private fun cancelAlarm(context: Context) {
    val intent = Intent(context, AlarmReceiver::class.java)
    val sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(sender)
}

private fun setAlarm(context: Context, minutes: Int) {
    val am = context.getSystemService(ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java)
    val pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    val dt = minutes * 60 * 1000L
    am[AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + dt] = pi
}

