package com.artworkspace.habittracker.ui.edit

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
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(private val habitRepository: HabitRepository) :
    ViewModel() {

    private var weeklyTargetId: Long? = null

    private var _habitTitleState = MutableLiveData<String>()
    val habitTitleState: LiveData<String> = _habitTitleState

    private var _habitDescriptionState = MutableLiveData<String>()
    val habitDescriptionState: LiveData<String> = _habitDescriptionState

    private var _habitCheckedDaysState = MutableLiveData<BooleanArray>()
    val habitCheckedDaysState: LiveData<BooleanArray> = _habitCheckedDaysState

    private var _habitIconState = MutableLiveData<Int?>()
    val habitIconState: LiveData<Int?> = _habitIconState

    private var _habitReminderState = MutableLiveData<ReminderTime>()
    val habitReminderState: LiveData<ReminderTime> = _habitReminderState

    private var _habitStartState = MutableLiveData<Long>()
    val habitStartState: LiveData<Long> = _habitStartState

    fun initializeData(habit: Habit) {
        _habitTitleState.value = habit.name
        _habitDescriptionState.value = habit.description
        _habitStartState.value = habit.startAt
        _habitIconState.value = habit.icon

        viewModelScope.launch {
            getHabitRepeatData(habit)
            getHabitReminderData(habit)
        }
    }

    private suspend fun getHabitRepeatData(habit: Habit) {
        val weeklyTarget = habitRepository.getWeeklyTargetByHabit(habit)
        val repeatState = booleanArrayOf(
            weeklyTarget.component3(),
            weeklyTarget.component4(),
            weeklyTarget.component5(),
            weeklyTarget.component6(),
            weeklyTarget.component7(),
            weeklyTarget.component8(),
            weeklyTarget.component9(),
        )

        weeklyTargetId = weeklyTarget.id
        _habitCheckedDaysState.value = repeatState
    }

    private suspend fun getHabitReminderData(habit: Habit) {
        val reminderTime = habitRepository.getReminderTime(habit)

        _habitReminderState.value = reminderTime
    }

    fun setIconState(id: Int?) {
        _habitIconState.value = id
    }

    fun setCheckedDaysState(checkedDays: BooleanArray) {
        _habitCheckedDaysState.value = checkedDays
    }

    fun setReminderTimeState(reminderTime: ReminderTime) {
        _habitReminderState.value = reminderTime
    }

    fun setStartAtState(timestamp: Long) {
        _habitStartState.value = timestamp
    }

    fun saveHabit(habit: Habit, checkedDays: BooleanArray, reminderTime: ReminderTime) {
        val weeklyTarget = WeeklyTarget(
            id = weeklyTargetId,
            habitId = habit.id!!,
            mon = checkedDays[0],
            tue = checkedDays[1],
            wed = checkedDays[2],
            thu = checkedDays[3],
            fri = checkedDays[4],
            sat = checkedDays[5],
            sun = checkedDays[6],
        )

        viewModelScope.launch {
            habitRepository.updateHabitData(habit, weeklyTarget, reminderTime)
        }
    }
}