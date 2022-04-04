package com.artworkspace.habittracker.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artworkspace.habittracker.data.HabitRepository
import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.data.entity.Record
import com.artworkspace.habittracker.data.entity.ReminderTime
import com.artworkspace.habittracker.data.entity.WeeklyTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _weeklyTarget = MutableLiveData<WeeklyTarget>()
    val weeklyTarget: LiveData<WeeklyTarget> = _weeklyTarget

    private val _completionRate = MutableLiveData<Int>()
    val completionRate: LiveData<Int> = _completionRate

    private val _totalCompleted = MutableLiveData<Int>()
    val totalCompleted: LiveData<Int> = _totalCompleted

    fun getReminderTime(habit: Habit): LiveData<ReminderTime> =
        habitRepository.getReminderTimeLiveData(habit)

    fun getAllHabitRecord(habit: Habit): LiveData<List<Record>> =
        habitRepository.getAllHabitRecords(habit)

    fun getWeeklyTarget(habit: Habit) {
        viewModelScope.launch {
            _weeklyTarget.value = habitRepository.getWeeklyTargetByHabit(habit)
        }
    }

    fun getTotalCompleted(habit: Habit) {
        viewModelScope.launch {
            _totalCompleted.value = habitRepository.getCountCompletedHabit(habit)
        }
    }

    fun getTotalAllRecord(habit: Habit) {
        viewModelScope.launch {
            val totalRecord = habitRepository.getCountAllHabitRecord(habit)
            val completedHabit = habitRepository.getCountCompletedHabit(habit)

            val completionRate = (completedHabit.toDouble() / totalRecord.toDouble()) * 100
            _completionRate.value = completionRate.toInt()
        }
    }

    fun deleteRecord(habit: Habit) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habit)
        }
    }

    companion object {
        private const val TAG = "DetailViewModel"
    }
}