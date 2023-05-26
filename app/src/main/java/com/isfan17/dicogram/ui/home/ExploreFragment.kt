package com.isfan17.dicogram.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.isfan17.dicogram.R
import com.isfan17.dicogram.data.model.Story
import com.isfan17.dicogram.databinding.FragmentExploreBinding
import com.isfan17.dicogram.databinding.TooltipMarkerBinding
import com.isfan17.dicogram.ui.viewmodels.MainViewModel
import com.isfan17.dicogram.ui.viewmodels.ViewModelFactory
import com.isfan17.dicogram.utils.Constants.Companion.USER_PREF_TOKEN_NAME
import com.isfan17.dicogram.utils.Helper
import com.isfan17.dicogram.utils.Result
import kotlin.math.pow

class ExploreFragment : Fragment(), OnMapReadyCallback, GoogleMap.InfoWindowAdapter {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    lateinit var viewModel: MainViewModel

    private lateinit var mMap: GoogleMap
    private val boundsBuilder = LatLngBounds.Builder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory: ViewModelFactory = ViewModelFactory.getInstance(requireActivity())
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        val mapFragment = (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment)
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        viewModel.getUserPreferences(USER_PREF_TOKEN_NAME).observe(this.viewLifecycleOwner) { token ->
            viewModel.getLocatedStories("Bearer $token")
        }

        viewModel.locatedStories.observe(this.viewLifecycleOwner) { result ->
            if (result != null)
            {
                when (result)
                {
                    is Result.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is Result.Success -> {
                        binding.progressBar.visibility = View.GONE

                        val stories = result.data
                        stories.forEach { story ->
                            val latLng = LatLng(
                                story.lat?.toDouble() ?: 0.0,
                                story.lon?.toDouble() ?: 0.0
                            )
                            mMap.addMarker(
                                MarkerOptions()
                                    .position(latLng)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                            )?.tag = story
                            boundsBuilder.include(latLng)
                        }

                        val bounds: LatLngBounds = boundsBuilder.build()
                        mMap.animateCamera(
                            CameraUpdateFactory.newLatLngBounds(
                                bounds,
                                resources.displayMetrics.widthPixels,
                                resources.displayMetrics.heightPixels,
                                200
                            )
                        )
                    }
                    is Result.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, result.error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        mMap.setInfoWindowAdapter(this)
        mMap.setOnMarkerClickListener { marker ->
            val zoom = mMap.cameraPosition.zoom
            val cu = CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    marker.position.latitude + 150 / 2.0.pow(zoom.toDouble()),
                    marker.position.longitude
                ),
                zoom
            )
            mMap.animateCamera(cu, 600, null)
            marker.showInfoWindow()
            return@setOnMarkerClickListener true
        }
        mMap.setOnInfoWindowClickListener { marker ->
            val story: Story = marker.tag as Story
            (activity as MainActivity).moveToDetail(story)
        }

        setMapStyle()

        binding.btnShowLocation.setOnClickListener {
            getMyLocation()
        }
    }

    override fun getInfoContents(marker: Marker): View? = null

    override fun getInfoWindow(marker: Marker): View {
        val tooltipBinding = TooltipMarkerBinding.inflate(LayoutInflater.from(requireContext()))
        val story: Story = marker.tag as Story
        tooltipBinding.apply {
            tvName.text = story.name
            tvLocation.text = Helper.getAddressFromLatLng(requireContext(), marker.position.latitude, marker.position.longitude)
            ivPhoto.setImageBitmap(Helper.bitmapFromURL(requireContext(), story.photoUrl))
        }
        return tooltipBinding.root
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style))
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", exception)
        }
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
            mMap.isMyLocationEnabled = true
            val manager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            if (!statusOfGPS) {
                Toast.makeText(requireContext(), "Turn on your gps to show your location on map", Toast.LENGTH_SHORT).show()
            } else {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    // If the location is not null, move the camera to the user's location
                    location?.let {
                        val latLng = LatLng(location.latitude, location.longitude)
                        val cu = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                        mMap.animateCamera(cu)
                    }
                }
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    companion object {
        private const val TAG = "ExploreFragment"
    }
}