package com.artworkspace.habittracker.ui.journal

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.artworkspace.habittracker.BaseApplication
import com.artworkspace.habittracker.R
import com.artworkspace.habittracker.adapter.ListHabitAdapter
import com.artworkspace.habittracker.adapter.OnItemClickCallback
import com.artworkspace.habittracker.adapter.SwipeGesture
import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.data.entity.HabitRecord
import com.artworkspace.habittracker.databinding.FragmentJournalBinding
import com.artworkspace.habittracker.ui.create.CreateHabitActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class JournalFragment : Fragment() {

    private var _binding: FragmentJournalBinding? = null
    private val binding get() = _binding!!

    private val journalViewModel: JournalViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJournalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listUncompletedHabit =
            ListHabitAdapter(requireActivity().application as BaseApplication)
        setUncompletedHabitRecyclerView(listUncompletedHabit)

        val listCompletedHabit = ListHabitAdapter(requireActivity().application as BaseApplication)
        setCompletedHabitRecyclerView(listCompletedHabit)

        lifecycleScope.launchWhenStarted {
            launch {
                journalViewModel.getUncompletedHabit().collect { habits ->
                    listUncompletedHabit.submitList(habits)
                }
            }

            launch {
                journalViewModel.getCompletedHabit().collect { habits ->
                    listCompletedHabit.submitList(habits)

                    if (habits.isEmpty()) {
                        binding.tvCountCompleted.visibility = View.GONE
                    } else {
                        binding.tvCountCompleted.visibility = View.VISIBLE
                    }
                }
            }
        }

        binding.apply {
            fabNewHabit.setOnClickListener {
                Intent(requireContext(), CreateHabitActivity::class.java).also {
                    startActivity(it)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        journalViewModel.todayRecordInit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showMessage(isVisible: Boolean) {
        val messageView = binding.tvHabitMessage

        if (isVisible) {
            messageView.apply {
                alpha = 0f
                visibility = View.VISIBLE

                animate()
                    .alpha(1f)
                    .setDuration(300L)
                    .setListener(null)
            }

        } else {
            messageView.animate()
                .alpha(0f)
                .setDuration(300L)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        messageView.visibility = View.GONE
                    }
                })
        }
    }

    private fun setCompletedHabitRecyclerView(listAdapter: ListHabitAdapter) {
        val linearLayoutManager = LinearLayoutManager(requireContext())

        val backgroundColor = requireContext().getColor(R.color.dark_grey)
        val labelColor = requireContext().getColor(R.color.white)
        val actionIcon = R.drawable.ic_baseline_undo_white_24
        val actionLabel = getString(R.string.undo)

        val swipeGesture =
            object : SwipeGesture(backgroundColor, actionIcon, actionLabel, labelColor) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.absoluteAdapterPosition
                    val habitRecord = listAdapter.currentList[position]
                    val habit = Habit(
                        id = habitRecord.id,
                        name = habitRecord.name,
                        description = habitRecord.description,
                        icon = habitRecord.icon,
                        startAt = habitRecord.startAt,
                        createdAt = habitRecord.createdAt
                    )
                    journalViewModel.setHabitRecordCheck(habit, false)
                }
            }

        listAdapter.setOnItemClickCallback(object : OnItemClickCallback {
            override fun onItemClicked(habit: HabitRecord) {
                Toast.makeText(requireContext(), "Hi", Toast.LENGTH_SHORT).show()
            }
        })

        binding.rvHabitCompleted.apply {
            layoutManager = linearLayoutManager
            adapter = listAdapter
            ItemTouchHelper(swipeGesture).attachToRecyclerView(this)
        }
    }

    private fun setUncompletedHabitRecyclerView(listAdapter: ListHabitAdapter) {
        val linearLayoutManager = LinearLayoutManager(requireContext())

        val backgroundColor = requireContext().getColor(R.color.blue_500)
        val labelColor = requireContext().getColor(R.color.white)
        val actionIcon = R.drawable.ic_baseline_check_white_24
        val actionLabel = getString(R.string.done)

        val swipeGesture =
            object : SwipeGesture(backgroundColor, actionIcon, actionLabel, labelColor) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.absoluteAdapterPosition
                    val habitRecord = listAdapter.currentList[position]
                    val habit = Habit(
                        id = habitRecord.id,
                        name = habitRecord.name,
                        description = habitRecord.description,
                        icon = habitRecord.icon,
                        startAt = habitRecord.startAt,
                        createdAt = habitRecord.createdAt
                    )
                    journalViewModel.setHabitRecordCheck(habit, true)
                }
            }

        listAdapter.setOnItemClickCallback(object : OnItemClickCallback {
            override fun onItemClicked(habit: HabitRecord) {
                Toast.makeText(requireContext(), "Hi", Toast.LENGTH_SHORT).show()
            }
        })

        binding.rvHabitNotCompleted.apply {
            layoutManager = linearLayoutManager
            adapter = listAdapter
            ItemTouchHelper(swipeGesture).attachToRecyclerView(this)
        }
    }
}