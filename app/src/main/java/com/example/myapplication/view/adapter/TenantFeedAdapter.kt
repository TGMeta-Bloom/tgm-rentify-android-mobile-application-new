package com.example.myapplication.view.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemTenantFeedPostBinding
import com.example.myapplication.model.TenantPost

// UPDATE: Now accepts TWO callbacks (Helpful Click AND Hide Post)
class TenantFeedAdapter(
    private val onHelpfulClicked: (String, Int) -> Unit,
    private val onHidePost: (String) -> Unit
) : ListAdapter<TenantPost, TenantFeedAdapter.PostViewHolder>(DiffCallback) {

    class PostViewHolder(private val binding: ItemTenantFeedPostBinding, val context: Context) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: TenantPost, onHelpfulClicked: (String, Int) -> Unit, onHidePost: (String) -> Unit) {

            // 1. Basic Data
            binding.tvAuthorName.text = post.userName
            binding.tvPostContent.text = post.caption
            binding.tvHelpfulCount.text = "${post.helpfulCount} Helpful"

            // 2. Time
            if (post.timestamp != null) {
                val now = System.currentTimeMillis()
                binding.tvPostTime.text = DateUtils.getRelativeTimeSpanString(
                    post.timestamp.time, now, DateUtils.MINUTE_IN_MILLIS
                )
            } else {
                binding.tvPostTime.text = "Just now"
            }

            // 3. Avatar
            Glide.with(binding.root).load(post.userAvatarUrl)
                .placeholder(R.drawable.ic_tenant_profile_placeholder).circleCrop()
                .into(binding.ivAuthorAvatar)

            // 4. Main Image
            if (post.postImageUrl.isNotEmpty()) {
                binding.ivPostImage.visibility = View.VISIBLE
                Glide.with(binding.root).load(post.postImageUrl).into(binding.ivPostImage)
            } else {
                binding.ivPostImage.visibility = View.GONE
            }

            // 5. Helpful Button Logic
            updateHelpfulButtonStyle(post.isHelpfulClicked)

            binding.btnHelpful.setOnClickListener {
                post.isHelpfulClicked = !post.isHelpfulClicked
                if (post.isHelpfulClicked) post.helpfulCount++ else post.helpfulCount--

                binding.tvHelpfulCount.text = "${post.helpfulCount} Helpful"
                updateHelpfulButtonStyle(post.isHelpfulClicked)

                onHelpfulClicked(post.id, post.helpfulCount)
            }

            // 6. HIDE POST MENU LOGIC (This fixes your error)
            binding.btnMoreOptions.setOnClickListener { view ->
                val popup = PopupMenu(context, view)
                // Make sure you created res/menu/menu_post_item.xml !
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
        holder.bind(getItem(position), onHelpfulClicked, onHidePost)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<TenantPost>() {
        override fun areItemsTheSame(oldItem: TenantPost, newItem: TenantPost) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: TenantPost, newItem: TenantPost) = oldItem == newItem
    }
}