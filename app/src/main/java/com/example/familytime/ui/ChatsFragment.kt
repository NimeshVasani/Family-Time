package com.example.familytime.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.familytime.UserChattingActivity
import com.example.familytime.adapters.ChatMainAdapter
import com.example.familytime.databinding.FragmentChatsBinding
import com.example.familytime.models.CommonForRequest
import com.example.familytime.models.User
import com.example.familytime.other.Resource
import com.example.familytime.viewmodels.firestore.FireStoreViewModel
import com.example.familytime.viewmodels.firestore.FireStoreViewModelFactory
import com.example.familytime.viewmodels.storage.StorageViewModel
import com.example.familytime.viewmodels.storage.StorageViewModelFactory
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChatsFragment : Fragment() {

    private lateinit var chatMainAdapter: ChatMainAdapter
    private lateinit var binding: FragmentChatsBinding
    private var currentUser: MutableLiveData<User> = MutableLiveData()
    private val families: MutableLiveData<MutableList<CommonForRequest>> = MutableLiveData()


    @Inject
    lateinit var fireStoreViewModelFactory: FireStoreViewModelFactory
    private val fireStoreViewModel by viewModels<FireStoreViewModel> { fireStoreViewModelFactory }

    @Inject
    lateinit var firebaseStorageViewModelFactory: StorageViewModelFactory
    private val storageViewModel by viewModels<StorageViewModel> { firebaseStorageViewModelFactory }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        setUpAdapter()
        chatMainAdapter.getStorageReference(storageViewModel.getStorageReference())

        getCurrentUser()

        binding.chatsBackBtn.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        currentUser.observe(viewLifecycleOwner) { user ->
            if (!user.families.isNullOrEmpty()) {
                loadFamilies(user.families)
            }
            binding.fragmentChatUserName.text = user.name
        }
        families.observe(viewLifecycleOwner) { list ->
            list.sortBy { it }
            chatMainAdapter.asyncDiffer.submitList(list)
            chatMainAdapter.notifyDataSetChanged()
        }

        chatMainAdapter.setOnItemClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    UserChattingActivity::class.java
                ).putExtra("user", it)
            )
        }
        return binding.root
    }

    private fun setUpAdapter() {
        chatMainAdapter = ChatMainAdapter()
        binding.chatRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.chatRecycler.adapter = chatMainAdapter

    }

    private fun getCurrentUser() {
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
}