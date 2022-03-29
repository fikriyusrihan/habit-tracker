package com.artworkspace.habittracker.ui.journal

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artworkspace.habittracker.data.HabitRepository
import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.data.entity.HabitRecord
import com.artworkspace.habittracker.data.entity.Record
import com.artworkspace.habittracker.utils.todayTimestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(private val habitRepository: HabitRepository) :
    ViewModel() {

    fun getUncompletedHabit(timestamp: Long = todayTimestamp): Flow<List<HabitRecord>> =
        habitRepository.getUncompletedHabit(timestamp)

    fun getCompletedHabit(timestamp: Long = todayTimestamp): Flow<List<HabitRecord>> =
        habitRepository.getCompletedHabit(timestamp)

    fun todayRecordInit() {
        viewModelScope.launch {
            val count = habitRepository.getRecordSizeByTimestamp()

            habitRepository.getAllStartedHabit().collect { habits ->
                if (habits.size != count) {
                    habits.forEach { habit ->
                        val oldRecord = habitRepository.getHabitRecord(habit)
                        if (oldRecord == null) {
                            val newRecord = Record(
                                id = null,
                                habitId = habit.id!!,
                                isChecked = false,
                                timestamp = todayTimestamp
                            )
                            habitRepository.insertDailyRecord(newRecord)
                        }
                    }

                    Log.d(TAG, "todayRecordInit: called")
                }
            }
        }
    }

    fun setHabitRecordCheck(habit: Habit, isChecked: Boolean, timestamp: Long = todayTimestamp) {
        viewModelScope.launch {
            habitRepository.setHabitRecordCheck(habit, isChecked, timestamp)
        }
    }

    companion object {
        private const val TAG = "JournalViewModel"
    }
}