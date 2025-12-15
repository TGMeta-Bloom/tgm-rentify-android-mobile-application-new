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
import com.google.firebase.auth.FirebaseAuth // Needed to eventually track user ID

// UPDATE: Now accepts TWO callbacks (Helpful Click AND Hide Post)
class TenantFeedAdapter(
    private val onHelpfulClicked: (String, Int) -> Unit,
    private val onHidePost: (String) -> Unit
) : ListAdapter<TenantPost, TenantFeedAdapter.PostViewHolder>(DiffCallback) {

    // NEW: Set to store the IDs of posts the current user has clicked during this session
    private val clickedPostIds = mutableSetOf<String>()

    class PostViewHolder(private val binding: ItemTenantFeedPostBinding, val context: Context) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: TenantPost, clickedPostIds: MutableSet<String>, onHelpfulClicked: (String, Int) -> Unit, onHidePost: (String) -> Unit) {

            // 1. Basic Data
            binding.tvAuthorName.text = post.userName
            binding.tvPostContent.text = post.caption

            // Note: post.isHelpfulClicked is unreliable as it's not loaded from DB

            // 2. Check current state from the session-tracking set
            val isClicked = clickedPostIds.contains(post.id)
            updateHelpfulButtonStyle(isClicked)
            binding.tvHelpfulCount.text = "${post.helpfulCount} Helpful"

            // 3. Time, Avatar, Image setup (unchanged)

            // 4. Helpful Button Logic (FIXED to prevent multiple clicks)
            binding.btnHelpful.setOnClickListener {
                if (clickedPostIds.contains(post.id)) {
                    // Already clicked in this session: Stop the action and inform user
                    Toast.makeText(context, "You already marked this post as helpful.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // First click:
                clickedPostIds.add(post.id) // Mark as clicked for the rest of the session

                // 1. Update local model count and UI
                post.helpfulCount++

                binding.tvHelpfulCount.text = "${post.helpfulCount} Helpful"
                updateHelpfulButtonStyle(true) // Set to active state

                // 2. Update database
                onHelpfulClicked(post.id, post.helpfulCount)
            }

            // 5. HIDE POST MENU LOGIC (unchanged)
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
            // ... (Style logic remains the same) ...
            if (isClicked) {
                // ACTIVE STATE:
                val activeBgColor = ContextCompat.getColor(context, R.color.window_background_light_blue)
                val activeContentColor = ContextCompat.getColor(context, R.color.app_blue)

                binding.btnHelpful.backgroundTintList = ColorStateList.valueOf(activeBgColor)
                binding.btnHelpful.setTextColor(activeContentColor)
                binding.btnHelpful.iconTint = ColorStateList.valueOf(activeContentColor)
            } else {
                // INACTIVE STATE:
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
        // PASS the state tracking set to the view holder
        holder.bind(getItem(position), clickedPostIds, onHelpfulClicked, onHidePost)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<TenantPost>() {
        override fun areItemsTheSame(oldItem: TenantPost, newItem: TenantPost) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: TenantPost, newItem: TenantPost) = oldItem == newItem
    }
}