package com.artworkspace.habittracker.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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

    /**
     * OnItemClickCallback setter for ListHabitAdapter
     */
    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    inner class ListViewHolder(var binding: HabitItemBinding, var application: BaseApplication) :
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
            } else {
                binding.ivHabitIcon.setImageResource(R.drawable.ic_baseline_directions_run_24)
            }

            binding.apply {
                if (habit.isChecked) {
                    ivCompletedIcon.visibility = View.VISIBLE
                    tvHabitStatus.text = application.getString(R.string.completed)

                    root.setBackgroundColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.light_grey
                        )
                    )

                    ivHabitIcon.apply {
                        setColorFilter(Color.WHITE)
                        setBackgroundResource(R.drawable.icon_background_circle_dark)
                    }
                }

                tvHabitTitle.text = habit.name
            }
        }
    }

    interface OnItemClickCallback {
        fun onItemClicked(habit: HabitRecord)
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