package com.example.myapplication.view

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.model.User
import com.example.myapplication.viewModel.ProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class ProfileInfoActivity : AppCompatActivity() {

    private val viewModel: ProfileViewModel by viewModels()
    
    private lateinit var etFirstName: TextInputEditText
    private lateinit var etLastName: TextInputEditText
    private lateinit var etUsername: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etBio: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etAddress: TextInputEditText
    private lateinit var spinnerCity: AppCompatSpinner
    private lateinit var btnSave: Button
    private lateinit var ivProfile: CircleImageView
    private lateinit var ivCover: ImageView
    private lateinit var btnEditPhoto: ImageView
    private lateinit var btnEditCover: ImageView
    private lateinit var tvNameInfo: TextView
    private lateinit var tvBioInfoHeader: TextView
    private lateinit var progressBar: ProgressBar

    private var currentUser: User? = null
    private var tempImageUri: Uri? = null // For Camera
    
    private val cities = listOf("Colombo", "Kandy", "Galle", "Matara", "Negombo", "Jaffna", "Kurunegala", "Ratnapura", "Trincomalee", "Batticaloa", "Other")

    // --- Image Pickers ---

    // 1. Gallery Launchers
    private val pickProfileGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { uploadProfileImage(it) }
    }

    private val pickCoverGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { uploadCoverImage(it) }
    }

    // 2. Camera Launchers
    private val takeProfileCamera = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempImageUri != null) {
            uploadProfileImage(tempImageUri!!)
        }
    }

    private val takeCoverCamera = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempImageUri != null) {
            uploadCoverImage(tempImageUri!!)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_info)

        // Restore State (Fix for Camera returning null URI if activity was killed)
        if (savedInstanceState != null) {
            tempImageUri = savedInstanceState.getParcelable("temp_uri")
        }

        // Bind Views
        val btnBack: ImageView = findViewById(R.id.btn_back_info)
        btnBack.setOnClickListener { finish() }

        etFirstName = findViewById(R.id.et_first_name)
        etLastName = findViewById(R.id.et_last_name)
        etUsername = findViewById(R.id.et_username)
        etEmail = findViewById(R.id.et_email)
        etBio = findViewById(R.id.et_bio)
        etPhone = findViewById(R.id.et_phone)
        etAddress = findViewById(R.id.et_address)
        spinnerCity = findViewById(R.id.spinner_city)
        btnSave = findViewById(R.id.btn_save_info)
        
        ivProfile = findViewById(R.id.image_profile_info)
        ivCover = findViewById(R.id.iv_cover_info)
        
        btnEditPhoto = findViewById(R.id.btn_edit_photo_info)
        btnEditCover = findViewById(R.id.btn_edit_cover_info)

        tvNameInfo = findViewById(R.id.tv_name_info)
        tvBioInfoHeader = findViewById(R.id.tv_bio_info_header)
        progressBar = findViewById(R.id.progress_bar)

        // Setup City Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCity.adapter = adapter

        // Setup Click Listeners (Dialog Options)
        btnEditPhoto.setOnClickListener {
            showImagePickerOptions(isProfile = true)
        }

        btnEditCover.setOnClickListener {
            showImagePickerOptions(isProfile = false)
        }

        // Load Data
        viewModel.userProfile.observe(this) { user ->
            if (user != null) {
                currentUser = user
                updateUI(user)
            }
        }
        
        viewModel.updateResult.observe(this) { result ->
            // Hide Progress Bar when result is received
            progressBar.visibility = View.GONE
            btnSave.isEnabled = true
            
            result.onSuccess {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                if (it.contains("Updated Successfully", true)) {
                     // finish() 
                }
            }.onFailure {
                Toast.makeText(this, "Update Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loadProfileData()

        btnSave.setOnClickListener {
            saveProfile()
        }
    }

    // FIX: Save tempImageUri state to prevent loss during camera activity
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("temp_uri", tempImageUri)
    }

    private fun showImagePickerOptions(isProfile: Boolean) {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Remove Photo")
        MaterialAlertDialogBuilder(this)
            .setTitle(if (isProfile) "Edit Profile Photo" else "Edit Cover Photo")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> { // Take Photo
                        val uri = createTempImageUri()
                        if (uri != null) {
                            tempImageUri = uri
                            if (isProfile) takeProfileCamera.launch(uri) else takeCoverCamera.launch(uri)
                        } else {
                            Toast.makeText(this, "Error creating temp file", Toast.LENGTH_SHORT).show()
                        }
                    }
                    1 -> { // Gallery
                        if (isProfile) pickProfileGallery.launch("image/*") else pickCoverGallery.launch("image/*")
                    }
                    2 -> { // Remove
                        // Show progress for removal too
                        progressBar.visibility = View.VISIBLE
                        if (isProfile) viewModel.removeProfilePhoto() else viewModel.removeCoverPhoto()
                    }
                }
            }
            .show()
    }

    private fun createTempImageUri(): Uri? {
        val tempFile = File.createTempFile("IMG_${System.currentTimeMillis()}", ".jpg", cacheDir)
        return try {
            FileProvider.getUriForFile(this, "$packageName.provider", tempFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun uploadProfileImage(uri: Uri) {
        currentUser?.let { user ->
            // Show Loading
            progressBar.visibility = View.VISIBLE
            Toast.makeText(this, "Updating Profile Photo...", Toast.LENGTH_SHORT).show()
            ivProfile.setImageURI(uri) // Preview immediately
            viewModel.updateProfile(user, uri)
        }
    }

    private fun uploadCoverImage(uri: Uri) {
        currentUser?.let { user ->
            // Show Loading
            progressBar.visibility = View.VISIBLE
            Toast.makeText(this, "Updating Cover Photo...", Toast.LENGTH_SHORT).show()
            ivCover.setImageURI(uri) // Preview immediately
            viewModel.updateCoverImage(user, uri)
        }
    }

    private fun updateUI(user: User) {
        etFirstName.setText(user.firstName)
        etLastName.setText(user.lastName)
        etUsername.setText(user.username)
        etEmail.setText(user.email)
        etBio.setText(user.bio)
        etPhone.setText(user.mobileNumber)
        etAddress.setText(user.address)
        
        tvNameInfo.text = "${user.firstName} ${user.lastName}"
        tvBioInfoHeader.text = user.bio

        val cityIndex = cities.indexOfFirst { it.equals(user.city, ignoreCase = true) }
        if (cityIndex >= 0) {
            spinnerCity.setSelection(cityIndex)
        }

        if (!user.profileImageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(user.profileImageUrl)
                .placeholder(R.drawable.ic_default_profile)
                .into(ivProfile)
        } else {
            ivProfile.setImageResource(R.drawable.ic_default_profile)
        }

        if (!user.coverImageUrl.isNullOrEmpty()) {
             Glide.with(this)
                .load(user.coverImageUrl)
                .placeholder(R.drawable.bg_login_header)
                .into(ivCover)
        } else {
            ivCover.setImageResource(R.drawable.bg_login_header)
        }
    }

    private fun saveProfile() {
        val user = currentUser ?: return
        
        // Show Loading
        progressBar.visibility = View.VISIBLE
        btnSave.isEnabled = false // Prevent double click
        
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val bio = etBio.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val city = spinnerCity.selectedItem.toString()

        val updatedUser = user.copy(
            firstName = firstName,
            lastName = lastName,
            username = if(username.isNotEmpty()) username else null,
            bio = bio,
            mobileNumber = phone,
            address = address,
            city = city
        )
        
        viewModel.updatePersonalDetails(updatedUser)
    }
}