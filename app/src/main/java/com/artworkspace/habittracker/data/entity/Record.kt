package com.artworkspace.habittracker.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.sql.Timestamp

@Entity
@Parcelize
data class Record(
    @PrimaryKey
    val id: Int,

    @ColumnInfo(name = "habit_id")
    val habitId: Int,

    @ColumnInfo(name = "is_checked")
    val isChecked: Boolean,

    @ColumnInfo(name = "value")
    val value: Int,

    @ColumnInfo(name = "timestamp")
    val timestamp: Int
) : Parcelable