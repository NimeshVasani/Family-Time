package com.example.familytime.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.familytime.CameraActivity
import com.example.familytime.R
import com.example.familytime.adapters.MakeFamilyRequestAdapter
import com.example.familytime.databinding.FragmentUserProfileBinding
import com.example.familytime.models.CommonForRequest
import com.example.familytime.models.User
import com.example.familytime.other.Resource
import com.example.familytime.other.Utils.currentUserUID
import com.example.familytime.viewmodels.auth.AuthViewModel
import com.example.familytime.viewmodels.auth.AuthViewModelFactory
import com.example.familytime.viewmodels.firestore.FireStoreViewModel
import com.example.familytime.viewmodels.firestore.FireStoreViewModelFactory
import com.example.familytime.viewmodels.storage.StorageViewModel
import com.example.familytime.viewmodels.storage.StorageViewModelFactory
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UserProfileFragment : Fragment() {

    private lateinit var binding: FragmentUserProfileBinding
    private lateinit var makeFamilyRequestAdapter: MakeFamilyRequestAdapter
    private var familyPicUri: String = ""
    private var switchUserAndFamily: MutableLiveData<Boolean> = MutableLiveData()

    @Inject
    lateinit var fireStoreViewModelFactory: FireStoreViewModelFactory
    private val fireStoreViewModel by viewModels<FireStoreViewModel> { fireStoreViewModelFactory }

    @Inject
    lateinit var authViewModelFactory: AuthViewModelFactory
    private val authViewModel by viewModels<AuthViewModel> { authViewModelFactory }

    private val families: MutableLiveData<MutableList<CommonForRequest>> = MutableLiveData()
    private var currentUser: MutableLiveData<User> = MutableLiveData()


    @Inject
    lateinit var storageViewModelFactory: StorageViewModelFactory
    private val storageViewModel by viewModels<StorageViewModel> { storageViewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        setUpAdapter()
        makeFamilyRequestAdapter.getStorageReference(storageViewModel.getStorageReference())
        switchUserAndFamily.value = true

        binding.familyRequestsBtn.apply {
            setOnClickListener {
                switchUserAndFamily.value = true
                it.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.orange
                    )
                )
                binding.userRequestBtn.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
            }
        }

        binding.profileSetUpBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.userRequestBtn.apply {
            setOnClickListener {
                switchUserAndFamily.value = false
                it.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.orange
                    )
                )
                binding.familyRequestsBtn.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
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
                    .into(binding.profileSetUpImgView)
            }
            if (!user.pendingFamilyJoinReq.isNullOrEmpty()) {
                loadFamilies(user.pendingFamilyJoinReq)
            }
            binding.profileSetUpName.setText(user.name.toString())
        }

        binding.editProfileUserPicBtn.setOnClickListener {
            val cameraIntent = Intent(requireContext(), CameraActivity::class.java)
            cameraActivityLauncher.launch(cameraIntent)
        }
        binding.profileSetUpEdit.setOnClickListener {
            val cameraIntent = Intent(requireContext(), CameraActivity::class.java)
            cameraActivityLauncher.launch(cameraIntent)
        }

        switchUserAndFamily.observe(viewLifecycleOwner) { change ->
            if (change) {
                loadDataChanges()
            } else {
                makeFamilyRequestAdapter.asyncDiffer.submitList(mutableListOf())
                makeFamilyRequestAdapter.notifyDataSetChanged()
            }
        }

        makeFamilyRequestAdapter.setOnAccept { commonForReq ->
            fireStoreViewModel.updateFamilyMemberList(commonForReq.uid, currentUserUID)
                .observe(viewLifecycleOwner) { resources ->
                    when (resources) {
                        is Resource.Success -> {
                            fireStoreViewModel.updateFamilyJoinReq(
                                currentUserUID,
                                commonForReq.uid,
                                false
                            ).observe(viewLifecycleOwner) { resource ->
                                when (resource) {
                                    is Resource.Success -> {
                                        fireStoreViewModel.addFamilyIntoUserDocument(familyId = commonForReq.uid)
                                            .observe(viewLifecycleOwner) { resource1 ->
                                                when (resource1) {
                                                    is Resource.Success -> {
                                                        families.value?.remove(commonForReq)
                                                        loadDataChanges()
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
        binding.saveProfileBtn.setOnClickListener {
            val finalUserName = binding.profileSetUpName.text.toString()
            if (finalUserName.isNotBlank()) {
                authViewModel.updateProfile(displayName = finalUserName)
                    .observe(viewLifecycleOwner) { resources ->
                        when (resources) {
                            is Resource.Success -> {
                                if (familyPicUri.isBlank()) {
                                    updateProfileInFireStore(finalUserName, null)
                                }
                                else{
                                    uploadProfilePic(finalUserName,familyPicUri,"userProfilePic")
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

        return binding.root
    }

    private fun loadFamilies(familyIdList: MutableList<String>) {
        val commonList: MutableList<CommonForRequest> = mutableListOf()
        for (familyId in familyIdList) {
            fireStoreViewModel.getFamilyById(familyId).observe(viewLifecycleOwner) { resources ->
                when (resources) {
                    is Resource.Success -> {
                        commonList.add(
                            CommonForRequest(
                                profilePic = resources.data?.familyProfilePicId ?: "",
                                uid = resources.data?.familyID ?: "",
                                name = resources.data?.familyName ?: ""
                            )
                        )
                        families.value = commonList

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


    override fun onResume() {
        super.onResume()
    }

    override fun onStart() {
        super.onStart()

    }

    private fun setUpAdapter() {
        makeFamilyRequestAdapter = MakeFamilyRequestAdapter()
        binding.recyclerPendingRequests.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPendingRequests.adapter = makeFamilyRequestAdapter

    }

    private var cameraActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult? ->
        if (result?.resultCode == AppCompatActivity.RESULT_OK) {
            val data: Intent? = result.data
            if (data != null && data.data != null) {
                familyPicUri = data.data.toString()
                binding.profileSetUpImgView.setImageURI(data.data)
            }
        }
    }

    private fun loadDataChanges() {
        families.observe(viewLifecycleOwner) { list ->
            makeFamilyRequestAdapter.asyncDiffer.submitList(list)
            makeFamilyRequestAdapter.notifyDataSetChanged()
        }
    }

    private fun updateProfileInFireStore(finalUSerName: String, photoUri: String?) {
        fireStoreViewModel.updateProfile(finalUSerName, photoUri)
            .observe(viewLifecycleOwner) { resources ->
                when (resources) {
                    is Resource.Success -> {
                        Snackbar.make(
                            binding.root,
                            "Success",
                            Snackbar.LENGTH_LONG
                        ).show()
                        requireActivity().onBackPressedDispatcher.onBackPressed()

                    }

                    is Resource.Error -> {
                        Snackbar.make(
                            binding.root,
                            resources.message.toString(),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    is Resource.Loading -> {
                        Snackbar.make(
                            binding.root,
                            resources.message.toString(),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                }

            }
    }

    private fun uploadProfilePic(finalUSerName: String,photoUri: String, pathName: String) {
        storageViewModel.saveImageInStorage(imageUri = Uri.parse(photoUri), pathName = pathName)
            .observe(viewLifecycleOwner) { resources ->
                when (resources) {
                    is Resource.Success -> {
                        updateProfileInFireStore(finalUSerName,resources.data!!.toString())
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