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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _completionRate = MutableLiveData<Int>()
    val completionRate: LiveData<Int> = _completionRate

    private val _totalCompleted = MutableLiveData<Int>()
    val totalCompleted: LiveData<Int> = _totalCompleted

    /**
     * Get habit information from database,
     * using Flow to make sure the data that received from database always aware of data change
     *
     * @param habit Habit information
     * @return Flow
     */
    fun getHabit(habit: Habit): Flow<Habit> = habitRepository.getHabitByIdInFlow(habit.id!!)

    /**
     * Get weekly target information of a habit in Flow format
     *
     * @param habit Habit information
     * @return Flow
     */
    fun getWeeklyTarget(habit: Habit): Flow<WeeklyTarget> =
        habitRepository.getHabitWeeklyTargetInFlow(habit.id!!)

    /**
     * Get reminder time information of a habit in Flow format
     *
     * @param habit Habit information
     * @return Flow
     */
    fun getReminderTime(habit: Habit): Flow<ReminderTime> =
        habitRepository.getReminderTimeLiveData(habit)

    /**
     * Get all record that related to a habit
     *
     * @param habit Habit Information
     * @return LiveData
     */
    fun getAllHabitRecord(habit: Habit): LiveData<List<Record>> =
        habitRepository.getAllHabitRecords(habit)

    /**
     * Get total completed data that related to a habit
     *
     * @param habit Habit information
     */
    fun getTotalCompleted(habit: Habit) {
        viewModelScope.launch {
            _totalCompleted.value = habitRepository.getCountCompletedHabit(habit)
        }
    }

    /**
     * Get total record data that relate to a habit
     *
     * @param habit Habit information
     */
    fun getTotalAllRecord(habit: Habit) {
        viewModelScope.launch {
            val totalRecord = habitRepository.getCountAllRecordOfHabit(habit)
            val completedHabit = habitRepository.getCountCompletedHabit(habit)

            val completionRate = (completedHabit.toDouble() / totalRecord.toDouble()) * 100
            _completionRate.value = completionRate.toInt()
        }
    }

    /**
     * Delete a record from database
     *
     * @param habit Habit to delete
     */
    fun deleteRecord(habit: Habit) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habit)
        }
    }
}