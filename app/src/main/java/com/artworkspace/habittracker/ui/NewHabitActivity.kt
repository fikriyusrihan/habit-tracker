package com.artworkspace.habittracker.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.artworkspace.habittracker.databinding.ActivityNewHabitBinding

class NewHabitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewHabitBinding

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
            }
        }
    }
}