package com.example.familytime.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.familytime.R
import com.example.familytime.databinding.ItemAllFamiliesPostsBinding
import com.example.familytime.models.Posts
import com.google.firebase.storage.FirebaseStorage

class AllFamiliesPostsAdapter :
    RecyclerView.Adapter<AllFamiliesPostsAdapter.AllFamiliesPostsHolder>() {

    private lateinit var binding: ItemAllFamiliesPostsBinding
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllFamiliesPostsHolder {
        binding =
            ItemAllFamiliesPostsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AllFamiliesPostsHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return asyncDiffer.currentList.size
    }

    override fun onBindViewHolder(holder: AllFamiliesPostsHolder, position: Int) {
        val currentPost = asyncDiffer.currentList[position]
        holder.itemView.setOnClickListener {
            onItemClickListener?.let { it(currentPost) }
        }
        holder.apply {

            Glide.with(postImage)
                .load(firebaseStorage.getReferenceFromUrl(currentPost.postsUri!!))
                .into(postImage)


            postDescription.text = currentPost.description
        }
    }

    inner class AllFamiliesPostsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postImage = binding.itemImgAllFamilies
        val postDescription = binding.allFamiliesPostsDescription
    }

    fun getStorageReference(firebaseStorage: FirebaseStorage) {
        this.firebaseStorage = firebaseStorage
    }


    private var onItemClickListener: ((Posts) -> Unit)? = null

    fun setOnItemClickListener(listener: (Posts) -> Unit) {
        onItemClickListener = listener
    }
}