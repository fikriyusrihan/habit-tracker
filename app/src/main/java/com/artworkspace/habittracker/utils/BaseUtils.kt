package com.artworkspace.habittracker.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import java.util.*

val todayTimestamp: Long
    get() {
        val calendar = Calendar.getInstance()
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return calendar.timeInMillis
    }

val tomorrowTimestamp: Long
    get() {
        val calendar = Calendar.getInstance()
        calendar.apply {
            timeInMillis = todayTimestamp
            add(Calendar.DATE, 1)
        }

        return calendar.timeInMillis
    }

val yesterdayTimestamp: Long
    get() {
        val calendar = Calendar.getInstance()
        calendar.apply {
            timeInMillis = todayTimestamp
            add(Calendar.DATE, -1)
        }

        return calendar.timeInMillis
    }

/**
 * Animate view's visibility with crossfade effect
 *
 * @param isVisible visibility setting
 * @param view view to apply the animation
 */
fun animateViewVisibility(isVisible: Boolean, view: View) {
    if (isVisible) {
        view.apply {
            alpha = 0f
            visibility = View.VISIBLE

            animate()
                .alpha(1f)
                .setDuration(200L)
                .setListener(null)
        }

    } else {
        view.animate()
            .alpha(0f)
            .setDuration(200L)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    view.visibility = View.GONE
                }
            })
    }
}