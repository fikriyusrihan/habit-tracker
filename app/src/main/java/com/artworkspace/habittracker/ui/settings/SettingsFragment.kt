package com.artworkspace.habittracker.ui.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.artworkspace.habittracker.R
import com.artworkspace.habittracker.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentSettingsBinding? = null
    private val settingsViewModel: SettingsViewModel by viewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            btnSendFeedback.setOnClickListener(this@SettingsFragment)
            btnShareApp.setOnClickListener(this@SettingsFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnSendFeedback.id -> {
                composeEmail(
                    addresses = arrayOf(EMAIL),
                    subject = getString(R.string.mail_subject),
                    body = getString(R.string.feedback_mail_body)
                )
            }
            binding.btnShareApp.id -> {
                shareApp(getString(R.string.share_app_message))
            }
        }
    }

    /**
     * Create an Intent to share message
     *
     * @param message a message to share
     */
    private fun shareApp(message: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(intent, null)
        startActivity(shareIntent)
    }

    /**
     * Create an Intent to send email for feedback to developer
     *
     * @param addresses receiver's email address
     * @param subject email's subject
     * @param body email's body
     */
    private fun composeEmail(addresses: Array<String>, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, addresses)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                getString(R.string.application_not_found),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    companion object {
        private const val EMAIL = "fikriyusrihan@gmail.com"
    }
}