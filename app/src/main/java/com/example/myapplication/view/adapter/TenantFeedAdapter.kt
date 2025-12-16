package com.example.myapplication.view.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemTenantFeedPostBinding
import com.example.myapplication.model.TenantPost

class TenantFeedAdapter(
    private val onHelpfulClicked: (String, Int) -> Unit,
    private val onHidePost: (String) -> Unit
) : ListAdapter<TenantPost, TenantFeedAdapter.PostViewHolder>(DiffCallback) {

    // Set to store the IDs of posts the current user has clicked during this session
    private val clickedPostIds = mutableSetOf<String>()

    class PostViewHolder(private val binding: ItemTenantFeedPostBinding, val context: Context) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            post: TenantPost,
            clickedPostIds: MutableSet<String>,
            onHelpfulClicked: (String, Int) -> Unit,
            onHidePost: (String) -> Unit
        ) {

            // 1. Author Name & Caption
            binding.tvAuthorName.text = post.userName ?: "Unknown User"
            binding.tvPostContent.text = post.caption

            // 2. FIXED: Real Timestamp Logic
            if (post.timestamp != null) {
                val now = System.currentTimeMillis()
                // Calculates "5 minutes ago", "Yesterday", etc.
                val timeAgo = DateUtils.getRelativeTimeSpanString(
                    post.timestamp.time,
                    now,
                    DateUtils.MINUTE_IN_MILLIS
                )
                binding.tvTimestamp.text = timeAgo
            } else {
                binding.tvTimestamp.text = "Just now"
            }

            // 3. Helper Count & State
            val isClicked = clickedPostIds.contains(post.id)
            updateHelpfulButtonStyle(isClicked)
            binding.tvHelpfulCount.text = "${post.helpfulCount} Helpful"

            // 4. FIXED: Author Avatar Loading
            if (!post.userAvatarUrl.isNullOrEmpty()) {
                Glide.with(context)
                    .load(post.userAvatarUrl)
                    .placeholder(R.drawable.ic_tenant_profile_placeholder)
                    .error(R.drawable.ic_tenant_profile_placeholder)
                    .circleCrop()
                    .into(binding.ivAuthorAvatar)
            } else {
                // Explicitly set placeholder if no URL
                binding.ivAuthorAvatar.setImageResource(R.drawable.ic_tenant_profile_placeholder)
            }

            // 5. FIXED: Post Image Loading (Hide ImageView if empty)
            if (!post.postImageUrl.isNullOrEmpty()) {
                binding.ivPostImage.visibility = View.VISIBLE
                Glide.with(context)
                    .load(post.postImageUrl)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(binding.ivPostImage)
            } else {
                binding.ivPostImage.visibility = View.GONE
            }

            // 6. Helpful Button Click Logic
            binding.btnHelpful.setOnClickListener {
                if (clickedPostIds.contains(post.id)) {
                    Toast.makeText(context, "You already marked this post as helpful.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Mark as clicked locally
                clickedPostIds.add(post.id)
                post.helpfulCount++

                // Update UI immediately
                binding.tvHelpfulCount.text = "${post.helpfulCount} Helpful"
                updateHelpfulButtonStyle(true)

                // Update Database
                onHelpfulClicked(post.id, post.helpfulCount)
            }

            // 7. Hide Post Menu Logic
            binding.btnMoreOptions.setOnClickListener { view ->
                val popup = PopupMenu(context, view)
                popup.menuInflater.inflate(R.menu.menu_tenant_post_options, popup.menu)

                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_hide -> {
                            onHidePost(post.id)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }

        private fun updateHelpfulButtonStyle(isClicked: Boolean) {
            if (isClicked) {
                // Active State (Blue)
                val activeBgColor = ContextCompat.getColor(context, R.color.window_background_light_blue)
                val activeContentColor = ContextCompat.getColor(context, R.color.app_blue)

                binding.btnHelpful.backgroundTintList = ColorStateList.valueOf(activeBgColor)
                binding.btnHelpful.setTextColor(activeContentColor)
                binding.btnHelpful.iconTint = ColorStateList.valueOf(activeContentColor)
            } else {
                // Inactive State (Gray)
                val inactiveContentColor = ContextCompat.getColor(context, R.color.app_text_gray)

                binding.btnHelpful.backgroundTintList = ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
                binding.btnHelpful.setTextColor(inactiveContentColor)
                binding.btnHelpful.iconTint = ColorStateList.valueOf(inactiveContentColor)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemTenantFeedPostBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PostViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position), clickedPostIds, onHelpfulClicked, onHidePost)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<TenantPost>() {
        override fun areItemsTheSame(oldItem: TenantPost, newItem: TenantPost) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: TenantPost, newItem: TenantPost) = oldItem == newItem
    }
}