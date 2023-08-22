package com.example.familytime.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.familytime.databinding.ItemYourPostsBinding
import com.example.familytime.models.Posts
import com.google.firebase.storage.FirebaseStorage

class YourPostsAdapter : RecyclerView.Adapter<YourPostsAdapter.YourPostsHolder>() {

    private lateinit var binding: ItemYourPostsBinding

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YourPostsHolder {
        binding = ItemYourPostsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return YourPostsHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return asyncDiffer.currentList.size
    }

    override fun onBindViewHolder(holder: YourPostsHolder, position: Int) {
        val currentPost = asyncDiffer.currentList[position]
        holder.apply {

            Glide.with(postImage)
                .load(firebaseStorage.getReferenceFromUrl(currentPost.postsUri!!))
                .into(postImage)

            postDescription.text = currentPost.description
        }
    }

    inner class YourPostsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postImage = binding.itemImgShareByYou
        val postDescription = binding.yourPostsDescription
    }

    fun getStorageReference(firebaseStorage: FirebaseStorage) {
        this.firebaseStorage = firebaseStorage
    }
}