package com.example.myapplication.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.LandlordItemPropertyGridBinding
import com.example.myapplication.model.Property

class LandlordGridAdapter(
    private val onDetailsClick: (Property) -> Unit
) : ListAdapter<Property, LandlordGridAdapter.PropertyViewHolder>(PropertyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val binding = LandlordItemPropertyGridBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PropertyViewHolder(binding)
    }


    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PropertyViewHolder(private val binding: LandlordItemPropertyGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(property: Property) {
            binding.textPropertyTitle.text = property.title
            binding.textPropertyLocation.text = "City: ${property.location}"

            /// Image Loading
            val imageUrl = property.imageUrls?.firstOrNull()
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(binding.imageProperty.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_property_image2)
                    .error(R.drawable.ic_property_image2)
                    .centerCrop()
                    .into(binding.imageProperty)
            } else {
                ///Default image if no URL
                binding.imageProperty.setImageResource(R.drawable.ic_property_image2)
            }

            ///Handle clicks
            binding.btnDetails.setOnClickListener { onDetailsClick(property) }
            binding.root.setOnClickListener { onDetailsClick(property) }
        }
    }

    class PropertyDiffCallback : DiffUtil.ItemCallback<Property>() {
        override fun areItemsTheSame(oldItem: Property, newItem: Property): Boolean {
            return oldItem.propertyId == newItem.propertyId
        }

        override fun areContentsTheSame(oldItem: Property, newItem: Property): Boolean {
            return oldItem == newItem
        }
    }
}
