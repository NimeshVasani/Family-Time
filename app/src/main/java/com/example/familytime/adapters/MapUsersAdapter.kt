package com.example.familytime.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.familytime.R
import com.example.familytime.databinding.ItemFamilyMemberBinding
import com.example.familytime.databinding.ItemMapUserListBinding
import com.example.familytime.models.User
import com.example.familytime.other.Utils.currentUserUID
import com.google.firebase.storage.FirebaseStorage

class MapUsersAdapter : RecyclerView.Adapter<MapUsersAdapter.MapUsersHolder>() {

    private lateinit var binding: ItemMapUserListBinding
    private lateinit var firebaseStorage: FirebaseStorage


    private val differCallback = object : DiffUtil.ItemCallback<User>() {

        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.uId == newItem.uId
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }

    var asyncDiffer: AsyncListDiffer<User> = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapUsersHolder {
        binding =
            ItemMapUserListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MapUsersHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return asyncDiffer.currentList.size
    }

    override fun onBindViewHolder(holder: MapUsersHolder, position: Int) {
        val currentMember = asyncDiffer.currentList[position]

        holder.apply {
            if (!currentMember.profilePic.isNullOrBlank()) {
                Glide.with(binding.root.context)
                    .load(firebaseStorage.getReferenceFromUrl(currentMember.profilePic!!))
                    .into(memberImg)
            } else {
                memberImg.setImageResource(R.drawable.img_user_logo)
            }
            if (currentMember.uId == currentUserUID) {
                memberName.text = "You"
            } else
                memberName.text = currentMember.name
        }
    }

    inner class MapUsersHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val memberImg = binding.itemMapUserProfilePic
        val memberName = binding.itemMapUserName
    }

    fun getStorageReference(firebaseStorage: FirebaseStorage) {
        this.firebaseStorage = firebaseStorage
    }

}