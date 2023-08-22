package com.example.familytime.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.familytime.R
import com.example.familytime.databinding.ItemCreateNewRequestBinding
import com.example.familytime.models.User
import com.google.firebase.storage.FirebaseStorage

class MakeMemberRequestAdapter :
    RecyclerView.Adapter<MakeMemberRequestAdapter.MakeMemberRequestHolder>() {

    private lateinit var binding: ItemCreateNewRequestBinding
    private lateinit var firebaseStorage: FirebaseStorage


    private var differCallback = object : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }

    }
    var asyncDiffer: AsyncListDiffer<User> = AsyncListDiffer(this, differCallback)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MakeMemberRequestHolder {
        binding = ItemCreateNewRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MakeMemberRequestHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return asyncDiffer.currentList.size
    }

    override fun onBindViewHolder(holder: MakeMemberRequestHolder, position: Int) {
        val currentUser = asyncDiffer.currentList[position]
        holder.apply {
            if (!currentUser.profilePic.isNullOrBlank()) {
                Glide.with(binding.root.context)
                    .load(firebaseStorage.getReferenceFromUrl(currentUser.profilePic!!))
                    .into(familyProfilePic)
            } else {
                familyProfilePic.setImageResource(R.drawable.img_user_logo)
            }
            familyName.text = currentUser.name
        }
        holder.addToFamilyBtn.setOnClickListener {
            onItemClickListener?.let { it(currentUser.uId!!) }

        }
    }

    inner class MakeMemberRequestHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val familyProfilePic = binding.itemSendReqImg
        val familyName = binding.itemSendReqName
        val addToFamilyBtn = binding.sendRequestBtn
    }

    fun getStorageReference(firebaseStorage: FirebaseStorage) {
        this.firebaseStorage = firebaseStorage
    }

    private var onItemClickListener: ((String) -> Unit)? = null
    fun setOnItemClickListener(listener: (String) -> Unit) {
        onItemClickListener = listener
    }

    fun changeReqStatus() {
        binding.sendRequestBtn.setBackgroundColor(
            ContextCompat.getColor(
                binding.root.context,
                R.color.custom_grey
            )
        )
        binding.sendRequestBtn.text = "sent"
    }
}