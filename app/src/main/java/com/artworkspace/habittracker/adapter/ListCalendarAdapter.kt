package com.artworkspace.habittracker.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.artworkspace.habittracker.R
import com.artworkspace.habittracker.databinding.CalendarItemBinding
import com.artworkspace.habittracker.utils.todayTimestamp
import java.text.SimpleDateFormat
import java.util.*

class ListCalendarAdapter(private val calendarData: ArrayList<Long>, private val context: Context) :
    RecyclerView.Adapter<ListCalendarAdapter.ViewHolder>() {

    private lateinit var onItemClickListener: OnItemClickCallback
    private var selectedIndex = calendarData.indexOf(todayTimestamp)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = ViewHolder(
            CalendarItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

        viewHolder.itemView.setOnClickListener {
            // Update highlight for selected date
            val position = viewHolder.bindingAdapterPosition
            if (position != selectedIndex) {
                notifyItemChanged(selectedIndex)
                selectedIndex = viewHolder.bindingAdapterPosition
                notifyItemChanged(selectedIndex)
            }

            onItemClickListener.onItemClicked(calendarData[position])
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = calendarData[position]
        }
        val date = Date(calendar.timeInMillis)
        val sdf = SimpleDateFormat("EEE", Locale.ENGLISH)

        if (selectedIndex == position) setItemAsSelected(holder)
        else setItemAsDefault(holder)

        holder.binding.apply {
            tvDay.text = sdf.format(date).toString()
            tvDate.text = calendar.get(Calendar.DATE).toString()
        }
    }

    override fun getItemCount(): Int = calendarData.size

    /**
     * UI logic for the selected item
     */
    private fun setItemAsSelected(holder: ViewHolder) {
        holder.binding.apply {
            tvDate.setTextColor(ContextCompat.getColor(context, R.color.blue_500))
            tvDay.setTextColor(ContextCompat.getColor(context, R.color.blue_500))
            bgDate.setBackgroundResource(R.drawable.card_progress_radius)
        }
    }

    /**
     * UI logic for unselected items
     */
    private fun setItemAsDefault(holder: ViewHolder) {
        holder.binding.apply {
            tvDate.setTextColor(Color.GRAY)
            tvDay.setTextColor(Color.GRAY)
            bgDate.background = null
        }
    }

    /**
     * OnItemClickCallback setter for ListCalendarAdapter
     */
    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickListener = onItemClickCallback
    }

    inner class ViewHolder(var binding: CalendarItemBinding) : RecyclerView.ViewHolder(binding.root)

    interface OnItemClickCallback {
        fun onItemClicked(timestamp: Long)
    }
}