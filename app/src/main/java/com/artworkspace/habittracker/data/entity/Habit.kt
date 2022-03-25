package com.artworkspace.habittracker.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Int?,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "icon")
    val icon: Int?,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "start_at")
    val startAt: Long,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
) : Parcelable