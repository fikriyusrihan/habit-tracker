package com.artworkspace.habittracker.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.artworkspace.habittracker.BaseApplication
import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.databinding.HabitItemBinding


class ListHabitAdapter(
    private val application: BaseApplication
) :
    ListAdapter<Habit, ListHabitAdapter.ListViewHolder>(DiffCallback) {

    private lateinit var onItemClickCallback: OnItemClickCallback

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val viewHolder = ListViewHolder(
            HabitItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            application
        )

        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.bindingAdapterPosition
            onItemClickCallback.onItemClicked(getItem(position))
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ListViewHolder(var binding: HabitItemBinding, application: BaseApplication) :
        RecyclerView.ViewHolder(binding.root) {
        private val iconPack = application.iconPack

        fun bind(habit: Habit) {
            if (habit.icon != null) {
                val icon = iconPack?.getIcon(habit.icon)
                binding.ivHabitIcon.setImageDrawable(icon?.drawable)
            }
            binding.tvHabitTitle.text = habit.name
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Habit>() {
            override fun areItemsTheSame(oldItem: Habit, newItem: Habit): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Habit, newItem: Habit): Boolean {
                return oldItem == newItem
            }

        }

        private const val TAG = "ListHabitAdapter"
    }
}