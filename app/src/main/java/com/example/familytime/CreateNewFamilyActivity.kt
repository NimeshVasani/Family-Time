package com.example.familytime

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.familytime.adapters.MakeMemberRequestAdapter
import com.example.familytime.databinding.ActivityCreateNewFamilyBinding
import com.example.familytime.models.Family
import com.example.familytime.other.Resource
import com.example.familytime.viewmodels.firestore.FireStoreViewModel
import com.example.familytime.viewmodels.firestore.FireStoreViewModelFactory
import com.example.familytime.viewmodels.storage.StorageViewModel
import com.example.familytime.viewmodels.storage.StorageViewModelFactory
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class CreateNewFamilyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateNewFamilyBinding
    private lateinit var makeMemberRequestAdapter: MakeMemberRequestAdapter
    private var familyPicUri: String = ""
    private val membersList: MutableList<String> = mutableListOf()

    @Inject
    lateinit var fireStoreViewModelFactory: FireStoreViewModelFactory
    private val fireStoreViewModel by viewModels<FireStoreViewModel> { fireStoreViewModelFactory }


    @Inject
    lateinit var storageViewModelFactory: StorageViewModelFactory
    private val storageViewModel by viewModels<StorageViewModel> { storageViewModelFactory }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateNewFamilyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpAdapter()
        fireStoreViewModel.getCurrentUser().observe(this) { resources ->
            when (resources) {
                is Resource.Success -> {
                    membersList.add(resources.data!!.uId!!)
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


        binding.createFamilyBackBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.createFamilySearchBtn.setOnClickListener {
            val searchUserName = binding.createFamilyMemberSearch.text.toString()
            fireStoreViewModel.searchUserByName(userName = searchUserName)
                .observe(this) { resource ->
                    Snackbar.make(binding.root, "Clicked", Snackbar.LENGTH_LONG).show()

                    when (resource) {
                        is Resource.Success -> {
                            makeMemberRequestAdapter.asyncDiffer.submitList(resource.data)
                            makeMemberRequestAdapter.notifyDataSetChanged()
                            binding.createFamilyMemberSearch.clearFocus()
                            binding.createFamilyScrollView.post {
                                binding.createFamilyScrollView.apply {
                                    fullScroll(ScrollView.FOCUS_DOWN)
                                }
                            }
                        }

                        is Resource.Error -> {
                            Snackbar.make(
                                binding.root,
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

        binding.createFamilyImg.setOnClickListener {
            val cameraIntent = Intent(this, CameraActivity::class.java)
            cameraActivityLauncher.launch(cameraIntent)
        }

        binding.profileSetUpName.apply {
            setOnFocusChangeListener { _, hasFocus ->
                binding.editCancelBtn.visibility = if (hasFocus) View.VISIBLE else View.GONE
                binding.editDoneBtn.visibility = if (hasFocus) View.VISIBLE else View.GONE
                binding.createFamilyBackBtn.visibility =
                    if (hasFocus) View.GONE else View.VISIBLE
                binding.finishCreateFamilyBtn.visibility =
                    if (hasFocus) View.GONE else View.VISIBLE
            }
        }

        binding.createFamilyDescription.apply {
            setOnFocusChangeListener { _, hasFocus ->
                binding.editCancelBtn.visibility = if (hasFocus) View.VISIBLE else View.GONE
                binding.editDoneBtn.visibility = if (hasFocus) View.VISIBLE else View.GONE
                binding.createFamilyBackBtn.visibility =
                    if (hasFocus) View.GONE else View.VISIBLE
                binding.finishCreateFamilyBtn.visibility =
                    if (hasFocus) View.GONE else View.VISIBLE
            }
        }

        binding.editDoneBtn.setOnClickListener {
            hideKeyboard()
            binding.profileSetUpName.clearFocus()
            binding.createFamilyDescription.clearFocus()
        }

        binding.editCancelBtn.setOnClickListener {
            hideKeyboard()
            binding.profileSetUpName.clearFocus()
            binding.createFamilyDescription.clearFocus()
        }

        binding.finishCreateFamilyBtn.setOnClickListener {
            val finalName = binding.profileSetUpName.text.toString()
            if (finalName.isNotBlank()) {
                if (familyPicUri.isNotBlank()) {
                    saveFamily()
                }
            } else {
                Snackbar.make(binding.root, "Family name is mandatory", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun setUpAdapter() {
        makeMemberRequestAdapter = MakeMemberRequestAdapter()
        binding.recyclerCreateFamilyMembers.layoutManager = LinearLayoutManager(this)
        binding.recyclerCreateFamilyMembers.adapter = makeMemberRequestAdapter
    }


    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.createFamilyMemberSearch.windowToken, 0)
    }


    private fun saveFamily() {
        val pathName = "familyProfilePic"
        storageViewModel.saveImageInStorage(imageUri = Uri.parse(familyPicUri), pathName = pathName)
            .observe(this) { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val familyID = UUID.randomUUID().toString()
                        val familyName = binding.profileSetUpName.text.toString()
                        val familyDescription = binding.createFamilyDescription.text.toString()
                        saveInFireStore(
                            Family(
                                familyID = familyID,
                                members = membersList,
                                familyName = familyName,
                                familyDescription = familyDescription,
                                familyProfilePicId = resource.data.toString()
                            )

                        )
                    }

                    is Resource.Error -> {
                        Snackbar.make(binding.root, "Loading", Snackbar.LENGTH_LONG).show()

                    }

                    is Resource.Loading -> {
                        Snackbar.make(binding.root, "Loading", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
    }

    private fun saveInFireStore(family: Family) {
        fireStoreViewModel.saveFamilies(family).observe(this) { resource ->
            when (resource) {
                is Resource.Success -> {
                    updateUserFamilyList(familyId = family.familyID)
                }

                is Resource.Error -> {
                    Snackbar.make(binding.root, resource.message.toString(), Snackbar.LENGTH_LONG)
                        .show()

                }

                is Resource.Loading -> {
                    Snackbar.make(binding.root, "Loading", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun updateUserFamilyList(familyId: String) {
        fireStoreViewModel.addFamilyIntoUserDocument(familyId = familyId)
            .observe(this) { resources ->
                when (resources) {
                    is Resource.Success -> {
                        startActivity(
                            Intent(this, FamilyActivity::class.java).putExtra(
                                "familyId",
                                familyId
                            )
                        )
                        finish()
                    }

                    is Resource.Error -> {
                        Snackbar.make(
                            binding.root,
                            resources.message.toString(),
                            Snackbar.LENGTH_LONG
                        ).show()

                    }

                    is Resource.Loading -> {
                        Snackbar.make(binding.root, "Loading", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
    }

    private var cameraActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult? ->
        if (result?.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            if (data != null && data.data != null) {
                familyPicUri = data.data.toString()
                binding.createFamilyImg.setImageURI(data.data)
            }
        }
    }
}