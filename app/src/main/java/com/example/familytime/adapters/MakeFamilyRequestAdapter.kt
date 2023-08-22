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
import com.example.familytime.models.CommonForRequest
import com.example.familytime.models.Family
import com.google.firebase.storage.FirebaseStorage

class MakeFamilyRequestAdapter :
    RecyclerView.Adapter<MakeFamilyRequestAdapter.MakeFamilyRequestHolder>() {

    private lateinit var binding: ItemCreateNewRequestBinding
    private lateinit var firebaseStorage: FirebaseStorage

    private var differCallback = object : DiffUtil.ItemCallback<CommonForRequest>() {
        override fun areItemsTheSame(
            oldItem: CommonForRequest,
            newItem: CommonForRequest
        ): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(
            oldItem: CommonForRequest,
            newItem: CommonForRequest
        ): Boolean {
            return oldItem == newItem
        }

    }
    var asyncDiffer: AsyncListDiffer<CommonForRequest> = AsyncListDiffer(this, differCallback)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MakeFamilyRequestHolder {
        binding = ItemCreateNewRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MakeFamilyRequestHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return asyncDiffer.currentList.size
    }

    override fun onBindViewHolder(holder: MakeFamilyRequestHolder, position: Int) {
        val currentReq = asyncDiffer.currentList[position]
        holder.acceptFamilyJoinBtn.apply {
            this.setBackgroundColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.blue
                )
            )
            this.text = "Accept"
            setOnClickListener {
                onItemClickListener?.let { it(currentReq) }

            }

        }
        holder.apply {
            if (currentReq.profilePic.isNotBlank()) {
                Glide.with(binding.root.context)
                    .load(firebaseStorage.getReferenceFromUrl(currentReq.profilePic))
                    .into(familyProfilePic)
            } else {
                familyProfilePic.setImageResource(R.drawable.img_user_logo)
            }
            familyName.text = currentReq.name
        }

    }

    inner class MakeFamilyRequestHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val familyProfilePic = binding.itemSendReqImg
        val familyName = binding.itemSendReqName
        val acceptFamilyJoinBtn = binding.sendRequestBtn
    }

    fun getStorageReference(firebaseStorage: FirebaseStorage) {
        this.firebaseStorage = firebaseStorage
    }

    private var onItemClickListener: ((CommonForRequest) -> Unit)? = null
    fun setOnAccept(listener: (CommonForRequest) -> Unit) {
        onItemClickListener = listener
    }

    fun changeReqStatus() {
        binding.sendRequestBtn.setBackgroundColor(
            ContextCompat.getColor(
                binding.root.context,
                R.color.blue
            )
        )
        binding.sendRequestBtn.text = "sent"
    }

}