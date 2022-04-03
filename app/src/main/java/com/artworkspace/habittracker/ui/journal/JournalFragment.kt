package com.artworkspace.habittracker.ui.journal

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.artworkspace.habittracker.ui.detail.DetailActivity
import com.artworkspace.habittracker.ui.detail.DetailActivity.Companion.EXTRA_DETAIL
import com.artworkspace.habittracker.utils.animateViewVisibility
import com.artworkspace.habittracker.utils.todayTimestamp
import com.artworkspace.habittracker.utils.tomorrowTimestamp
import com.artworkspace.habittracker.utils.yesterdayTimestamp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class JournalFragment : Fragment() {

    private lateinit var listUncompletedHabit: ListHabitAdapter
    private lateinit var listCompletedHabit: ListHabitAdapter

    private var getUncompletedHabitJob: Job = Job()
    private var getCompletedHabitJob: Job = Job()
    private var selectedTimestamp = todayTimestamp
    private var counterTodayHabit = 0

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

        // Initialize record first then get all habit's record
        journalViewModel.recordInit(selectedTimestamp)
        getAllHabitRecord(selectedTimestamp)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Get all habit record by timestamp
     *
     * @param timestamp habit record's timestamp
     */
    private fun getAllHabitRecord(timestamp: Long) {
        // Make sure only one job for each habit status is active and cancel previous job before starting new job
        if (getUncompletedHabitJob.isActive) getUncompletedHabitJob.cancel()
        if (getCompletedHabitJob.isActive) getCompletedHabitJob.cancel()
        counterTodayHabit = 0

        lifecycleScope.launchWhenStarted {
            getUncompletedHabitJob = launch {
                journalViewModel.getUncompletedHabit(timestamp).collect { habits ->
                    listUncompletedHabit.submitList(habits)
                    counterTodayHabit += habits.size

                    animateViewVisibility(counterTodayHabit == 0, binding.tvHabitMessage)
                    animateViewVisibility(habits.isNotEmpty(), binding.rvHabitNotCompleted)
                }
            }

            getCompletedHabitJob = launch {
                journalViewModel.getCompletedHabit(timestamp).collect { habits ->
                    listCompletedHabit.submitList(habits)
                    counterTodayHabit += habits.size

                    habits.isNotEmpty().let { visibility ->
                        animateViewVisibility(visibility, binding.tvCountCompleted)
                        animateViewVisibility(visibility, binding.rvHabitCompleted)
                        animateViewVisibility(visibility, binding.dividerTop)
                    }

                    animateViewVisibility(counterTodayHabit == 0, binding.tvHabitMessage)
                }
            }
        }
    }

    /**
     * Set the horizontal calendar to the RecyclerView
     *
     * @param calendarData array of calendar to display
     */
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
                // Initialize record first then get all habit's record
                journalViewModel.recordInit(timestamp)
                getAllHabitRecord(timestamp)

                setToolbarDate(timestamp)
                selectedTimestamp = timestamp
            }
        })
    }

    /**
     * Set the completed habit to the RecyclerView. Call this function after the listAdapter has initialized
     *
     * @param listAdapter ListHabitAdapter
     */
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
                val habitDetail = Habit(
                    habit.id,
                    habit.name,
                    habit.icon,
                    habit.description,
                    habit.startAt,
                    habit.createdAt
                )

                Intent(requireContext(), DetailActivity::class.java).apply {
                    putExtra(EXTRA_DETAIL, habitDetail)
                }.also { intent ->
                    startActivity(intent)
                }
            }
        })

        binding.rvHabitCompleted.apply {
            layoutManager = linearLayoutManager
            adapter = listAdapter

            ItemTouchHelper(swipeGesture).attachToRecyclerView(this)
        }
    }

    /**
     * Set the completed habit to the RecyclerView. Call this function after the listAdapter has initialized
     *
     * @param listAdapter ListHabitAdapter
     */
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
                val habitDetail = Habit(
                    habit.id,
                    habit.name,
                    habit.icon,
                    habit.description,
                    habit.startAt,
                    habit.createdAt
                )

                Intent(requireContext(), DetailActivity::class.java).apply {
                    putExtra(EXTRA_DETAIL, habitDetail)
                }.also { intent ->
                    startActivity(intent)
                }
            }
        })

        binding.rvHabitNotCompleted.apply {
            layoutManager = linearLayoutManager
            adapter = listAdapter

            ItemTouchHelper(swipeGesture).attachToRecyclerView(this)
        }
    }

    /**
     * UI logic for setting date on the toolbar
     *
     * @param timestamp selected timestamp
     */
    private fun setToolbarDate(timestamp: Long) {
        val sdf = SimpleDateFormat.getDateInstance()

        binding.toolbarTitleSecondary.let { view ->
            animateViewVisibility(false, view)
            view.text = when (timestamp) {
                todayTimestamp -> getString(R.string.today)
                tomorrowTimestamp -> getString(R.string.tomorrow)
                yesterdayTimestamp -> getString(R.string.yesterday)
                else -> {
                    val date = Date(timestamp)
                    val txtDate = sdf.format(date)
                    txtDate
                }
            }
            animateViewVisibility(true, view)
        }
    }
}