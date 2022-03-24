package com.artworkspace.habittracker.ui.progress

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.artworkspace.habittracker.R
import com.artworkspace.habittracker.databinding.FragmentProgressBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProgressFragment : Fragment() {

    private var _binding: FragmentProgressBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val progressViewModel by viewModels<ProgressViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            btnShowInformation.setOnClickListener {
                showProgressInformation(requireContext())
            }

            cardStreak.setOnLongClickListener {
                Toast.makeText(requireContext(), getString(R.string.keep_it_up), Toast.LENGTH_SHORT)
                    .show()
                true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showProgressInformation(ctx: Context) {
        MaterialAlertDialogBuilder(ctx)
            .setTitle(getString(R.string.progress_information_title))
            .setMessage(getString(R.string.progress_information_message))
            .setPositiveButton(getString(R.string.progress_information_ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}