package com.example.familytime.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.familytime.CreateNewFamilyActivity
import com.example.familytime.CreateNewPostActivity
import com.example.familytime.R
import com.example.familytime.adapters.AllFamiliesNameAdapter
import com.example.familytime.adapters.AllFamiliesPostsAdapter
import com.example.familytime.databinding.FragmentDashboardBinding
import com.example.familytime.databinding.PopUpAddMembersBinding
import com.example.familytime.databinding.PopUpPostDescriptionBinding
import com.example.familytime.models.Family
import com.example.familytime.models.LocationData
import com.example.familytime.models.Posts
import com.example.familytime.models.User
import com.example.familytime.other.Resource
import com.example.familytime.other.Utils
import com.example.familytime.viewmodels.chats.ChatsViewModel
import com.example.familytime.viewmodels.chats.ChatsViewModelFactory
import com.example.familytime.viewmodels.firestore.FireStoreViewModel
import com.example.familytime.viewmodels.firestore.FireStoreViewModelFactory
import com.example.familytime.viewmodels.storage.StorageViewModel
import com.example.familytime.viewmodels.storage.StorageViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var bindingPopUpView: PopUpPostDescriptionBinding
    private lateinit var popupWindow: PopupWindow


    private lateinit var allFamiliesPostsAdapter: AllFamiliesPostsAdapter
    private lateinit var allFamiliesNameAdapter: AllFamiliesNameAdapter
    private val familyListUId: MutableLiveData<MutableList<String>> = MutableLiveData()

    private var currentUser: MutableLiveData<User> = MutableLiveData()
    private val allPosts: MutableLiveData<MutableList<Posts>> = MutableLiveData()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        bindingPopUpView = PopUpPostDescriptionBinding.inflate(inflater)

        setUpAdapters()
        allFamiliesPostsAdapter.getStorageReference(storageViewModel.getStorageReference())

        binding.chatBtn.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_chatsGraph)
        }

        binding.tvCreateNewFamily.setOnClickListener {
            allFamiliesNameAdapter.clearSelectedItem()
            binding.allFeedBtn.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.orange
                )
            )
            startActivity(Intent(requireContext(), CreateNewFamilyActivity::class.java))
        }

        binding.createMemoriesBtn.setOnClickListener {
            startActivity(Intent(requireContext(), CreateNewPostActivity::class.java))
        }

        binding.userProfileBtn.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_profile_set_up_graph)
        }

        allFamiliesPostsAdapter.setOnItemClickListener { post ->
            popupWindow = PopupWindow(
                bindingPopUpView.root,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
            )
            popupWindow.animationStyle = R.style.popup_window_animation
            popupWindow.showAtLocation(binding.root, Gravity.CENTER, 0, 0)
            if (!post.postsUri.isNullOrBlank()) {
                Glide.with(this)
                    .load(
                        storageViewModel.getStorageReference().getReferenceFromUrl(post.postsUri!!)
                    )
                    .into(bindingPopUpView.postPopUpImg)
            }
            bindingPopUpView.apply {
                popUpDate.text = post.timestamp?.toDate().toString()
                popUpLocation.text = post.location ?: ""
                popUpDescription.text = post.description.toString()
                popUpUserName.text = post.sharedByUserName.toString()
            }
        }

        bindingPopUpView.closePopUpBtn.setOnClickListener {
            popupWindow.dismiss()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.container.setOnRefreshListener {

            currentUser.observe(viewLifecycleOwner) { user ->
                if (!user.families.isNullOrEmpty()) {
                    loadFamilies(user.families)
                }
            }

            allPosts.observe(viewLifecycleOwner) {
                allFamiliesPostsAdapter.asyncDiffer.submitList(it!!)
                allFamiliesPostsAdapter.notifyDataSetChanged()
                // Delay to allow for the RecyclerView to update before scrolling
                binding.recyclerAllFamilyPosts.postDelayed({
                    binding.recyclerAllFamilyPosts.smoothScrollToPosition(0)
                }, 100L)
            }

        }

        allFamiliesNameAdapter.setOnItemClickListener {
            loadSpecificFamilyPost(it)
            binding.allFeedBtn.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
        }
        binding.allFeedBtn.apply {
            this.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.orange
                )
            )
            setOnClickListener {
                familyListUId.observe(viewLifecycleOwner) { familyUIds ->
                    loadAllPosts(familyUIds)
                }
                allFamiliesNameAdapter.clearSelectedItem()
                it.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.orange
                    )
                )
            }
        }
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
                    binding.container.isRefreshing = true
                }

            }
        }

        currentUser.observe(viewLifecycleOwner) { user ->
            if (!user.profilePic.isNullOrBlank()) {
                Glide.with(this)
                    .load(
                        storageViewModel.getStorageReference()
                            .getReferenceFromUrl(user.profilePic!!)
                    )
                    .into(binding.userProfileBtn)
            }
            if (!user.families.isNullOrEmpty()) {
                loadFamilies(user.families)
            }
        }

        allPosts.observe(viewLifecycleOwner) {
            allFamiliesPostsAdapter.asyncDiffer.submitList(it!!)
            allFamiliesPostsAdapter.notifyDataSetChanged()
            // Delay to allow for the RecyclerView to update before scrolling
            binding.recyclerAllFamilyPosts.postDelayed({
                binding.recyclerAllFamilyPosts.smoothScrollToPosition(0)
            }, 100L)
        }
    }

    override fun onStart() {
        super.onStart()
        familyListUId.observe(viewLifecycleOwner) { familyUIds ->
            loadAllPosts(familyUIds)
        }

        currentUser.observe(viewLifecycleOwner) { user ->
            if (!user.profilePic.isNullOrBlank()) {
                Glide.with(this)
                    .load(
                        storageViewModel.getStorageReference()
                            .getReferenceFromUrl(user.profilePic!!)
                    )
                    .into(binding.userProfileBtn)
            }
            if (!user.families.isNullOrEmpty()) {
                loadFamilies(user.families)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        familyListUId.observe(viewLifecycleOwner) { familyUIds ->
            loadAllPosts(familyUIds)
        }

        currentUser.observe(viewLifecycleOwner) { user ->
            if (!user.profilePic.isNullOrBlank()) {
                Glide.with(this)
                    .load(
                        storageViewModel.getStorageReference()
                            .getReferenceFromUrl(user.profilePic!!)
                    )
                    .into(binding.userProfileBtn)
            }
            if (!user.families.isNullOrEmpty()) {
                loadFamilies(user.families)
            }
        }
    }

    private fun setUpAdapters() {
        allFamiliesPostsAdapter = AllFamiliesPostsAdapter()
        val layoutManager = GridLayoutManager(requireContext(), 2)// Your layout manager
        binding.recyclerAllFamilyPosts.layoutManager = layoutManager
        binding.recyclerAllFamilyPosts.adapter = allFamiliesPostsAdapter
        binding.recyclerAllFamilyPosts.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                // Check if the user is at the last item
                if (lastVisibleItemPosition == totalItemCount - 1) {
                    // Allow the user to scroll a bit more by adjusting the RecyclerView scroll position
                    recyclerView.setPadding(40, 40, 0, 300) // Adjust the value as needed
                }
            }
        })

        allFamiliesNameAdapter = AllFamiliesNameAdapter()

        binding.recyclerFamiliesNames.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerFamiliesNames.adapter = allFamiliesNameAdapter
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        saveLocationToFirebase()

    }

    private fun loadFamilies(familyIdList: MutableList<String>) {
        val familyList: MutableList<Family> = mutableListOf()
        familyListUId.value = familyIdList

        for (familyId in familyIdList) {
            fireStoreViewModel.getFamilyById(familyId).observe(viewLifecycleOwner) { resources ->
                when (resources) {
                    is Resource.Success -> {
                        familyList.add(resources.data!!)
                        familyList.sortBy { it }
                        allFamiliesNameAdapter.asyncDiffer.submitList(familyList)
                        allFamiliesNameAdapter.notifyDataSetChanged()
                    }

                    is Resource.Error -> {
                        Snackbar.make(
                            binding.root,
                            resources.message.toString(),
                            Snackbar.LENGTH_LONG
                        ).show()

                    }

                    is Resource.Loading -> {
                        binding.container.isRefreshing = true

                    }
                }
            }
        }
    }

    private fun loadAllPosts(familiesId: MutableList<String>) {

        fireStoreViewModel.getAllFamiliesPosts(familiesId)
            .observe(viewLifecycleOwner) { resources ->
                when (resources) {
                    is Resource.Success -> {
                        sortAllPostsByTime(resources.data!!)
                    }

                    is Resource.Error -> {
                        Snackbar.make(
                            binding.root,
                            resources.message.toString(),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    is Resource.Loading -> {
                        binding.container.isRefreshing = true

                    }
                }
            }
    }

    private fun loadSpecificFamilyPost(familyId: String) {

        fireStoreViewModel.getSpecificFamilyPost(familyId)
            .observe(viewLifecycleOwner) { resources ->
                when (resources) {
                    is Resource.Success -> {
                        sortAllPostsByTime(resources.data!!)
                    }

                    is Resource.Error -> {
                        Snackbar.make(
                            binding.root,
                            resources.message.toString(),
                            Snackbar.LENGTH_LONG
                        ).show()
                        binding.container.isRefreshing = false

                        allPosts.value = mutableListOf()
                    }

                    is Resource.Loading -> {
                        binding.container.isRefreshing = true

                    }
                }
            }

    }

    private fun sortAllPostsByTime(postIds: MutableList<String>) {
        fireStoreViewModel.getPostsByTimeStamp(postIds).observe(viewLifecycleOwner) { resources ->
            when (resources) {
                is Resource.Success -> {
                    allPosts.value = resources.data!!
                    binding.container.isRefreshing = false
                }

                is Resource.Error -> {
                    Snackbar.make(
                        binding.root,
                        resources.message.toString(),
                        Snackbar.LENGTH_LONG
                    ).show()

                    allPosts.value = mutableListOf()
                }

                is Resource.Loading -> {
                    binding.container.isRefreshing = true

                }
            }
        }
    }


    private fun saveLocationToFirebase() {
        if (checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful && task.result != null) {
                    val location = task.result
                    val userLocation = LocationData(location.latitude, location.longitude)
                    setUserLiveLocation(Utils.currentUserUID, userLocation)
                }
            }
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 123)
        }
    }

    private fun setUserLiveLocation(userId: String, userLocation: LocationData) {
        chatsViewModel.setUsersLiveLocation(userId, userLocation)
            .observe(viewLifecycleOwner) { resources ->
                when (resources) {
                    is Resource.Success -> {

                    }

                    is Resource.Error -> {
                        Snackbar.make(
                            binding.root,
                            resources.message.toString(),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    is Resource.Loading -> {}
                }
            }
    }

}