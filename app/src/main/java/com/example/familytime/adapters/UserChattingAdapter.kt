package com.example.familytime.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.example.familytime.R
import com.example.familytime.databinding.ItemUserChatReceiveBinding
import com.example.familytime.databinding.ItemUserChatSendBinding
import com.example.familytime.models.UserChatting
import com.example.familytime.other.Utils.currentUserUID
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Locale


class UserChattingAdapter :
    RecyclerView.Adapter<UserChattingAdapter.UserChattingViewHolder>() {

    private lateinit var binding1: ItemUserChatSendBinding
    private lateinit var binding2: ItemUserChatReceiveBinding
    private lateinit var firebaseStorage: FirebaseStorage

    private val differCallback = object : DiffUtil.ItemCallback<UserChatting>() {

        override fun areItemsTheSame(oldItem: UserChatting, newItem: UserChatting): Boolean {
            return oldItem.message == newItem.message
        }

        override fun areContentsTheSame(oldItem: UserChatting, newItem: UserChatting): Boolean {
            return oldItem == newItem
        }
    }

    var asyncDiffer: AsyncListDiffer<UserChatting> = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserChattingViewHolder {
        binding1 =
            ItemUserChatSendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding2 =
            ItemUserChatReceiveBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return if (viewType == 1) {
            UserChattingViewHolder(binding1)

        } else {
            UserChattingViewHolder(binding2)
        }
    }

    override fun getItemCount(): Int {
        return asyncDiffer.currentList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (asyncDiffer.currentList[position].sender) {
            currentUserUID -> 1
            else ->
                2
        }
    }

    override fun onBindViewHolder(holder: UserChattingViewHolder, position: Int) {

        val userChatting = asyncDiffer.currentList[position]
        val dateTimeFormat = SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
        val dateTime = dateTimeFormat.parse(userChatting.time!!)

// Create a new SimpleDateFormat for extracting and formatting time
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        val timeString = dateTime?.let { timeFormat.format(it) }


        if (getItemViewType(position) == 1) {
            if (userChatting.imageId.isNullOrEmpty()) {
                holder.apply {
                    sendMessage.visibility = View.VISIBLE
                    sendMsgImg.visibility = View.GONE
                    sendMessage.text = userChatting.message
                    sendMsgTime.text = timeString
                }
            } else {
                holder.apply {
                    sendMessage.visibility = View.GONE
                    sendMsgImg.visibility = View.VISIBLE
                    sendMsgTime.text = timeString
                    Glide.with(sendMsgImg)
                        .load(firebaseStorage.getReferenceFromUrl(userChatting.imageId))
                        .into(sendMsgImg)
                    sendChatConstraint.background = null
                }
            }

        } else {
            if (userChatting.imageId.isNullOrEmpty()) {
                holder.apply {
                    receiveMessage.visibility = View.VISIBLE
                    receiveMsgTime.visibility = View.GONE
                    receiveMessage.text = userChatting.message
                    receiveMsgTime.text = timeString
                    if (!userChatting.senderProfilePic.isNullOrBlank()){
                        Glide.with(receiverUserProfilePic)
                            .load(firebaseStorage.getReferenceFromUrl(userChatting.senderProfilePic))
                            .into(receiverUserProfilePic)
                    }
                }
            } else {
                holder.apply {
                    receiveMessage.visibility = View.GONE
                    receiveMsgImg.visibility = View.VISIBLE
                    receiveMsgTime.text = timeString
                    Glide.with(receiveMsgImg)
                        .load(firebaseStorage.getReferenceFromUrl(userChatting.imageId))
                        .into(receiveMsgImg)
                    receiveChatConstraint.background = null
                    if (!userChatting.senderProfilePic.isNullOrBlank()){
                        Glide.with(receiverUserProfilePic)
                            .load(firebaseStorage.getReferenceFromUrl(userChatting.senderProfilePic))
                            .into(receiverUserProfilePic)
                    }

                }
            }
        }

    }

    inner class UserChattingViewHolder(binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val sendMessage = binding1.sendMessage
        val receiveMessage = binding2.receiveMessage
        val sendMsgTime = binding1.sendMsgTime
        val receiveMsgTime = binding2.receiveMsgTime
        val sendMsgImg = binding1.sendMsgImg
        val receiveMsgImg = binding2.receiveMsgImg
        val sendChatConstraint = binding1.sendChatConstraint
        val receiveChatConstraint = binding2.receiveChatConstraint
        val receiverUserProfilePic = binding2.receiverUserImg
    }

    fun getStorageReference(firebaseStorage: FirebaseStorage) {
        this.firebaseStorage = firebaseStorage
    }
}