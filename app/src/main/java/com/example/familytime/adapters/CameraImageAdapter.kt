package com.example.familytime.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.familytime.R
import com.example.familytime.databinding.ItemCameraImagesBinding

class CameraImageAdapter : RecyclerView.Adapter<CameraImageAdapter.CameraImageViewHolder>() {
    private lateinit var binding: ItemCameraImagesBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CameraImageViewHolder {
        binding =
            ItemCameraImagesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CameraImageViewHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return 10
    }

    override fun onBindViewHolder(holder: CameraImageViewHolder, position: Int) {
        holder.itemCameraImage.apply {
            setImageResource(R.drawable.background)
        }
    }

    inner class CameraImageViewHolder(itemView: View) : ViewHolder(itemView) {
        val itemCameraImage = binding.itemCameraImage
    }
}