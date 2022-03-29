package com.artworkspace.habittracker.adapter

import android.content.Context
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.artworkspace.habittracker.R
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

abstract class SwipeGesture(private val context: Context) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

    private val doneColor = ContextCompat.getColor(context, R.color.blue_500)
    private val doneIcon = R.drawable.ic_baseline_check_white_24

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
            .addSwipeRightBackgroundColor(doneColor)
            .addSwipeRightLabel(context.getString(R.string.done))
            .setSwipeRightLabelColor(ContextCompat.getColor(context, R.color.white))
            .addSwipeRightActionIcon(doneIcon)
            .create()
            .decorate()
    }

}