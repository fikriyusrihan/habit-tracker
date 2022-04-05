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

    private var _habitStartAtState = MutableLiveData<Long>()
    val habitStartAtState: LiveData<Long> = _habitStartAtState

    private var _habitReminderTimeState = MutableLiveData<ReminderTime>()
    val habitReminderTimeState: LiveData<ReminderTime> = _habitReminderTimeState

    private var _habitCheckedDaysState = MutableLiveData<BooleanArray>()
    val habitCheckedDaysState: LiveData<BooleanArray> = _habitCheckedDaysState

    private var _habitIconState = MutableLiveData<Icon?>()
    val habitIconState: LiveData<Icon?> = _habitIconState

    init {
        val defaultReminderHour = 9
        val defaultReminderMinute = 0

        _habitStartAtState.value = todayTimestamp
        _habitCheckedDaysState.value = booleanArrayOf(true, true, true, true, true, true, true)
        _habitReminderTimeState.value = ReminderTime(
            hour = defaultReminderHour,
            minute = defaultReminderMinute
        )
    }

    /**
     * Save new habit to database
     *
     * @param habit Habit to save
     * @param checkedDays Checked days state to save
     * @param reminderTime ReminderTime state to save
     */
    suspend fun saveNewHabit(
        habit: Habit,
        checkedDays: BooleanArray?,
        reminderTime: ReminderTime
    ): Long {
        val habitId = habitRepository.insertHabit(habit)

        val weeklyTarget = WeeklyTarget(
            id = null,
            habitId = habitId,
            mon = checkedDays?.get(0) ?: true,
            tue = checkedDays?.get(1) ?: true,
            wed = checkedDays?.get(2) ?: true,
            thu = checkedDays?.get(3) ?: true,
            fri = checkedDays?.get(4) ?: true,
            sat = checkedDays?.get(5) ?: true,
            sun = checkedDays?.get(6) ?: true
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
     *
     * @param startAtTimestamp StartAt state
     */
    fun setHabitStartAtState(startAtTimestamp: Long) {
        _habitStartAtState.value = startAtTimestamp
    }

    /**
     * Saving reminder state from the UI
     *
     * @param reminderTime ReminderTime state
     */
    fun setHabitReminderTimeState(reminderTime: ReminderTime) {
        _habitReminderTimeState.value = reminderTime
    }

    /**
     * Saving checkedDays state from the UI
     *
     * @param checkedDays CheckedDays state
     */
    fun setHabitCheckedDaysState(checkedDays: BooleanArray?) {
        val default = booleanArrayOf(true, true, true, true, true, true, true)
        _habitCheckedDaysState.value = checkedDays ?: default
    }

    /**
     * Saving icon state from the UI
     *
     * @param icon Icon state
     */
    fun setHabitIconState(icon: Icon?) {
        _habitIconState.value = icon
    }
}