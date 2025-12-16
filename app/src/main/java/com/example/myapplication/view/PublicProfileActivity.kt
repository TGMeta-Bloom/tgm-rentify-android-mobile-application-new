package com.example.myapplication.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.model.User
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Locale

class PublicProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_public_profile)

        val user = intent.getSerializableExtra("USER_DATA") as? User
        if (user == null) {
            Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        updateUI(user)
    }

    private fun updateUI(user: User) {
        val ivCoverPhoto: ImageView = findViewById(R.id.iv_cover_photo)
        val ivProfilePhoto: CircleImageView = findViewById(R.id.iv_profile_photo)
        val tvProfileName: TextView = findViewById(R.id.tv_profile_name)
        val tvUserRole: TextView = findViewById(R.id.tv_user_role)
        val tvProfileBio: TextView = findViewById(R.id.tv_profile_bio)
        val tvEmailContact: TextView = findViewById(R.id.tv_email_contact)
        val tvPhoneContact: TextView = findViewById(R.id.tv_phone_contact)

        // Capitalize Name
        val firstName = user.firstName.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        val lastName = user.lastName.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        tvProfileName.text = "$firstName $lastName"

        tvUserRole.text = user.role
        if (user.role.equals("Landlord", ignoreCase = true)) {
            tvUserRole.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
        } else {
            tvUserRole.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
        }

        tvProfileBio.text = user.bio.ifEmpty { "No bio provided." }
        tvEmailContact.text = user.email

        if (user.isPhonePublic) {
            tvPhoneContact.visibility = View.VISIBLE
            tvPhoneContact.text = user.mobileNumber
        } else {
            tvPhoneContact.visibility = View.GONE
        }

        if (!user.profileImageUrl.isNullOrEmpty()) {
            Glide.with(this).load(user.profileImageUrl).placeholder(R.drawable.ic_default_profile).into(ivProfilePhoto)
        } else {
            ivProfilePhoto.setImageResource(R.drawable.ic_default_profile)
        }

        if (!user.coverImageUrl.isNullOrEmpty()) {
            Glide.with(this).load(user.coverImageUrl).placeholder(R.drawable.bg_login_header).into(ivCoverPhoto)
        } else {
            ivCoverPhoto.setImageResource(R.drawable.bg_login_header)
        }
    }
}
