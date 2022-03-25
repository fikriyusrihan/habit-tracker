package com.artworkspace.habittracker.ui.create

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.artworkspace.habittracker.R
import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.databinding.ActivityNewHabitBinding
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class NewHabitActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityNewHabitBinding

    private var startAtTimestamp: Long? = null

    private val viewModel: NewHabitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewHabitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.startAtTimestamp.observe(this) { timeInMillis ->
            startAtTimestamp = timeInMillis
            setDefaultStartAt(timeInMillis)
        }

        binding.apply {
            btnExit.setOnClickListener(this@NewHabitActivity)
            btnSave.setOnClickListener(this@NewHabitActivity)
            startFromStatus.setOnClickListener(this@NewHabitActivity)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnExit.id -> {
                finish()
            }

            binding.btnSave.id -> {
                Toast.makeText(
                    this@NewHabitActivity,
                    getString(R.string.new_habit_created),
                    Toast.LENGTH_SHORT
                ).show()
                saveNewHabit()
                finish()
            }

            binding.startFromStatus.id -> {
                setStartDate()
            }
        }
    }

    private fun setDefaultStartAt(timeInMillis: Long) {
        val date = Date(timeInMillis)
        val sdf = SimpleDateFormat.getDateInstance()

        binding.tvStartFromStatus.text = getString(R.string.start_from, sdf.format(date))
    }

    private fun setStartDate() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.select_date))
            .setSelection(startAtTimestamp)
            .build()

        datePicker.show(supportFragmentManager, TAG)
        datePicker.addOnPositiveButtonClickListener {
            viewModel.setStartAtTimestamp(it)
        }
    }

    private fun saveNewHabit() {
        val id = null
        val name = binding.etHabitTitle.text.toString()
        val icon = null
        val description = ""
        val startAt = startAtTimestamp
        val createdAt = Calendar.getInstance().timeInMillis

        val habit = Habit(
            id, name, icon, description, startAt!!, createdAt
        )

        viewModel.insertHabit(habit)
    }

    companion object {
        private const val TAG = "NewHabitActivity"
    }
}