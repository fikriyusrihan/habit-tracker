package com.artworkspace.habittracker.adapter

import com.artworkspace.habittracker.data.entity.Habit

interface OnItemClickCallback {
    fun onItemClicked(habit: Habit)
}