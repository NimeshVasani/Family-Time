package com.example.familytime.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.familytime.databinding.ItemFamilyAllPostBinding
import com.example.familytime.models.Posts
import com.google.firebase.storage.FirebaseStorage

class AllPostsAdapter : RecyclerView.Adapter<AllPostsAdapter.AllPostsHolder>() {

    private lateinit var binding: ItemFamilyAllPostBinding

    private lateinit var firebaseStorage: FirebaseStorage


    private var differCallback = object : DiffUtil.ItemCallback<Posts>() {
        override fun areItemsTheSame(oldItem: Posts, newItem: Posts): Boolean {
            return oldItem.postUid == newItem.postUid
        }

        override fun areContentsTheSame(oldItem: Posts, newItem: Posts): Boolean {
            return oldItem == newItem
        }

    }
    var asyncDiffer: AsyncListDiffer<Posts> = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllPostsHolder {
        binding =
            ItemFamilyAllPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AllPostsHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return asyncDiffer.currentList.size
    }

    override fun onBindViewHolder(holder: AllPostsHolder, position: Int) {
        val currentPost = asyncDiffer.currentList[position]
        holder.apply {

            Glide.with(postImage)
                .load(firebaseStorage.getReferenceFromUrl(currentPost.postsUri!!))
                .into(postImage)
            postSharedBy.text = "By ${currentPost.sharedByUserName}"
        }
    }

    inner class AllPostsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postImage = binding.itemImgShareByAll
        val postSharedBy = binding.postsSharedBy
    }

    fun getStorageReference(firebaseStorage: FirebaseStorage) {
        this.firebaseStorage = firebaseStorage
    }
}