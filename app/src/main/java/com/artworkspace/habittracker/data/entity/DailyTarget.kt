package com.artworkspace.habittracker.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class DailyTarget(
    @PrimaryKey
    val id: Int,

    @ColumnInfo(name = "habit_id")
    val habitId: Int,

    @ColumnInfo(name = "value")
    val value: Int,

    @ColumnInfo(name = "unit")
    val unit: Int
) : Parcelable
