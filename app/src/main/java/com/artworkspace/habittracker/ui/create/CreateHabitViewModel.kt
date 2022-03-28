package com.artworkspace.habittracker.ui.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artworkspace.habittracker.data.HabitRepository
import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.data.entity.Record
import com.artworkspace.habittracker.data.entity.ReminderTime
import com.artworkspace.habittracker.data.entity.WeeklyTarget
import com.artworkspace.habittracker.utils.todayTimestamp
import com.maltaisn.icondialog.data.Icon
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateHabitViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private var _startAtTimestamp = MutableLiveData<Long>()
    val startAtTimestamp: LiveData<Long> = _startAtTimestamp

    private var _reminder = MutableLiveData<ReminderTime>()
    val reminder: LiveData<ReminderTime> = _reminder

    private var _checkedDays = MutableLiveData<BooleanArray>()
    val checkedDays: LiveData<BooleanArray> = _checkedDays

    private var _icon = MutableLiveData<Icon?>()
    val icon: LiveData<Icon?> = _icon

    init {
        val defaultReminderHour = 9
        val defaultReminderMinute = 0

        _startAtTimestamp.value = todayTimestamp
        _reminder.value = ReminderTime(
            hour = defaultReminderHour,
            minute = defaultReminderMinute
        )
        _checkedDays.value = booleanArrayOf(true, true, true, true, true, true, true)
    }

    fun saveNewHabit(habit: Habit, weeklyTargetArray: BooleanArray?) {
        viewModelScope.launch {
            val habitId = habitRepository.insertHabit(habit)

            val weeklyTarget = WeeklyTarget(
                id = null,
                habitId = habitId,
                mon = weeklyTargetArray?.get(0) ?: true,
                tue = weeklyTargetArray?.get(1) ?: true,
                wed = weeklyTargetArray?.get(2) ?: true,
                thu = weeklyTargetArray?.get(3) ?: true,
                fri = weeklyTargetArray?.get(4) ?: true,
                sat = weeklyTargetArray?.get(5) ?: true,
                sun = weeklyTargetArray?.get(6) ?: true
            )

            val record = Record(
                id = null,
                habitId = habitId,
                isChecked = false,
                timestamp = habit.startAt
            )

            habitRepository.insertWeeklyTarget(weeklyTarget)
            habitRepository.insertDailyRecord(record)
        }
    }

    fun setStartAtTimestamp(timeInMillis: Long) {
        _startAtTimestamp.value = timeInMillis
    }

    fun setReminderTime(time: ReminderTime) {
        _reminder.value = time
    }

    fun setCheckedDays(checkedDays: BooleanArray?) {
        val default = booleanArrayOf(true, true, true, true, true, true, true)
        _checkedDays.value = checkedDays ?: default
    }

    fun setIcon(icon: Icon?) {
        _icon.value = icon
    }
}