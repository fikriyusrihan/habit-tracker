package com.artworkspace.habittracker.adapter

import com.artworkspace.habittracker.data.entity.HabitRecord

interface OnItemClickCallback {
    fun onItemClicked(habit: HabitRecord)
}