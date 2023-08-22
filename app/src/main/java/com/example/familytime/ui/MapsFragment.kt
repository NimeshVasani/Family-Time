package com.example.familytime.ui

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.familytime.R
import com.example.familytime.adapters.MapUsersAdapter
import com.example.familytime.databinding.FragmentMapsBinding
import com.example.familytime.models.Family
import com.example.familytime.models.User
import com.example.familytime.other.Resource
import com.example.familytime.other.Utils.currentUserUID
import com.example.familytime.viewmodels.chats.ChatsViewModel
import com.example.familytime.viewmodels.chats.ChatsViewModelFactory
import com.example.familytime.viewmodels.firestore.FireStoreViewModel
import com.example.familytime.viewmodels.firestore.FireStoreViewModelFactory
import com.example.familytime.viewmodels.storage.StorageViewModel
import com.example.familytime.viewmodels.storage.StorageViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.security.MessageDigest
import javax.inject.Inject

@AndroidEntryPoint
class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var binding: FragmentMapsBinding
    private lateinit var map: GoogleMap
    private lateinit var mapUsersAdapter: MapUsersAdapter
    private var currentUser: MutableLiveData<User> = MutableLiveData()
    private val families: MutableList<Family> = mutableListOf()
    private var markers: MutableList<Marker> = mutableListOf()

    @Inject
    lateinit var fireStoreViewModelFactory: FireStoreViewModelFactory
    private val fireStoreViewModel by viewModels<FireStoreViewModel> { fireStoreViewModelFactory }

    @Inject
    lateinit var storageViewModelFactory: StorageViewModelFactory
    private val storageViewModel by viewModels<StorageViewModel> { storageViewModelFactory }

    @Inject
    lateinit var chatsViewModelFactory: ChatsViewModelFactory
    private val chatsViewModel by viewModels<ChatsViewModel> { chatsViewModelFactory }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        setUpAdapter()
        mapUsersAdapter.getStorageReference(storageViewModel.getStorageReference())
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.mapType = GoogleMap.MAP_TYPE_SATELLITE
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isZoomGesturesEnabled = true
        map.setPadding(0, 0, 0, 700)
        setUpMap()
        fireStoreViewModel.getCurrentUser().observe(viewLifecycleOwner) { resources ->
            when (resources) {
                is Resource.Success -> {
                    currentUser.value = resources.data!!
                }

                is Resource.Error -> {
                    Snackbar.make(binding.root, resources.message.toString(), Snackbar.LENGTH_LONG)
                        .show()
                }

                is Resource.Loading -> {
                }

            }
        }

        currentUser.observe(viewLifecycleOwner) { user ->
            if (!user.families.isNullOrEmpty()) {
                loadFamilies(user.families)
            }
        }
        binding.mapsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                clearMarkers()
                loadMembers(families[position].members)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

    }

    override fun onMarkerClick(p0: Marker): Boolean {
        return false
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        map.isMyLocationEnabled = true

    }

    private fun placeMarkerOnMap(locations: MutableList<LatLng>) {
        for (location in locations) {
            if (!mapUsersAdapter.asyncDiffer.currentList[locations.indexOf(location)].profilePic.isNullOrBlank()) {
                Glide.with(this)
                    .asBitmap()
                    .load(
                        storageViewModel.getStorageReference()
                            .getReferenceFromUrl(mapUsersAdapter.asyncDiffer.currentList[locations.indexOf(location)].profilePic.toString())
                    )
                    .circleCrop()
                    .override(100, 100)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            bitmap: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            // Create a custom marker icon
                            val icon = BitmapDescriptorFactory.fromBitmap(bitmap)
                            val markerOptions = MarkerOptions()
                                .position(location)
                                .icon(icon)

                            val marker = map.addMarker(markerOptions)
                            if (marker != null) {
                                markers.add(marker)
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {

                        }
                    })
            } else {
                val markerOptions = MarkerOptions()
                    .position(location)
                val marker = map.addMarker(markerOptions)
                if (marker != null) {
                    markers.add(marker)
                }
            }
        }
    }

    private fun clearMarkers() {
        for (marker in markers) {
            marker.remove()
        }
        markers.clear()
    }

    private fun loadFamilies(familyIdList: MutableList<String>) {
        val familyList: MutableList<String> = mutableListOf()
        for (familyId in familyIdList) {
            fireStoreViewModel.getFamilyById(familyId).observe(viewLifecycleOwner) { resources ->
                when (resources) {
                    is Resource.Success -> {
                        familyList.add(resources.data!!.familyName)
                        families.add(resources.data)
                        if (familyIdList.size == familyList.size) {
                            val adapter = ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_spinner_dropdown_item,
                                familyList
                            )
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                            binding.mapsSpinner.adapter = adapter
                        }
                    }

                    is Resource.Error -> {
                        Snackbar.make(
                            binding.root,
                            resources.message.toString(),
                            Snackbar.LENGTH_LONG
                        ).show()

                    }

                    is Resource.Loading -> {
                    }
                }
            }
        }
    }

    private fun loadMembers(membersIdList: MutableList<String>) {
        mapUsersAdapter.asyncDiffer.submitList(mutableListOf())
        mapUsersAdapter.notifyDataSetChanged()

        val membersList: MutableList<User> = mutableListOf()
        for (membersId in membersIdList) {
            fireStoreViewModel.searchUserByUserId(membersId)
                .observe(viewLifecycleOwner) { resources ->
                    when (resources) {
                        is Resource.Success -> {
                            membersList.add(resources.data!!)
                            if (membersIdList.size == membersList.size) {
                                mapUsersAdapter.asyncDiffer.submitList(membersList)
                                mapUsersAdapter.notifyDataSetChanged()
                                loadCurrentLiveLocation(membersIdList)

                            }
                        }

                        is Resource.Error -> {
                            Snackbar.make(
                                binding.root,
                                resources.message.toString(),
                                Snackbar.LENGTH_LONG
                            )
                                .show()
                        }

                        is Resource.Loading -> {}

                    }
                }
        }
    }

    private fun loadCurrentLiveLocation(userIdList: MutableList<String>) {
        val locations: MutableList<LatLng> = mutableListOf()
        for (userId in userIdList) {
            chatsViewModel.getUserLiveLocation(userId).observe(viewLifecycleOwner) { resources ->
                when (resources) {
                    is Resource.Success -> {
                        val locationData = resources.data
                        val latitude = locationData?.latitude ?: 0.0
                        val longitude = locationData?.longitude ?: 0.0
                        locations.add(LatLng(latitude, longitude))

                        if (userIdList.size == locations.size) {
                            placeMarkerOnMap(locations)
                        }
                        if (userId == currentUserUID) {
                            map.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        latitude,
                                        longitude
                                    ), 12f
                                )
                            )
                        }

                    }

                    is Resource.Error -> {

                    }

                    is Resource.Loading -> {

                    }

                }
            }
        }
    }


    private fun setUpAdapter() {
        mapUsersAdapter = MapUsersAdapter()
        binding.mapsUserList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.mapsUserList.adapter = mapUsersAdapter
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}

