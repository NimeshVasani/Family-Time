package com.example.familytime.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.familytime.R
import com.example.familytime.databinding.ItemFamilyBinding
import com.example.familytime.models.Family
import com.google.firebase.storage.FirebaseStorage


class AllFamiliesAdapter :
    RecyclerView.Adapter<AllFamiliesAdapter.AllFamiliesHolder>() {

    private lateinit var binding: ItemFamilyBinding
    private lateinit var firebaseStorage: FirebaseStorage

    private var differCallback = object : DiffUtil.ItemCallback<Family>() {
        override fun areItemsTheSame(oldItem: Family, newItem: Family): Boolean {
            return oldItem.familyName == newItem.familyName
        }

        override fun areContentsTheSame(oldItem: Family, newItem: Family): Boolean {
            return oldItem == newItem
        }

    }
    var asyncDiffer: AsyncListDiffer<Family> = AsyncListDiffer(this, differCallback)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllFamiliesHolder {
        binding =
            ItemFamilyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AllFamiliesHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return asyncDiffer.currentList.size
    }

    override fun onBindViewHolder(holder: AllFamiliesHolder, position: Int) {
        val currentFamily = asyncDiffer.currentList[position]
        holder.itemView.setOnClickListener {
            onItemClickListener?.let { it(currentFamily.familyID) }
        }
        holder.apply {
            if (!currentFamily.familyProfilePicId.isNullOrBlank()) {
                Glide.with(familyProfilePic)
                    .load(firebaseStorage.getReferenceFromUrl(currentFamily.familyProfilePicId))
                    .into(familyProfilePic)
            } else {
                familyProfilePic.setImageResource(R.drawable.ic_family_time)
            }
            familyName.text = currentFamily.familyName
            familyAbout.text = currentFamily.familyDescription ?: ""
        }
    }

    inner class AllFamiliesHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val familyProfilePic = binding.itemImgFamily
        val familyName = binding.familyName
        val familyAbout = binding.allFamiliesAbout
    }


    private var onItemClickListener: ((String) -> Unit)? = null

    fun setOnItemClickListener(listener: (String) -> Unit) {
        onItemClickListener = listener
    }

    fun getStorageReference(firebaseStorage: FirebaseStorage) {
        this.firebaseStorage = firebaseStorage
    }
}