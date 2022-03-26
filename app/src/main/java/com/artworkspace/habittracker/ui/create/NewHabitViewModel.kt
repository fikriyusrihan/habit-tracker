package com.artworkspace.habittracker.ui.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artworkspace.habittracker.data.HabitRepository
import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.data.entity.ReminderTime
import com.artworkspace.habittracker.data.entity.WeeklyTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class NewHabitViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private var _startAtTimestamp = MutableLiveData<Long>()
    val startAtTimestamp: LiveData<Long> = _startAtTimestamp

    private var _reminder = MutableLiveData<ReminderTime>()
    val reminder: LiveData<ReminderTime> = _reminder

    init {
        val calendar = Calendar.getInstance()
        val defaultReminderHour = 9
        val defaultReminderMinute = 0

        _startAtTimestamp.value = calendar.timeInMillis
        _reminder.value = ReminderTime(
            hour = defaultReminderHour,
            minute = defaultReminderMinute
        )
    }

    fun saveNewHabit(habit: Habit, weeklyTargetArray: BooleanArray, dailyTarget: Int) {
        viewModelScope.launch {
            val habitId = habitRepository.insertHabit(habit)

            val weeklyTarget = WeeklyTarget(
                id = null,
                habitId = habitId,
                mon = weeklyTargetArray[0],
                tue = weeklyTargetArray[1],
                wed = weeklyTargetArray[2],
                thu = weeklyTargetArray[3],
                fri = weeklyTargetArray[4],
                sat = weeklyTargetArray[5],
                sun = weeklyTargetArray[6]
            )
            habitRepository.insertWeeklyTarget(weeklyTarget)
        }
    }

    fun setStartAtTimestamp(timeInMillis: Long) {
        _startAtTimestamp.value = timeInMillis
    }

    fun setReminderTime(time: ReminderTime) {
        _reminder.value = time
    }
}