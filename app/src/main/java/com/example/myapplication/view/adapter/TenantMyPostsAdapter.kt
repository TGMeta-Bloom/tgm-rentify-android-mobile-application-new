package com.example.myapplication.view.adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemMyPostBinding
import com.example.myapplication.model.TenantPost

class TenantMyPostsAdapter(
    private val onDeleteClick: (TenantPost) -> Unit
) : ListAdapter<TenantPost, TenantMyPostsAdapter.MyPostViewHolder>(DiffCallback) {

    inner class MyPostViewHolder(private val binding: ItemMyPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: TenantPost) {
            binding.tvCaption.text = post.caption
            binding.btnHelpfulCount.text = "${post.helpfulCount} Helpful"

            // Format Time
            if (post.timestamp != null) {
                val now = System.currentTimeMillis()
                binding.tvTimestamp.text = DateUtils.getRelativeTimeSpanString(
                    post.timestamp.time, now, DateUtils.MINUTE_IN_MILLIS
                )
            }

            // Load Image
            if (post.postImageUrl.isNotEmpty()) {
                binding.ivPostImage.visibility = View.VISIBLE
                Glide.with(binding.root)
                    .load(post.postImageUrl)
                    .placeholder(R.drawable.ic_add_image_placeholder)
                    .centerCrop()
                    .into(binding.ivPostImage)
            } else {
                binding.ivPostImage.visibility = View.GONE
            }

            // Handle Delete
            binding.btnDelete.setOnClickListener {
                onDeleteClick(post)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPostViewHolder {
        val binding = ItemMyPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyPostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyPostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<TenantPost>() {
        override fun areItemsTheSame(oldItem: TenantPost, newItem: TenantPost) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: TenantPost, newItem: TenantPost) = oldItem == newItem
    }
}