package com.example.familytime.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.familytime.R
import com.example.familytime.databinding.ItemCreatePostFamiliesListBinding
import com.example.familytime.models.Family
import com.example.familytime.models.User
import com.google.firebase.storage.FirebaseStorage

class CreatePostFamiliesAdapter :
    RecyclerView.Adapter<CreatePostFamiliesAdapter.CreatePostFamiliesHolder>() {

    private lateinit var binding: ItemCreatePostFamiliesListBinding
    private lateinit var firebaseStorage: FirebaseStorage
    private val sharedToFamilyList: MutableList<String> = mutableListOf()


    private var differCallback = object : DiffUtil.ItemCallback<Family>() {
        override fun areItemsTheSame(oldItem: Family, newItem: Family): Boolean {
            return oldItem.familyName == newItem.familyName
        }

        override fun areContentsTheSame(oldItem: Family, newItem: Family): Boolean {
            return oldItem == newItem
        }

    }
    var asyncDiffer: AsyncListDiffer<Family> = AsyncListDiffer(this, differCallback)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreatePostFamiliesHolder {
        binding = ItemCreatePostFamiliesListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CreatePostFamiliesHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return asyncDiffer.currentList.size
    }

    override fun onBindViewHolder(holder: CreatePostFamiliesHolder, position: Int) {
        val currentFamily = asyncDiffer.currentList[position]
        holder.apply {
            if (!currentFamily.familyProfilePicId.isNullOrBlank()) {
                Glide.with(binding.root.context)
                    .load(firebaseStorage.getReferenceFromUrl(currentFamily.familyProfilePicId))
                    .into(familyProfilePic)
            } else {
                familyProfilePic.setImageResource(R.drawable.img_user_logo)
            }
            familyName.text = currentFamily.familyName
            shareToFamilySwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked){
                    sharedToFamilyList.add(currentFamily.familyID)
                }else {
                    if (sharedToFamilyList.contains(currentFamily.familyID)){
                        sharedToFamilyList.remove(currentFamily.familyID)
                    }
                }
            }
        }
    }

    inner class CreatePostFamiliesHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val familyProfilePic = binding.itemCreatePostFamilyImg
        val familyName = binding.itemCreatePostFamilyName
        val shareToFamilySwitch = binding.newPostShareBtn
    }

    fun getStorageReference(firebaseStorage: FirebaseStorage) {
        this.firebaseStorage = firebaseStorage
    }

    fun setSharedToFamilies(): MutableList<String> {
        return sharedToFamilyList
    }

}