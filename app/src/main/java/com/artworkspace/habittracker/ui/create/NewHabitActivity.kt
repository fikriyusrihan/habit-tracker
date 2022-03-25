package com.artworkspace.habittracker.ui.create

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.databinding.ActivityNewHabitBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class NewHabitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewHabitBinding
    private val viewModel: NewHabitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewHabitBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.apply {
            btnExit.setOnClickListener {
                finish()
            }

            btnSave.setOnClickListener {
                Toast.makeText(this@NewHabitActivity, "It clicked", Toast.LENGTH_SHORT).show()
                saveNewHabit()
                finish()
            }
        }
    }

    private fun saveNewHabit() {
        val id = null
        val name = binding.etHabitTitle.text.toString()
        val icon = null
        val description = ""
        val startAt = Calendar.getInstance().timeInMillis
        val createdAt = Calendar.getInstance().timeInMillis

        val habit = Habit(
            id, name, icon, description, startAt, createdAt
        )

        viewModel.insertHabit(habit)
    }
}