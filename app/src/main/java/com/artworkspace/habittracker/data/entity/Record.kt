package com.artworkspace.habittracker.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class Record(
    @PrimaryKey(autoGenerate = true)
    val id: Long?,

    @ColumnInfo(name = "habit_id")
    val habitId: Long,

    @ColumnInfo(name = "is_checked")
    val isChecked: Boolean,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long
) : Parcelable