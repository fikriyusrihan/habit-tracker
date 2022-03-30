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
import com.artworkspace.habittracker.adapter.ListCalendarAdapter
import com.artworkspace.habittracker.adapter.ListHabitAdapter
import com.artworkspace.habittracker.adapter.SwipeGesture
import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.data.entity.HabitRecord
import com.artworkspace.habittracker.databinding.FragmentJournalBinding
import com.artworkspace.habittracker.ui.create.CreateHabitActivity
import com.artworkspace.habittracker.utils.todayTimestamp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class JournalFragment : Fragment() {

    private lateinit var listUncompletedHabit: ListHabitAdapter
    private lateinit var listCompletedHabit: ListHabitAdapter

    private var getUncompletedHabitJob: Job = Job()
    private var getCompletedHabitJob: Job = Job()
    private var selectedTimestamp = todayTimestamp

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

        listUncompletedHabit = ListHabitAdapter(requireActivity().application as BaseApplication)
        setUncompletedHabitRecyclerView(listUncompletedHabit)

        listCompletedHabit = ListHabitAdapter(requireActivity().application as BaseApplication)
        setCompletedHabitRecyclerView(listCompletedHabit)

        journalViewModel.calendarHorizontalData.observe(viewLifecycleOwner) { calendarData ->
            setHorizontalCalendar(calendarData)
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
        getAllHabitRecord(selectedTimestamp)
        journalViewModel.recordInit(selectedTimestamp)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun animateViewVisibility(isVisible: Boolean, messageView: View) {
        if (isVisible) {
            messageView.apply {
                alpha = 0f
                visibility = View.VISIBLE

                animate()
                    .alpha(1f)
                    .setDuration(200L)
                    .setListener(null)
            }

        } else {
            messageView.animate()
                .alpha(0f)
                .setDuration(200L)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        messageView.visibility = View.GONE
                    }
                })
        }
    }

    private fun getAllHabitRecord(timestamp: Long) {
        if (getUncompletedHabitJob.isActive) getUncompletedHabitJob.cancel()
        if (getCompletedHabitJob.isActive) getCompletedHabitJob.cancel()

        lifecycleScope.launchWhenStarted {
            getUncompletedHabitJob = launch {
                journalViewModel.getUncompletedHabit(timestamp).collect { habits ->
                    listUncompletedHabit.submitList(habits)
                }
            }

            getCompletedHabitJob = launch {
                journalViewModel.getCompletedHabit(timestamp).collect { habits ->
                    listCompletedHabit.submitList(habits)
                    habits.isNotEmpty().let { visibility ->
                        animateViewVisibility(visibility, binding.tvCountCompleted)
                        animateViewVisibility(visibility, binding.rvHabitCompleted)
                        animateViewVisibility(visibility, binding.dividerTop)
                        animateViewVisibility(visibility, binding.dividerBottom)
                    }
                }
            }
        }
    }

    private fun setHorizontalCalendar(calendarData: ArrayList<Long>) {
        val linearLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val calendarAdapter = ListCalendarAdapter(calendarData, requireContext())

        binding.rvHorizontalCalendar.apply {
            layoutManager = linearLayoutManager
            adapter = calendarAdapter
            scrollToPosition(calendarData.indexOf(todayTimestamp))
        }

        calendarAdapter.setOnItemClickCallback(object : ListCalendarAdapter.OnItemClickCallback {
            override fun onItemClicked(timestamp: Long) {
                getAllHabitRecord(timestamp)
                journalViewModel.recordInit(timestamp)

                selectedTimestamp = timestamp
            }
        })
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
                    journalViewModel.setHabitRecordCheck(habit, false, selectedTimestamp)
                }
            }

        listAdapter.setOnItemClickCallback(object : ListHabitAdapter.OnItemClickCallback {
            override fun onItemClicked(habit: HabitRecord) {
                Toast.makeText(requireContext(), "Hi", Toast.LENGTH_SHORT).show()
            }
        })

        binding.rvHabitCompleted.apply {
            layoutManager = linearLayoutManager
            adapter = listAdapter
            itemAnimator = null

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
                    journalViewModel.setHabitRecordCheck(habit, true, selectedTimestamp)
                }
            }

        listAdapter.setOnItemClickCallback(object : ListHabitAdapter.OnItemClickCallback {
            override fun onItemClicked(habit: HabitRecord) {
                Toast.makeText(requireContext(), "Hi", Toast.LENGTH_SHORT).show()
            }
        })

        binding.rvHabitNotCompleted.apply {
            layoutManager = linearLayoutManager
            adapter = listAdapter
            itemAnimator = null

            ItemTouchHelper(swipeGesture).attachToRecyclerView(this)
        }
    }
}