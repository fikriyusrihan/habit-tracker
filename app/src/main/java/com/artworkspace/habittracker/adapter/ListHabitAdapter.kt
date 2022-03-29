package com.artworkspace.habittracker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.artworkspace.habittracker.BaseApplication
import com.artworkspace.habittracker.R
import com.artworkspace.habittracker.data.entity.HabitRecord
import com.artworkspace.habittracker.databinding.HabitItemBinding


class ListHabitAdapter(private val application: BaseApplication) :
    ListAdapter<HabitRecord, ListHabitAdapter.ListViewHolder>(DiffCallback) {

    private lateinit var onItemClickCallback: OnItemClickCallback

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

    class ListViewHolder(var binding: HabitItemBinding, var application: BaseApplication) :
        RecyclerView.ViewHolder(binding.root) {
        private val iconPack = application.iconPack

        /**
         * Bind the habit record to the correspondent views
         *
         * @param habit HabitRecord
         */
        fun bind(habit: HabitRecord) {
            if (habit.icon != null) {
                val icon = iconPack?.getIcon(habit.icon)
                binding.ivHabitIcon.setImageDrawable(icon?.drawable)
            }

            binding.apply {
                tvHabitTitle.text = habit.name
                tvHabitStatus.text =
                    if (habit.isChecked) application.getString(R.string.completed)
                    else application.getString(R.string.not_completed)
            }
        }
    }

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<HabitRecord>() {
            override fun areItemsTheSame(oldItem: HabitRecord, newItem: HabitRecord): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: HabitRecord, newItem: HabitRecord): Boolean {
                return oldItem == newItem
            }
        }
    }
}