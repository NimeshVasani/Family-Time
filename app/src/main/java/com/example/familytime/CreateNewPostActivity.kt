package com.example.familytime

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.familytime.adapters.CreatePostFamiliesAdapter
import com.example.familytime.databinding.ActivityCreateNewPostBinding
import com.example.familytime.models.Family
import com.example.familytime.models.Posts
import com.example.familytime.models.User
import com.example.familytime.other.Resource
import com.example.familytime.other.Utils.currentUserUID
import com.example.familytime.other.Utils.getCurrentDateLiveData
import com.example.familytime.other.Utils.getCurrentTimeLiveData
import com.example.familytime.viewmodels.firestore.FireStoreViewModel
import com.example.familytime.viewmodels.firestore.FireStoreViewModelFactory
import com.example.familytime.viewmodels.storage.StorageViewModel
import com.example.familytime.viewmodels.storage.StorageViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class CreateNewPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateNewPostBinding
    private lateinit var createPostFamiliesAdapter: CreatePostFamiliesAdapter
    private var newPostUri: String = ""

    private var currentUser: MutableLiveData<User> = MutableLiveData()
    private var currentUserName: String = ""

    @Inject
    lateinit var fireStoreViewModelFactory: FireStoreViewModelFactory
    private val fireStoreViewModel by viewModels<FireStoreViewModel> { fireStoreViewModelFactory }

    @Inject
    lateinit var storageViewModelFactory: StorageViewModelFactory
    private val storageViewModel by viewModels<StorageViewModel> { storageViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpAdapter()
        createPostFamiliesAdapter.getStorageReference(firebaseStorage = storageViewModel.getStorageReference())

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

        currentUser.observe(this) { user ->
            if (!user.families.isNullOrEmpty()) {
                currentUserName = user.name!!
                loadFamilies(user.families)
            }
        }

        getCurrentDateLiveData().observe(this) {
            binding.tvNewPostDate.text = it
        }

        getCurrentTimeLiveData().observe(this) {
            binding.tvNewPostTime.text = it
        }

        binding.newPostImg.setOnClickListener {
            val cameraIntent = Intent(this, CameraActivity::class.java)
            cameraActivityLauncher.launch(cameraIntent)
        }

        binding.newPostBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.newPostShareBtn.setOnClickListener {
            val postDescription = binding.newPostDescription.text.toString()
            binding.newPostDescription.clearFocus()

            if (newPostUri.isNotBlank() || postDescription.isNotBlank()) {
                sharePost(
                    families = createPostFamiliesAdapter.setSharedToFamilies()
                )
            } else {
                Snackbar.make(binding.root, "Write and/or Share Image...", Snackbar.LENGTH_LONG)
                    .show()

            }
        }

    }

    private fun setUpAdapter() {
        createPostFamiliesAdapter = CreatePostFamiliesAdapter()
        binding.createPostFamiliesRecycler.layoutManager = LinearLayoutManager(this)
        binding.createPostFamiliesRecycler.adapter = createPostFamiliesAdapter
    }

    private var cameraActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult? ->
        if (result?.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            if (data != null && data.data != null) {
                newPostUri = data.data.toString()
                binding.newPostImg.setImageURI(data.data)
            }
        }
    }

    private fun loadFamilies(familyIdList: MutableList<String>) {
        val familyList: MutableList<Family> = mutableListOf()
        for (familyId in familyIdList) {
            fireStoreViewModel.getFamilyById(familyId).observe(this) { resources ->
                when (resources) {
                    is Resource.Success -> {
                        familyList.add(resources.data!!)
                        createPostFamiliesAdapter.asyncDiffer.submitList(familyList)
                        createPostFamiliesAdapter.notifyDataSetChanged()

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

    private fun sharePost(families: MutableList<String>) {
        val postDescription = binding.newPostDescription.text.toString()
        val postUid = UUID.randomUUID().toString()
        val date = binding.tvNewPostDate.text.toString()
        val time = binding.tvNewPostTime.text.toString()
        val dateTimeString = "$date $time"
        val dateTimeFormat = SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.ENGLISH)
        val combinedTimestamp = dateTimeFormat.parse(dateTimeString)


        if (newPostUri.isNotBlank()) {
            storageViewModel.saveImageInStorage(
                imageUri = Uri.parse(newPostUri),
                pathName = "posts"
            ).observe(this) { resources ->
                when (resources) {
                    is Resource.Success -> {
                        sharePostToFireStore(
                            Posts(
                                sharedByUserName = currentUserName,
                                postUid = postUid,
                                createdBy = currentUserUID,
                                postsUri = resources.data.toString(),
                                timestamp = combinedTimestamp?.let { Timestamp(it) },
                                description = postDescription,
                                location = binding.tvNewPostLocation.text.toString()
                            ), families
                        )
                    }

                    is Resource.Error -> {
                        Snackbar.make(
                            binding.root,
                            resources.message.toString(),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    is Resource.Loading -> {
                        binding.createNewPostLoading.apply {
                            visibility = View.VISIBLE
                            show()
                        }
                    }
                }

            }
        } else {
            sharePostToFireStore(
                Posts(
                    sharedByUserName = currentUserName,
                    postUid = postUid,
                    createdBy = currentUserUID,
                    postsUri = null,
                    timestamp = combinedTimestamp?.let { Timestamp(it) },
                    description = postDescription,
                    location = binding.tvNewPostLocation.text.toString()
                ), families
            )
        }
    }

    private fun sharePostToFireStore(posts: Posts, families: MutableList<String>) {
        fireStoreViewModel.shareNewPosts(posts).observe(this) { resource ->
            when (resource) {
                is Resource.Success -> {
                    updatePostsUid(resource.data.toString(), families)
                }

                is Resource.Error -> {
                    Snackbar.make(
                        binding.root,
                        resource.message.toString(),
                        Snackbar.LENGTH_LONG
                    ).show()
                }

                is Resource.Loading -> {
                    binding.createNewPostLoading.apply {
                        visibility = View.VISIBLE
                        show()
                    }
                }

            }
        }
    }

    private fun updatePostsUid(postsUid: String, families: MutableList<String>) {
        fireStoreViewModel.updatePostUid(postsUid, families).observe(this) { resource ->
            when (resource) {
                is Resource.Success -> {
                    Snackbar.make(
                        binding.root,
                        "Success",
                        Snackbar.LENGTH_LONG
                    ).show()
                    this@CreateNewPostActivity.finish()
                }

                is Resource.Error -> {
                    Snackbar.make(
                        binding.root,
                        resource.message.toString(),
                        Snackbar.LENGTH_LONG
                    ).show()
                }

                is Resource.Loading -> {
                    binding.createNewPostLoading.apply {
                        visibility = View.VISIBLE
                        show()
                    }
                }

            }

        }

    }
}
