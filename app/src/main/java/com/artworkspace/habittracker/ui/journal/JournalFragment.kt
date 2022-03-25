package com.artworkspace.habittracker.ui.journal

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.artworkspace.habittracker.databinding.FragmentJournalBinding
import com.artworkspace.habittracker.ui.create.NewHabitActivity

class JournalFragment : Fragment() {

    private var _binding: FragmentJournalBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
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
                Intent(requireContext(), NewHabitActivity::class.java).also {
                    startActivity(it)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}