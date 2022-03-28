package com.artworkspace.habittracker.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artworkspace.habittracker.data.HabitRepository
import com.artworkspace.habittracker.data.entity.Record
import com.artworkspace.habittracker.utils.todayTimestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(private val habitRepository: HabitRepository) :
    ViewModel() {

    init {
        todayRecordInit()
    }

    fun getUncompletedHabit(timestamp: Long? = null) =
        habitRepository.getUncompletedHabit(timestamp ?: todayTimestamp)

    private fun todayRecordInit() {
        viewModelScope.launch {
            val count = habitRepository.getRecordSizeByTimestamp()
            val habits = habitRepository.getAllStartedHabit()

            if (habits.size != count) {
                habits.forEach { habit ->
                    val record = habitRepository.getRecordByHabitId(habit.id!!)
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
            }
        }
    }
}