package com.example.familytime

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.doOnAttach
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.familytime.adapters.UserChattingAdapter
import com.example.familytime.databinding.ActivityUserChattingBinding
import com.example.familytime.databinding.AttachFileMenuBinding
import com.example.familytime.models.CommonForRequest
import com.example.familytime.models.User
import com.example.familytime.models.UserChatting
import com.example.familytime.other.Resource
import com.example.familytime.other.Utils.currentUserUID
import com.example.familytime.other.Utils.getCurrentDateLiveData
import com.example.familytime.other.Utils.getCurrentTimeLiveData
import com.example.familytime.viewmodels.auth.AuthViewModel
import com.example.familytime.viewmodels.auth.AuthViewModelFactory
import com.example.familytime.viewmodels.chats.ChatsViewModel
import com.example.familytime.viewmodels.chats.ChatsViewModelFactory
import com.example.familytime.viewmodels.firestore.FireStoreViewModel
import com.example.familytime.viewmodels.firestore.FireStoreViewModelFactory
import com.example.familytime.viewmodels.storage.StorageViewModel
import com.example.familytime.viewmodels.storage.StorageViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.database.ServerValue
import dagger.hilt.android.AndroidEntryPoint
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class UserChattingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserChattingBinding
    private lateinit var receiver: CommonForRequest
    private lateinit var menuBinding: AttachFileMenuBinding
    private lateinit var userChattingAdapter: UserChattingAdapter
    private lateinit var lastChats: MutableList<UserChatting>
    private var currentUser: MutableLiveData<User> = MutableLiveData()

    private var currentUserPic = ""
    private var currentTime = ""
    private var currentDate = ""


    @Inject
    lateinit var chatsViewModelFactory: ChatsViewModelFactory
    private val chatsViewModel by viewModels<ChatsViewModel> { chatsViewModelFactory }

    @Inject
    lateinit var storageViewModelFactory: StorageViewModelFactory
    private val storageViewModel by viewModels<StorageViewModel> { storageViewModelFactory }


    @Inject
    lateinit var fireStoreViewModelFactory: FireStoreViewModelFactory
    private val fireStoreViewModel by viewModels<FireStoreViewModel> { fireStoreViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserChattingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpAdapter()
        userChattingAdapter.getStorageReference(storageViewModel.getStorageReference())
        receiver = intent.getSerializable("user", CommonForRequest::class.java)
        val menuInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        menuBinding = AttachFileMenuBinding.inflate(menuInflater)
        binding.tvUserName.text = receiver.name
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
                }

            }
        }
        currentUser.observe(this) { user ->
            if (!user.profilePic.isNullOrBlank()) {
                currentUserPic = user.profilePic.toString()
            }
        }
        Glide.with(binding.root.context)
            .load(storageViewModel.getStorageReference().getReferenceFromUrl(receiver.profilePic))
            .into(binding.imgUserChatting)
        binding.userChattingBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.edtSendUserChat.doOnTextChanged { text, _, _, _ ->

            binding.sendUserChatBtn.visibility =
                if (text.isNullOrBlank()) View.GONE else View.VISIBLE
            binding.sendDocsUserChat.visibility =
                if (text.isNullOrBlank()) View.VISIBLE else View.GONE
            binding.cameraUserChat.visibility =
                if (text.isNullOrBlank()) View.VISIBLE else View.GONE
            binding.recordUserChat.visibility =
                if (text.isNullOrBlank()) View.VISIBLE else View.GONE
        }
        binding.sendDocsUserChat.setOnClickListener {
            val popupWindow = PopupWindow(
                menuBinding.root,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )
            popupWindow.animationStyle = R.style.popup_window_animation
            popupWindow.showAtLocation(binding.root, Gravity.START + Gravity.BOTTOM, 0, 0)
        }
        binding.cameraUserChat.setOnClickListener {
            val cameraIntent = Intent(this, CameraActivity::class.java)
            cameraActivityLauncher.launch(cameraIntent)
        }
        getCurrentTimeLiveData().observe(this) {
            currentTime = it
        }
        getCurrentDateLiveData().observe(this) {
            currentDate = it
        }
        chatsViewModel.loadLast20Chats(receiver.uid).observe(this) { data ->
            lastChats = data
            userChattingAdapter.asyncDiffer.submitList(lastChats)
            userChattingAdapter.notifyDataSetChanged()
            binding.recyclerUserChatting.postDelayed({
                val lastItemPosition = userChattingAdapter.asyncDiffer.currentList.size - 1
                binding.recyclerUserChatting.onScrolled(0, 10)
                binding.recyclerUserChatting.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
                    if (bottom < oldBottom) {
                        binding.recyclerUserChatting.scrollBy(0, oldBottom - bottom)
                        binding.recyclerUserChatting.scrollToPosition(lastItemPosition)

                    }
                }
                binding.recyclerUserChatting.scrollToPosition(lastItemPosition)

            }, 200)

        }
        binding.sendUserChatBtn.setOnClickListener {
            val dateTimeString = "$currentDate $currentTime"
            val dateTimeFormat = SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.ENGLISH)
            val combinedTimestamp = dateTimeFormat.parse(dateTimeString)
            if (!binding.edtSendUserChat.text.isNullOrBlank()) {
                val userChatting = UserChatting(
                    sender = currentUserUID,
                    receiver = receiver.uid,
                    senderProfilePic = currentUserPic,
                    time = combinedTimestamp?.toString(),
                    isRead = false,
                    type = "String",
                    message = binding.edtSendUserChat.text.toString().trim(),
                )
                saveChatInRealtimeDatabase(
                    userChatting = userChatting,
                    receiver = receiver.uid
                )

                binding.recyclerUserChatting.postDelayed({
                    val lastItemPosition = userChattingAdapter.itemCount - 1
                    if (lastItemPosition >= 0) {
                        binding.recyclerUserChatting.scrollToPosition(lastItemPosition)
                    }

                }, 200)
                binding.edtSendUserChat.setText("")
            }
        }


    }

    private fun setUpAdapter() {
        userChattingAdapter = UserChattingAdapter()
        binding.recyclerUserChatting.adapter = userChattingAdapter
        binding.recyclerUserChatting.setHasFixedSize(true)
        binding.recyclerUserChatting.setItemViewCacheSize(1000)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true

        binding.recyclerUserChatting.layoutManager = layoutManager


    }

    private fun <T : Serializable?> Intent.getSerializable(key: String, m_class: Class<T>): T {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            this.getSerializableExtra(key, m_class)!!
        else
            this.getSerializableExtra(key) as T
    }

    private var cameraActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult? ->
        if (result?.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            if (data != null && data.data != null) {
                val imageUri = data.data.toString()

                saveImageInFirebase(
                    imageUri = Uri.parse(imageUri),
                )
            }
        }
    }

    private fun saveImageInFirebase(imageUri: Uri) {
        val pathName = "ChatsImage"
        val dateTimeString = "$currentDate $currentTime"
        val dateTimeFormat = SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.ENGLISH)
        val combinedTimestamp = dateTimeFormat.parse(dateTimeString)

        storageViewModel.saveImageInStorage(imageUri, pathName).observe(this) { resource ->
            when (resource) {
                is Resource.Success -> {
                    saveChatInRealtimeDatabase(
                        userChatting = UserChatting(
                            sender = currentUserUID,
                            receiver = receiver.uid,
                            senderProfilePic = currentUserPic,
                            time = combinedTimestamp?.toString(),
                            type = "Image",
                            message = "",
                            imageId = resource.data!!.toString(),
                        ),
                        receiver = receiver.uid
                    )
                }

                is Resource.Error -> {
                    Toast.makeText(this, resource.message.toString(), Toast.LENGTH_LONG).show()
                }

                is Resource.Loading -> {

                }

                else -> {}
            }
        }
    }


    private fun saveChatInRealtimeDatabase(
        userChatting: UserChatting,
        receiver: String
    ) {
        chatsViewModel.saveChatInRealTimeDatabase(userChatting, receiver)
            .observe(this) { resources ->
                when (resources) {
                    is Resource.Success -> {
                        userChattingAdapter.asyncDiffer.submitList(lastChats)
                        userChattingAdapter.notifyDataSetChanged()
                        binding.recyclerUserChatting.postDelayed({
                            val lastItemPosition =
                                userChattingAdapter.asyncDiffer.currentList.size - 1
                            binding.recyclerUserChatting.onScrolled(0, 10)
                            binding.recyclerUserChatting.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
                                if (bottom < oldBottom) {
                                    binding.recyclerUserChatting.scrollBy(0, oldBottom - bottom)
                                    binding.recyclerUserChatting.scrollToPosition(lastItemPosition)

                                }
                            }
                            binding.recyclerUserChatting.scrollToPosition(lastItemPosition)

                        }, 200)
                    }

                    is Resource.Error -> {

                    }

                    is Resource.Loading -> {

                    }

                    else -> {

                    }
                }
            }
    }

}