package com.artworkspace.habittracker.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class Unit(
    @PrimaryKey
    val id: Int,
    val name: String
) : Parcelable