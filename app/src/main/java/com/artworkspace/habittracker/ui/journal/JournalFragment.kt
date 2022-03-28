package com.artworkspace.habittracker.ui.journal

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.artworkspace.habittracker.BaseApplication
import com.artworkspace.habittracker.adapter.ListHabitAdapter
import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.databinding.FragmentJournalBinding
import com.artworkspace.habittracker.ui.create.CreateHabitActivity
import dagger.hilt.android.AndroidEntryPoint

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

        journalViewModel.getUncompletedHabit().observe(viewLifecycleOwner) { habits ->
            showUncompletedHabit(habits)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showUncompletedHabit(habits: List<Habit>) {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        val listAdapter = ListHabitAdapter(habits, requireActivity().application as BaseApplication)
        val swipeGesture = object : ListHabitAdapter.SwipeGesture(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        val habit = listAdapter.getHabitItem(viewHolder.absoluteAdapterPosition)
                        Toast.makeText(
                            requireContext(),
                            habit.name,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    ItemTouchHelper.RIGHT -> {
                        Toast.makeText(
                            requireContext(),
                            "${viewHolder.absoluteAdapterPosition}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        ItemTouchHelper(swipeGesture).apply {
            attachToRecyclerView(binding.rvHabitNotCompleted)
        }

        binding.rvHabitNotCompleted.apply {
            layoutManager = linearLayoutManager
            adapter = listAdapter
            setHasFixedSize(true)
        }

        listAdapter.setOnItemClickCallback(object : ListHabitAdapter.OnItemClickCallback {
            override fun onItemClicked(habit: Habit) {
                Toast.makeText(requireContext(), "Hi", Toast.LENGTH_SHORT).show()
            }
        })
    }
}