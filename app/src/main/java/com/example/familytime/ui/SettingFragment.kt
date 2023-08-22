package com.example.familytime.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.familytime.LoginActivity
import com.example.familytime.R
import com.example.familytime.adapters.MakeFamilyRequestAdapter
import com.example.familytime.adapters.ManageFamiliesAdapter
import com.example.familytime.databinding.FragmentSettingBinding
import com.example.familytime.models.CommonForRequest
import com.example.familytime.models.User
import com.example.familytime.other.Resource
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
class SettingFragment : Fragment() {

    private lateinit var binding: FragmentSettingBinding
    private lateinit var manageFamiliesAdapter: ManageFamiliesAdapter

    @Inject
    lateinit var fireStoreViewModelFactory: FireStoreViewModelFactory
    private val fireStoreViewModel by viewModels<FireStoreViewModel> { fireStoreViewModelFactory }

    @Inject
    lateinit var authViewModelFactory: AuthViewModelFactory
    private val authViewModel by viewModels<AuthViewModel> { authViewModelFactory }

    @Inject
    lateinit var storageViewModelFactory: StorageViewModelFactory
    private val storageViewModel by viewModels<StorageViewModel> { storageViewModelFactory }

    private val families: MutableLiveData<MutableList<CommonForRequest>> = MutableLiveData()
    private var currentUser: MutableLiveData<User> = MutableLiveData()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        setUpAdapter()
        manageFamiliesAdapter.getStorageReference(storageViewModel.getStorageReference())
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
                    .into(binding.settingUserProfileImg)
            } else {
                binding.settingUserProfileImg.setImageResource(R.drawable.img_user_logo)
            }
            if (!user.families.isNullOrEmpty()) {
                loadFamilies(user.families)
            }
            binding.settingFragmentUserName.text = user.name.toString()
            binding.settingFragmentUserEmail.text = user.email.toString()
        }

        binding.settingLogOutBtn.setOnClickListener {
            authViewModel.logout()
            if (!authViewModel.checkLoginSession()) {
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }
        }
        binding.settingFragmentUserEdit.setOnClickListener {
            findNavController().navigate(R.id.action_settingFragment_to_profile_set_up_graph)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        families.observe(viewLifecycleOwner) { list ->
            manageFamiliesAdapter.asyncDiffer.submitList(list)
            manageFamiliesAdapter.notifyDataSetChanged()
        }
    }

    private fun setUpAdapter() {
        manageFamiliesAdapter = ManageFamiliesAdapter()
        binding.settingManageFamiliesRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.settingManageFamiliesRecycler.adapter = manageFamiliesAdapter

    }

}