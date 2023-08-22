package com.example.familytime

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.example.familytime.adapters.AllPostsAdapter
import com.example.familytime.adapters.FamilyMembersAdapter
import com.example.familytime.adapters.MakeMemberRequestAdapter
import com.example.familytime.adapters.YourPostsAdapter
import com.example.familytime.databinding.ActivityFamilyBinding
import com.example.familytime.databinding.PopUpAddMembersBinding
import com.example.familytime.models.Family
import com.example.familytime.models.Posts
import com.example.familytime.models.User
import com.example.familytime.other.Resource
import com.example.familytime.other.Utils.currentUserUID
import com.example.familytime.viewmodels.firestore.FireStoreViewModel
import com.example.familytime.viewmodels.firestore.FireStoreViewModelFactory
import com.example.familytime.viewmodels.storage.StorageViewModel
import com.example.familytime.viewmodels.storage.StorageViewModelFactory
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FamilyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFamilyBinding
    private lateinit var bindingPopUpView: PopUpAddMembersBinding
    private lateinit var familyMembersAdapter: FamilyMembersAdapter
    private lateinit var yourPostAdapter: YourPostsAdapter
    private lateinit var allPostAdapter: AllPostsAdapter
    private lateinit var popupWindow: PopupWindow
    private lateinit var makeMemberRequestAdapter: MakeMemberRequestAdapter
    private var familyId: String = ""

    private var currentFamily: MutableLiveData<Family> = MutableLiveData()
    private var currentUser: MutableLiveData<User> = MutableLiveData()
    private val allPosts: MutableLiveData<MutableList<Posts>> = MutableLiveData()


    @Inject
    lateinit var fireStoreViewModelFactory: FireStoreViewModelFactory
    private val fireStoreViewModel by viewModels<FireStoreViewModel> { fireStoreViewModelFactory }


    @Inject
    lateinit var storageViewModelFactory: StorageViewModelFactory
    private val storageViewModel by viewModels<StorageViewModel> { storageViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFamilyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        bindingPopUpView = PopUpAddMembersBinding.inflate(inflater)
        setUpAdapters()
        familyId = intent.extras!!.getString("familyId")!!
        familyMembersAdapter.getStorageReference(storageViewModel.getStorageReference())
        yourPostAdapter.getStorageReference(storageViewModel.getStorageReference())
        allPostAdapter.getStorageReference(storageViewModel.getStorageReference())

        binding.tvAddMembers.setOnClickListener {
            popupWindow = PopupWindow(
                bindingPopUpView.root,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
            )
            popupWindow.showAtLocation(binding.root, Gravity.CENTER, 0, 0)
        }
        binding.familyBackBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        bindingPopUpView.closePopUpBtn.setOnClickListener {
            popupWindow.dismiss()
        }
        bindingPopUpView.popUpSearchBtn.setOnClickListener {
            val searchUserName = bindingPopUpView.popUpMemberSearch.text.toString()
            fireStoreViewModel.searchUserByName(userName = searchUserName)
                .observe(this) { resource ->
                    Snackbar.make(bindingPopUpView.root, "Clicked", Snackbar.LENGTH_LONG).show()

                    when (resource) {
                        is Resource.Success -> {
                            makeMemberRequestAdapter.asyncDiffer.submitList(resource.data)
                            makeMemberRequestAdapter.notifyDataSetChanged()
                            bindingPopUpView.popUpMemberSearch.clearFocus()
                        }

                        is Resource.Error -> {
                            Snackbar.make(
                                bindingPopUpView.root,
                                resource.message.toString(),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }

                        is Resource.Loading -> {
                            Snackbar.make(binding.root, "Loading", Snackbar.LENGTH_LONG).show()
                        }
                    }

                }
        }
        fireStoreViewModel.getCurrentUser().observe(this) { resources ->
            when (resources) {
                is Resource.Success -> {
                    currentUser.value = resources.data!!
                }

                is Resource.Error -> {
                    Snackbar.make(binding.root, resources.message.toString(), Snackbar.LENGTH_LONG)
                        .show()
                }

                is Resource.Loading -> {
                    Snackbar.make(binding.root, "Loading", Snackbar.LENGTH_LONG).show()
                }

            }

        }

        fireStoreViewModel.getFamilyById(familyId).observe(this) { resources ->
            when (resources) {
                is Resource.Success -> {
                    currentFamily.value = resources.data!!
                }

                is Resource.Error -> {
                    Snackbar.make(binding.root, resources.message.toString(), Snackbar.LENGTH_LONG)
                        .show()
                }

                is Resource.Loading -> {

                }

            }
        }

        currentUser.observe(this) {
            if (!it.profilePic.isNullOrBlank()) {
                Glide.with(this)
                    .load(
                        storageViewModel.getStorageReference()
                            .getReferenceFromUrl(it.profilePic.toString())
                    )
                    .into(binding.imgUserFamily)
            } else {
                binding.imgUserFamily.setImageResource(R.drawable.img_user_logo)
            }
        }
        currentFamily.observe(this) {
            binding.familyTitleMain.text = it.familyName
            binding.familyNameToolbar.text = it.familyName

            Glide.with(this)
                .load(
                    storageViewModel.getStorageReference()
                        .getReferenceFromUrl(it.familyProfilePicId!!)
                )
                .into(binding.imgFamilyCover)

            if (!it.familyDescription.isNullOrBlank()) {
                binding.tvFamilyDetails.text = it.familyDescription.toString()
            }
            if (!it.familyLocation.isNullOrBlank()) {
                binding.tvFamilyLocation.text = it.familyLocation.toString()
            }
            loadMembers(it.members)
            binding.tvMembersCount.text = "${it.members.size} members"
        }

        allPosts.observe(this) { posts ->
            allPostAdapter.asyncDiffer.submitList(posts)
            allPostAdapter.notifyDataSetChanged()
            yourPostAdapter.asyncDiffer.submitList(posts.sortedByDescending { it.createdBy == currentUserUID })
            yourPostAdapter.notifyDataSetChanged()
            // Delay to allow for the RecyclerView to update before scrolling
            binding.recyclerYourPosts.postDelayed({
                binding.recyclerYourPosts.smoothScrollToPosition(0)
            }, 100L)

        }

        makeMemberRequestAdapter.setOnItemClickListener { uId ->
            fireStoreViewModel.updateFamilyJoinReq(uId, familyId, true).observe(this) { resources ->
                when (resources) {
                    is Resource.Success -> {
                        Snackbar.make(
                            binding.root,
                            "Join Req Sent",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        makeMemberRequestAdapter.changeReqStatus()
                    }

                    is Resource.Error -> {
                        Snackbar.make(
                            binding.root,
                            resources.message.toString(),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }

                    is Resource.Loading -> {

                    }

                }
            }
        }
    }

    private fun setUpAdapters() {
        familyMembersAdapter = FamilyMembersAdapter()
        binding.recyclerFamilyMembers.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerFamilyMembers.adapter = familyMembersAdapter

        yourPostAdapter = YourPostsAdapter()
        binding.recyclerYourPosts.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerYourPosts.adapter = yourPostAdapter

        allPostAdapter = AllPostsAdapter()
        binding.recyclerAllPosts.layoutManager =
            StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        binding.recyclerAllPosts.adapter = allPostAdapter

        makeMemberRequestAdapter = MakeMemberRequestAdapter()
        bindingPopUpView.recyclerPopUpFamilyMembers.layoutManager = LinearLayoutManager(this)
        bindingPopUpView.recyclerPopUpFamilyMembers.adapter = makeMemberRequestAdapter
    }

    private fun loadMembers(membersIdList: MutableList<String>) {
        val membersList: MutableList<User> = mutableListOf()
        for (membersId in membersIdList) {
            fireStoreViewModel.searchUserByUserId(membersId).observe(this) { resources ->
                when (resources) {
                    is Resource.Success -> {
                        membersList.add(resources.data!!)
                        familyMembersAdapter.asyncDiffer.submitList(membersList)
                        familyMembersAdapter.notifyDataSetChanged()
                        loadSpecificFamilyPost(familyId)
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

    private fun loadSpecificFamilyPost(familyId: String) {

        fireStoreViewModel.getSpecificFamilyPost(familyId)
            .observe(this) { resources ->
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

                        allPosts.value = mutableListOf()
                    }

                    is Resource.Loading -> {
                    }
                }
            }

    }

    private fun sortAllPostsByTime(postIds: MutableList<String>) {
        fireStoreViewModel.getPostsByTimeStamp(postIds).observe(this) { resources ->
            when (resources) {
                is Resource.Success -> {
                    allPosts.value = resources.data!!
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
                }
            }
        }
    }

}