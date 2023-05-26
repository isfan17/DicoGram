package com.isfan17.dicogram.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.isfan17.dicogram.R
import com.isfan17.dicogram.databinding.FragmentProfileBinding
import com.isfan17.dicogram.ui.viewmodels.MainViewModel
import com.isfan17.dicogram.ui.viewmodels.ViewModelFactory
import com.isfan17.dicogram.utils.Constants.Companion.USER_PREF_EMAIL_NAME
import com.isfan17.dicogram.utils.Constants.Companion.USER_PREF_NAME_NAME

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory: ViewModelFactory = ViewModelFactory.getInstance(requireActivity())
        val viewModel: MainViewModel by viewModels { factory }

        viewModel.getUserPreferences(USER_PREF_NAME_NAME).observe(this.viewLifecycleOwner) {
            binding.tvName.text = it
        }

        viewModel.getUserPreferences(USER_PREF_EMAIL_NAME).observe(this.viewLifecycleOwner) {
            binding.tvEmail.text = it
        }

        binding.actionLogout.setOnClickListener { _ ->
            context?.let { it ->
                MaterialAlertDialogBuilder(it)
                    .setTitle(getString(R.string.fixed_text_account_log_out))
                    .setMessage(getString(R.string.fixed_text_log_out_msg))
                    .setNegativeButton(getString(R.string.fixed_text_cancel)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(getString(R.string.fixed_text_yes)) { _, _ ->
                        viewModel.clearLoginSession()
                        (activity as MainActivity).moveToAuth()
                    }
                    .show()
            }
        }

        binding.btnLanguage.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
        }

        binding.btnPermission.setOnClickListener {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS

            val uri: Uri = Uri.fromParts("package", (activity as MainActivity).packageName, null)
            intent.data = uri

            (activity as MainActivity).startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}