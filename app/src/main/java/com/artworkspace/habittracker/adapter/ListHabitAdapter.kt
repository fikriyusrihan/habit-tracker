package com.artworkspace.habittracker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.artworkspace.habittracker.BaseApplication
import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.databinding.HabitItemBinding

class ListHabitAdapter(
    private val listHabit: List<Habit>,
    application: BaseApplication
) :
    RecyclerView.Adapter<ListHabitAdapter.ListViewHolder>() {

    private lateinit var onItemClickCallback: OnItemClickCallback
    private val iconPack = application.iconPack

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    class ListViewHolder(var binding: HabitItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = HabitItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val habit = listHabit[position]

        if (habit.icon != null) {
            val icon = iconPack?.getIcon(habit.icon)
            holder.binding.ivHabitIcon.setImageDrawable(icon?.drawable)
        }

        holder.binding.tvHabitTitle.text = habit.name
        holder.itemView.setOnClickListener { onItemClickCallback.onItemClicked(habit) }
    }

    override fun getItemCount(): Int = listHabit.size

    interface OnItemClickCallback {
        fun onItemClicked(habit: Habit)
    }
}