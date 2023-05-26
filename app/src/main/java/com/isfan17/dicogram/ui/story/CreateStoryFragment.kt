package com.isfan17.dicogram.ui.story

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.isfan17.dicogram.R
import com.isfan17.dicogram.databinding.FragmentCreateStoryBinding
import com.isfan17.dicogram.ui.viewmodels.StoryViewModel
import com.isfan17.dicogram.ui.viewmodels.ViewModelFactory
import com.isfan17.dicogram.utils.Constants.Companion.USER_PREF_TOKEN_NAME
import com.isfan17.dicogram.utils.Helper.rotateBitmap
import com.isfan17.dicogram.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CreateStoryFragment : Fragment() {

    private var _binding: FragmentCreateStoryBinding? = null
    private val binding get() = _binding!!

    private var token: String? = null
    private val navArgs: CreateStoryFragmentArgs by navArgs()

    private var storyLat: String? = null
    private var storyLng: String? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        _binding = FragmentCreateStoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory: ViewModelFactory = ViewModelFactory.getInstance(requireActivity())
        val viewModel: StoryViewModel by viewModels { factory }

        viewModel.getUserPreferences(USER_PREF_TOKEN_NAME).observe(this.viewLifecycleOwner) {
            token = StringBuilder("Bearer ").append(it).toString()
        }

        val imageFile = navArgs.imageFile
        val imageUri = navArgs.imageUri
        val isBackCamera = navArgs.isBackCamera

        if (isBackCamera == 0) {
            binding.ivPhoto.setImageURI(imageUri)
        }
        else {
            val imageResult = rotateBitmap(
                BitmapFactory.decodeFile(imageFile.path),
                isBackCamera
            )
            binding.ivPhoto.setImageBitmap(imageResult)
        }

        viewModel.isStoryUploaded.observe(this.viewLifecycleOwner) { result ->
            if (result != null)
            {
                when (result)
                {
                    is Result.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is Result.Success -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, getString(R.string.photo_success_story_uploaded), Toast.LENGTH_SHORT).show()
                        (activity as StoryActivity).moveToMain()
                    }
                    is Result.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, result.error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.btnSwitchLocation.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                storyLat = null
                storyLng = null
                binding.btnSwitchLocation.isChecked = (storyLat != null && storyLng != null)
            } else {
                getMyLocation()
                GlobalScope.launch(Dispatchers.Main) {
                    delay(500L)
                    binding.btnSwitchLocation.isChecked = (storyLat != null && storyLng != null)
                }
            }
        }

        binding.btnUpload.setOnClickListener { _ ->
            val description = binding.edAddDescription.text.toString()

            if (description.isEmpty()) {
                binding.edAddDescription.error = getString(R.string.ed_validation_blank_error)
            }
            else
            {
                context?.let {
                    MaterialAlertDialogBuilder(it)
                        .setTitle(getString(R.string.fixed_text_upload_dialog_title))
                        .setMessage(getString(R.string.fixed_text_upload_story_msg))
                        .setNegativeButton(getString(R.string.fixed_text_cancel)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setPositiveButton(getString(R.string.fixed_text_yes)) { _, _ ->
                            token?.let { it1 -> viewModel.uploadStory(it1, imageFile, isBackCamera, description, storyLat, storyLng) }
                        }
                        .show()
                }
            }
        }

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            } else {
                Toast.makeText(requireContext(), "Can't access your location. Check your permissions for this app", Toast.LENGTH_SHORT).show()
            }
        }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireActivity().applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val manager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            if (!statusOfGPS) {
                Toast.makeText(requireContext(), "Turn on your gps to show your location on story", Toast.LENGTH_SHORT).show()
            } else {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        storyLat = location.latitude.toString()
                        storyLng = location.longitude.toString()
                    }
                }
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}