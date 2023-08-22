package com.example.familytime.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.familytime.R
import com.example.familytime.databinding.ItemAllFamiliesNameBinding
import com.example.familytime.models.Family


class AllFamiliesNameAdapter :
    RecyclerView.Adapter<AllFamiliesNameAdapter.AllFamiliesNameHolder>() {

    private lateinit var binding: ItemAllFamiliesNameBinding
    private var selectedItemPosition: Int? = null


    private var differCallback = object : DiffUtil.ItemCallback<Family>() {
        override fun areItemsTheSame(oldItem: Family, newItem: Family): Boolean {
            return oldItem.familyName == newItem.familyName
        }

        override fun areContentsTheSame(oldItem: Family, newItem: Family): Boolean {
            return oldItem == newItem
        }

    }
    var asyncDiffer: AsyncListDiffer<Family> = AsyncListDiffer(this, differCallback)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllFamiliesNameHolder {
        binding =
            ItemAllFamiliesNameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AllFamiliesNameHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return asyncDiffer.currentList.size
    }

    override fun onBindViewHolder(holder: AllFamiliesNameHolder, position: Int) {
        val currentFamily = asyncDiffer.currentList[position]
        holder.familyName.apply {
            text = currentFamily.familyName
            setBackgroundColor(
                if (selectedItemPosition == position) {
                    ContextCompat.getColor(
                        context,
                        R.color.orange
                    )
                } else {
                    Color.WHITE
                }
            )
        }

        holder.familyName.setOnClickListener {
            // Update selected item position and notify adapter to redraw
            val previousSelectedItemPosition = selectedItemPosition
            selectedItemPosition = holder.adapterPosition
            previousSelectedItemPosition?.let { notifyItemChanged(it) }
            notifyItemChanged(holder.adapterPosition)

            // Notify the listener
            onItemClickListener?.let { it(currentFamily.familyID) }
        }
    }

    inner class AllFamiliesNameHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val familyName = binding.allFeedBtn
    }


    private var onItemClickListener: ((String) -> Unit)? = null

    fun setOnItemClickListener(listener: (String) -> Unit) {
        onItemClickListener = listener
    }

    fun clearSelectedItem() {
        val previousSelectedItemPosition = selectedItemPosition
        selectedItemPosition = null
        previousSelectedItemPosition?.let { notifyItemChanged(it) }
    }
}