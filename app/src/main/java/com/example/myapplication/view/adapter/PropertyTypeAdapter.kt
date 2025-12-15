package com.example.myapplication.view.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.LandlordItemPropertyTypeFilterBinding
import com.example.myapplication.model.PropertyType

class PropertyTypeAdapter(
    private val onTypeSelected: (PropertyType) -> Unit
) : ListAdapter<PropertyType, PropertyTypeAdapter.TypeViewHolder>(TypeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TypeViewHolder {
        val binding = LandlordItemPropertyTypeFilterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TypeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TypeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TypeViewHolder(private val binding: LandlordItemPropertyTypeFilterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(propertyType: PropertyType) {
            binding.textFilterName.text = propertyType.name

            // Set state based on model property
            binding.textFilterName.isSelected = propertyType.isSelected

            binding.root.setOnClickListener {
                onTypeSelected(propertyType)
            }
        }
    }


    class TypeDiffCallback : DiffUtil.ItemCallback<PropertyType>() {
        override fun areItemsTheSame(oldItem: PropertyType, newItem: PropertyType): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: PropertyType, newItem: PropertyType): Boolean {
            return oldItem == newItem
        }
    }
}