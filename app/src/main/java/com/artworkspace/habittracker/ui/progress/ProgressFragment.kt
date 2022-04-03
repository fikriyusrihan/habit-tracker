package com.artworkspace.habittracker.ui.progress

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.artworkspace.habittracker.R
import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.data.entity.Record
import com.artworkspace.habittracker.databinding.FragmentProgressBinding
import com.artworkspace.habittracker.utils.todayTimestamp
import com.artworkspace.habittracker.utils.tomorrowTimestamp
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProgressFragment : Fragment() {

    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!

    private val progressViewModel by viewModels<ProgressViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            launch {
                progressViewModel.getAllRecord().collect { records ->
                    countCurrentStreak(records)
                    countTotalHabitCompleted(records)
                    countDailyAverage(records)
                    countCompletionRate(records)
                }
            }

            launch {
                progressViewModel.getAllStartedHabit().collect { habits ->
                    countStartedHabit(habits)
                }
            }
        }

        binding.apply {
            btnShowInformation.setOnClickListener {
                showProgressInformation(requireContext())
            }

            cardStreak.setOnLongClickListener {
                Toast.makeText(requireContext(), getString(R.string.keep_it_up), Toast.LENGTH_SHORT)
                    .show()
                true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showProgressInformation(ctx: Context) {
        MaterialAlertDialogBuilder(ctx)
            .setTitle(getString(R.string.progress_information_title))
            .setMessage(getString(R.string.progress_information_message))
            .setPositiveButton(getString(R.string.progress_information_ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun countCurrentStreak(records: List<Record>) {
        var streakCounter = 0
        var lastTimestamp = 0L

        for (i in records.indices) {
            if (!(records[i].isChecked)) {
                if (!(records[i].timestamp == todayTimestamp || records[i].timestamp == tomorrowTimestamp)) break
            } else {
                if (lastTimestamp != records[i].timestamp) {
                    streakCounter++
                    lastTimestamp = records[i].timestamp
                } else {
                    continue
                }
            }
        }

        binding.tvCounterStreak.text = getString(R.string.streak_counter, streakCounter)
    }

    private fun countTotalHabitCompleted(records: List<Record>) {
        var counter = 0
        records.forEach { record ->
            if (record.isChecked) counter++
        }

        binding.tvTotalCompleted.text = getString(R.string.total_completed_placeholder, counter)
    }

    private fun countStartedHabit(habits: List<Habit>) {
        binding.tvStartedHabit.text = getString(R.string.started_habit_placeholder, habits.size)
    }

    private fun countDailyAverage(records: List<Record>) {
        var daysCounter = 0
        var completedHabitCounter = 0
        var lastTimestamp = 0L

        val sortedRecords = records.sortedByDescending {
            it.timestamp
        }

        for (sortedRecord in sortedRecords) {
            if (sortedRecord.timestamp != lastTimestamp) {
                if (sortedRecord.timestamp == tomorrowTimestamp) {
                    if (sortedRecord.isChecked) {
                        daysCounter++
                        lastTimestamp = sortedRecord.timestamp
                    }
                } else {
                    daysCounter++
                    lastTimestamp = sortedRecord.timestamp
                }
            }

            if (sortedRecord.isChecked) completedHabitCounter++
        }

        val avg = completedHabitCounter.toDouble() / daysCounter
        binding.tvAvgDaily.text = getString(R.string.double_placeholder, avg)
    }

    private fun countCompletionRate(records: List<Record>) {
        val completedCounter =
            records.filter { it.isChecked && it.timestamp <= todayTimestamp }.size
        val recordSize = records.filter { it.timestamp <= todayTimestamp }.size
        val completionRate = ((completedCounter.toDouble() / recordSize) * 100).toInt()
        val string = "$completionRate %"

        binding.tvCompletionRate.text = string
    }
}