package com.example.myapplication.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.model.User
import de.hdodenhof.circleimageview.CircleImageView

class UserSearchAdapter(private val onClick: (User) -> Unit) :
    ListAdapter<User, UserSearchAdapter.UserViewHolder>(UserDiffCallback) {

    class UserViewHolder(itemView: View, val onClick: (User) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val userName: TextView = itemView.findViewById(R.id.tv_user_name)
        private val userRole: TextView = itemView.findViewById(R.id.tv_user_role)
        private val userAvatar: CircleImageView = itemView.findViewById(R.id.iv_user_avatar)
        private val privacyStatus: TextView = itemView.findViewById(R.id.tv_privacy_status)
        private var currentUser: User? = null

        init {
            itemView.setOnClickListener {
                currentUser?.let { user ->
                    if (user.isProfilePublic) {
                        onClick(user)
                    } else {
                        Toast.makeText(itemView.context, "This account is private", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        fun bind(user: User) {
            currentUser = user
            userName.text = "${user.firstName} ${user.lastName}"
            userRole.text = user.role

            if (user.isProfilePublic) {
                privacyStatus.text = "Public"
                privacyStatus.setTextColor(Color.parseColor("#4CAF50")) // Green
            } else {
                privacyStatus.text = "Private"
                privacyStatus.setTextColor(Color.RED) // Red
            }

            if (!user.profileImageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(user.profileImageUrl)
                    .placeholder(R.drawable.ic_default_profile)
                    .into(userAvatar)
            } else {
                userAvatar.setImageResource(R.drawable.ic_default_profile)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_search, parent, false)
        return UserViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }
}

object UserDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.userId == newItem.userId
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}
