package com.artworkspace.habittracker.ui.journal

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.artworkspace.habittracker.BaseApplication
import com.artworkspace.habittracker.R
import com.artworkspace.habittracker.adapter.ListHabitAdapter
import com.artworkspace.habittracker.adapter.OnItemClickCallback
import com.artworkspace.habittracker.data.entity.Habit
import com.artworkspace.habittracker.databinding.FragmentJournalBinding
import com.artworkspace.habittracker.ui.create.CreateHabitActivity
import dagger.hilt.android.AndroidEntryPoint
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
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

        val listAdapter = ListHabitAdapter(requireActivity().application as BaseApplication)
        setRecyclerView(listAdapter)

        lifecycle.coroutineScope.launch {
            journalViewModel.getUncompletedHabit().collect { habits ->
                showMessage(habits.isEmpty())
                listAdapter.submitList(habits)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showMessage(isVisible: Boolean) {
        val recyclerView = binding.rvHabitNotCompleted
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
            recyclerView.apply {
                alpha = 0f
                visibility = View.VISIBLE

                animate()
                    .alpha(1f)
                    .setDuration(300L)
                    .setListener(null)
            }
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

    private fun setRecyclerView(listAdapter: ListHabitAdapter) {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        val itemTouchHelper = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            private val doneColor = ContextCompat.getColor(requireContext(), R.color.blue_500)
            private val doneIcon = R.drawable.ic_baseline_check_white_24

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.absoluteAdapterPosition
                Log.d(TAG, "onSwiped: ${listAdapter.currentList}")
                journalViewModel.setHabitAsDone(listAdapter.currentList[position])
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
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )

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
                    .addSwipeRightLabel(requireContext().getString(R.string.done))
                    .setSwipeRightLabelColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                    .addSwipeRightActionIcon(doneIcon)
                    .create()
                    .decorate()
            }

        }
        listAdapter.setOnItemClickCallback(object : OnItemClickCallback {
            override fun onItemClicked(habit: Habit) {
                Toast.makeText(requireContext(), "Hi", Toast.LENGTH_SHORT).show()
            }
        })

        binding.rvHabitNotCompleted.apply {
            layoutManager = linearLayoutManager
            adapter = listAdapter
            ItemTouchHelper(itemTouchHelper).attachToRecyclerView(this)
        }
    }

    companion object {
        private const val TAG = "JournalFragment"
    }
}