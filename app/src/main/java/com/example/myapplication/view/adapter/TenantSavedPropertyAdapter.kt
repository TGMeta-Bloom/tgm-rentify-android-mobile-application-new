package com.example.myapplication.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemTenantSavedPropertyBinding
import com.example.myapplication.model.SavedProperty

class TenantSavedPropertyAdapter(
    private val onRemoveClick: (SavedProperty) -> Unit
) : ListAdapter<SavedProperty, TenantSavedPropertyAdapter.SavedViewHolder>(SavedDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedViewHolder {
        val binding = ItemTenantSavedPropertyBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SavedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SavedViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SavedViewHolder(private val binding: ItemTenantSavedPropertyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(property: SavedProperty) {
            binding.tvPropertyTitle.text = property.title
            // Using "location" variable now
            binding.tvPropertyCity.text = property.location


            // Load the single saved image
            Glide.with(binding.root)
                .load(property.imageUrl)
                .placeholder(R.drawable.ic_add_image_placeholder)
                .centerCrop()
                .into(binding.ivPropertyImage)

            binding.btnRemove.setOnClickListener {
                onRemoveClick(property)
            }
        }
    }

    class SavedDiffCallback : DiffUtil.ItemCallback<SavedProperty>() {
        override fun areItemsTheSame(oldItem: SavedProperty, newItem: SavedProperty) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SavedProperty, newItem: SavedProperty) =
            oldItem == newItem
    }
}