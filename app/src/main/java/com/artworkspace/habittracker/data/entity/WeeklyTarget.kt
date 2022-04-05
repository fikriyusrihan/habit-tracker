package com.artworkspace.habittracker.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "weekly_target")
data class WeeklyTarget(
    @PrimaryKey(autoGenerate = true)
    val id: Long?,

    @ColumnInfo(name = "habit_id")
    val habitId: Long,

    val mon: Boolean,
    val tue: Boolean,
    val wed: Boolean,
    val thu: Boolean,
    val fri: Boolean,
    val sat: Boolean,
    val sun: Boolean
) : Parcelable