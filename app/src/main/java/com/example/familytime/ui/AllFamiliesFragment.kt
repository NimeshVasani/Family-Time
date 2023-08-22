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
import androidx.recyclerview.widget.RecyclerView
import com.example.familytime.CreateNewFamilyActivity
import com.example.familytime.FamilyActivity
import com.example.familytime.adapters.AllFamiliesAdapter
import com.example.familytime.databinding.FragmentAllFamiliesBinding
import com.example.familytime.models.Family
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
class AllFamiliesFragment : Fragment() {

    private lateinit var binding: FragmentAllFamiliesBinding
    private lateinit var allFamiliesAdapter: AllFamiliesAdapter


    private var currentUser: MutableLiveData<User> = MutableLiveData()

    @Inject
    lateinit var fireStoreViewModelFactory: FireStoreViewModelFactory
    private val fireStoreViewModel by viewModels<FireStoreViewModel> { fireStoreViewModelFactory }


    @Inject
    lateinit var storageViewModelFactory: StorageViewModelFactory
    private val storageViewModel by viewModels<StorageViewModel> { storageViewModelFactory }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllFamiliesBinding.inflate(inflater, container, false)
        setUpAdapter()
        allFamiliesAdapter.getStorageReference(storageViewModel.getStorageReference())

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


        binding.createNewFamilyBtn.setOnClickListener {
            startActivity(Intent(requireContext(), CreateNewFamilyActivity::class.java))
        }

        allFamiliesAdapter.setOnItemClickListener { familyId ->
            startActivity(
                Intent(requireContext(), FamilyActivity::class.java).putExtra(
                    "familyId",
                    familyId
                )
            )
        }
        return binding.root
    }

    private fun setUpAdapter() {
        allFamiliesAdapter = AllFamiliesAdapter()
        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerAllFamilies.layoutManager = layoutManager
        binding.recyclerAllFamilies.adapter = allFamiliesAdapter
        binding.recyclerAllFamilies.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                // Check if the user is at the last item
                if (lastVisibleItemPosition == totalItemCount - 1) {
                    // Allow the user to scroll a bit more by adjusting the RecyclerView scroll position
                    recyclerView.setPadding(40, 40, 40, 300) // Adjust the value as needed
                }
            }
        })
    }

    private fun loadFamilies(familyIdList: MutableList<String>) {
        val familyList: MutableList<Family> = mutableListOf()

        for (familyId in familyIdList) {
            fireStoreViewModel.getFamilyById(familyId).observe(viewLifecycleOwner) { resources ->
                when (resources) {
                    is Resource.Success -> {
                        familyList.add(resources.data!!)
                        familyList.sort()
                        allFamiliesAdapter.asyncDiffer.submitList(familyList)
                        allFamiliesAdapter.notifyDataSetChanged()
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