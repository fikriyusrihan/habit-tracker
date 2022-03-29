package com.artworkspace.habittracker.ui.journal

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artworkspace.habittracker.data.HabitRepository
import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.data.entity.Record
import com.artworkspace.habittracker.utils.todayTimestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(private val habitRepository: HabitRepository) :
    ViewModel() {

    init {
        todayRecordInit()
    }

    fun getUncompletedHabit(timestamp: Long = todayTimestamp): Flow<List<Habit>> =
        habitRepository.getUncompletedHabit(timestamp)

    private fun todayRecordInit() {
        viewModelScope.launch {
            val count = habitRepository.getRecordSizeByTimestamp()
            val habits = habitRepository.getAllStartedHabit()

            if (habits.size != count) {
                habits.forEach { habit ->
                    val record = habitRepository.getHabitRecord(habit.id!!)
                    if (record == null) {
                        val newRecord = Record(
                            id = null,
                            habitId = habit.id,
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

    fun setHabitAsDone(habit: Habit, timestamp: Long = todayTimestamp) {
        viewModelScope.launch {
            habitRepository.setHabitAsDone(habit, timestamp)
        }
    }

    companion object {
        private const val TAG = "JournalViewModel"
    }
}