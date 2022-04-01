package com.artworkspace.habittracker.ui.detail

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.artworkspace.habittracker.R
import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.data.entity.Record
import com.artworkspace.habittracker.data.entity.ReminderTime
import com.artworkspace.habittracker.data.entity.WeeklyTarget
import com.artworkspace.habittracker.databinding.ActivityDetailBinding
import com.artworkspace.habittracker.utils.todayTimestamp
import com.artworkspace.habittracker.utils.tomorrowTimestamp
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var habit: Habit

    private val detailViewModel: DetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        habit = intent.getParcelableExtra<Habit>(EXTRA_DETAIL)!!

        // Initialize data to fetch
        detailViewModel.apply {
            getWeeklyTarget(habit)
            getTotalCompleted(habit)
            getTotalAllRecord(habit)
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }


        detailViewModel.apply {
            val observer = this@DetailActivity
            weeklyTarget.observe(observer) { weeklyTarget ->
                parseWeeklyTarget(weeklyTarget)
            }

            totalCompleted.observe(observer) { totalCompleted ->
                parseTotalCompleted(totalCompleted)
            }

            completionRate.observe(observer) { completionRate ->
                parseCompletionRate(completionRate)
            }

            getReminderTime(habit).observe(observer) { reminderTime ->
                parseReminderTime(reminderTime)
            }

            getAllHabitRecord(habit).observe(observer) { records ->
                countCurrentStreak(records)
            }
        }

        binding.toolbarLayout.title = habit.name

        if (habit.description.isNotBlank()) {
            binding.tvDescriptionStatus.visibility = View.VISIBLE
            binding.tvHabitDescription.apply {
                visibility = View.VISIBLE
                text = habit.description
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_edit -> {
            Toast.makeText(this, "Edit", Toast.LENGTH_SHORT).show()
            true
        }
        R.id.menu_delete -> {
            deleteHabit(habit)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun parseWeeklyTarget(weeklyTarget: WeeklyTarget) {
        var isEveryday = true
        var string = ""
        val daysInWeek = arrayOf('M', 'T', 'W', 'T', 'F', 'S', 'S')
        val checkedDays = booleanArrayOf(
            weeklyTarget.mon,
            weeklyTarget.tue,
            weeklyTarget.wed,
            weeklyTarget.thu,
            weeklyTarget.fri,
            weeklyTarget.sat,
            weeklyTarget.sun
        )

        checkedDays.forEachIndexed { index, checked ->
            if (checked) string += if (string.isBlank()) daysInWeek[index] else ", ${daysInWeek[index]}"
            else isEveryday = false
        }

        if (!isEveryday)
            binding.tvRepeatAt.text = string
    }

    private fun parseReminderTime(reminderTime: ReminderTime?) {
        if (reminderTime != null) {
            val sdf = SimpleDateFormat("h:mm a", Locale.ENGLISH)
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, reminderTime.hour)
                set(Calendar.MINUTE, reminderTime.minute)
            }

            binding.tvReminderAt.text = sdf.format(calendar.time)
        }
    }

    private fun parseTotalCompleted(totalCompleted: Int) {
        binding.tvTotalCompleted.text = totalCompleted.toString()
    }

    private fun parseCompletionRate(completionRate: Int) {
        val string = "$completionRate %"
        binding.tvCompletionRate.text = string
    }

    private fun deleteHabit(habit: Habit) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.are_you_sure))
            .setMessage(getString(R.string.your_habit_will_be_deleted))
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                detailViewModel.deleteRecord(habit)
                finish()
            }
            .show()
    }

    private fun countCurrentStreak(habitRecords: List<Record>) {
        var streakCounter = 0

        for (i: Int in habitRecords.indices) {
            if (!habitRecords[i].isChecked) {
                if (!(habitRecords[i].timestamp == todayTimestamp || habitRecords[i].timestamp == tomorrowTimestamp)) break
            } else {
                streakCounter++
            }
        }

        binding.tvCounterStreak.text = getString(R.string.streak_counter, streakCounter)
    }

    companion object {
        const val EXTRA_DETAIL = "extra_detail"
    }
}