package com.example.myapplication.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.model.Property

class LandlordPropertiesAdapter(
    private var properties: List<Property>,
    private val onEditClick: (Property) -> Unit,
    private val onDeleteClick: (Property) -> Unit
) : RecyclerView.Adapter<LandlordPropertiesAdapter.PropertyViewHolder>() {

    fun updateProperties(newProperties: List<Property>) {
        this.properties = newProperties
        notifyDataSetChanged()
    }


    inner class PropertyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPropertyImage: ImageView = itemView.findViewById(R.id.iv_property_image)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_property_title)
        val tvDescription: TextView = itemView.findViewById(R.id.tv_property_description)
        val tvLocation: TextView = itemView.findViewById(R.id.tv_property_location)
        val tvPrice: TextView = itemView.findViewById(R.id.tv_property_price)
        val tvType: TextView = itemView.findViewById(R.id.tv_property_type)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_property_status) // Added status view
        val tvContact: TextView = itemView.findViewById(R.id.tv_contact_number)
        val btnEdit: View = itemView.findViewById(R.id.btn_edit)
        val btnDelete: View = itemView.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_landlord_my_property, parent, false)
        return PropertyViewHolder(view)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property = properties[position]

        holder.tvTitle.text = property.title
        holder.tvDescription.text = property.description
        holder.tvLocation.text = property.location


        holder.tvPrice.text = "Rs. ${property.rentAmount.toInt()}"
        holder.tvType.text = property.propertyType
        holder.tvStatus.text = property.status
        holder.tvContact.text = property.contactNumber

        val imageUrl = property.imageUrls?.firstOrNull()

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_property_image2)
                .error(R.drawable.ic_property_image2)
                .into(holder.ivPropertyImage)
        } else {
            holder.ivPropertyImage.setImageResource(R.drawable.ic_property_image2)
        }

        holder.btnEdit.setOnClickListener { onEditClick(property) }
        holder.btnDelete.setOnClickListener { onDeleteClick(property) }
    }

    override fun getItemCount(): Int = properties.size
}