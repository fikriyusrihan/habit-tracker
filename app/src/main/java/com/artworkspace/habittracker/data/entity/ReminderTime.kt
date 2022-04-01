package com.artworkspace.habittracker.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ReminderTime(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,

    @ColumnInfo(name = "habit_id")
    val habitId: Long = -1,

    val hour: Int,
    val minute: Int
)
