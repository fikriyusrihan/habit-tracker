package com.artworkspace.habittracker.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class WeeklyTarget(
    @PrimaryKey
    val id: Int,

    @ColumnInfo(name = "habit_id")
    val habitId: Int,

    val mon: Boolean,
    val tue: Boolean,
    val wed: Boolean,
    val thu: Boolean,
    val fri: Boolean,
    val sat: Boolean,
    val sun: Boolean
) : Parcelable