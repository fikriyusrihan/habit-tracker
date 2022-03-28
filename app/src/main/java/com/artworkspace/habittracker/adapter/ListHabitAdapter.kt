package com.artworkspace.habittracker.adapter

import android.content.Context
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.artworkspace.habittracker.BaseApplication
import com.artworkspace.habittracker.R
import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.databinding.HabitItemBinding
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator


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

    fun getHabitItem(index: Int): Habit = listHabit[index]

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

    abstract class SwipeGesture(context: Context) :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        private val doneColor = ContextCompat.getColor(context, R.color.blue_500)
        private val skipColor = ContextCompat.getColor(context, R.color.purple_500_half)
        private val doneIcon = R.drawable.ic_baseline_check_white_24
        private val skipIcon = R.drawable.ic_baseline_arrow_forward_24

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

            RecyclerViewSwipeDecorator.Builder(
                c,
                recyclerView,
                viewHolder,
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )
                .addSwipeLeftBackgroundColor(skipColor)
                .addSwipeLeftActionIcon(skipIcon)
                .addSwipeRightBackgroundColor(doneColor)
                .addSwipeRightActionIcon(doneIcon)
                .create()
                .decorate()
        }

    }
}