package com.artworkspace.habittracker.ui.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.artworkspace.habittracker.data.HabitRepository
import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.data.entity.ReminderTime
import com.artworkspace.habittracker.data.entity.WeeklyTarget
import com.artworkspace.habittracker.utils.todayTimestamp
import com.maltaisn.icondialog.data.Icon
import dagger.hilt.android.lifecycle.HiltViewModel
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

    /**
     * Save new habit to database
     */
    suspend fun saveNewHabit(
        habit: Habit,
        weeklyTargetArray: BooleanArray?,
        reminderTime: ReminderTime
    ): Long {
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

        val reminder = ReminderTime(
            id = null,
            habitId = habitId,
            hour = reminderTime.hour,
            minute = reminderTime.minute
        )

        habitRepository.insertWeeklyTarget(weeklyTarget)
        habitRepository.insertReminderTime(reminder)

        return habitId
    }

    /**
     * Saving startAtTimestamp state from the UI
     */
    fun setStartAtTimestamp(timeInMillis: Long) {
        _startAtTimestamp.value = timeInMillis
    }

    /**
     * Saving reminder state from the UI
     */
    fun setReminderTime(time: ReminderTime) {
        _reminder.value = time
    }

    /**
     * Saving checkedDays state from the UI
     */
    fun setCheckedDays(checkedDays: BooleanArray?) {
        val default = booleanArrayOf(true, true, true, true, true, true, true)
        _checkedDays.value = checkedDays ?: default
    }

    /**
     * Saving icon state from the UI
     */
    fun setIcon(icon: Icon?) {
        _icon.value = icon
    }
}