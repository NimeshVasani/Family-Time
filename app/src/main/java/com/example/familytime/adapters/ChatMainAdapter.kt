package com.example.familytime.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.familytime.R
import com.example.familytime.databinding.ItemChatMainBinding
import com.example.familytime.models.CommonForRequest
import com.example.familytime.models.User
import com.google.firebase.storage.FirebaseStorage

class ChatMainAdapter : RecyclerView.Adapter<ChatMainAdapter.ChatMainHolder>() {
    private lateinit var binding: ItemChatMainBinding
    private lateinit var firebaseStorage: FirebaseStorage

    private var differCallback = object : DiffUtil.ItemCallback<CommonForRequest>() {
        override fun areItemsTheSame(
            oldItem: CommonForRequest,
            newItem: CommonForRequest
        ): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(
            oldItem: CommonForRequest,
            newItem: CommonForRequest
        ): Boolean {
            return oldItem == newItem
        }

    }
    var asyncDiffer: AsyncListDiffer<CommonForRequest> = AsyncListDiffer(this, differCallback)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMainHolder {
        binding = ItemChatMainBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatMainHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return asyncDiffer.currentList.size

    }

    override fun onBindViewHolder(holder: ChatMainHolder, position: Int) {
        val user = asyncDiffer.currentList[position]

        holder.itemView.setOnClickListener {
            onItemClickListener?.let { it(user) }
        }

        holder.apply {
            userName.text = user.name
            if (user.profilePic.isNotBlank()) {
                Glide.with(binding.root.context)
                    .load(firebaseStorage.getReferenceFromUrl(user.profilePic))
                    .into(userProfilePic)
            } else {
                userProfilePic.setImageResource(R.drawable.img_user_logo)
            }
        }
    }

    private var onItemClickListener: ((CommonForRequest) -> Unit)? = null

    fun setOnItemClickListener(listener: (CommonForRequest) -> Unit) {
        onItemClickListener = listener
    }


    inner class ChatMainHolder(itemView: View) : ViewHolder(itemView) {
        val userName = binding.tvChatMainTitle
        val userLastChat = binding.tvChatMainDesc
        val userProfilePic = binding.imgChatMain
    }

    fun getStorageReference(firebaseStorage: FirebaseStorage) {
        this.firebaseStorage = firebaseStorage
    }
}