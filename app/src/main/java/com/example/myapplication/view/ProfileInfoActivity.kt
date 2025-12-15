package com.example.myapplication.view

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.model.User
import com.example.myapplication.viewModel.ProfileViewModel
import com.google.android.material.textfield.TextInputEditText
import de.hdodenhof.circleimageview.CircleImageView

class ProfileInfoActivity : AppCompatActivity() {

    private val viewModel: ProfileViewModel by viewModels()
    
    private lateinit var etFirstName: TextInputEditText
    private lateinit var etLastName: TextInputEditText
    private lateinit var etUsername: TextInputEditText // NEW
    private lateinit var etEmail: TextInputEditText
    private lateinit var etBio: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etAddress: TextInputEditText
    private lateinit var spinnerCity: AppCompatSpinner
    private lateinit var btnSave: Button
    private lateinit var ivProfile: CircleImageView
    private lateinit var tvNameInfo: TextView
    private lateinit var tvBioInfoHeader: TextView

    private var currentUser: User? = null
    
    // Sample cities list - you can move this to strings.xml or load dynamically
    private val cities = listOf("Colombo", "Kandy", "Galle", "Matara", "Negombo", "Jaffna", "Kurunegala", "Ratnapura", "Trincomalee", "Batticaloa", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_info)

        // Bind Views using the Correct XML IDs
        val btnBack: ImageView = findViewById(R.id.btn_back_info)
        btnBack.setOnClickListener { finish() }

        etFirstName = findViewById(R.id.et_first_name)
        etLastName = findViewById(R.id.et_last_name)
        etUsername = findViewById(R.id.et_username) // NEW
        etEmail = findViewById(R.id.et_email)
        etBio = findViewById(R.id.et_bio)
        etPhone = findViewById(R.id.et_phone)
        etAddress = findViewById(R.id.et_address)
        spinnerCity = findViewById(R.id.spinner_city)
        btnSave = findViewById(R.id.btn_save_info)
        ivProfile = findViewById(R.id.image_profile_info)
        tvNameInfo = findViewById(R.id.tv_name_info)
        tvBioInfoHeader = findViewById(R.id.tv_bio_info_header)

        // Setup City Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCity.adapter = adapter

        // Load Data
        viewModel.userProfile.observe(this) { user ->
            if (user != null) {
                currentUser = user
                updateUI(user)
            }
        }
        
        viewModel.updateResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
                finish() // Close activity on success
            }.onFailure {
                Toast.makeText(this, "Update Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loadProfileData()

        // Save Logic
        btnSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun updateUI(user: User) {
        etFirstName.setText(user.firstName)
        etLastName.setText(user.lastName)
        etUsername.setText(user.username) // NEW
        etEmail.setText(user.email)
        etBio.setText(user.bio)
        etPhone.setText(user.mobileNumber)
        etAddress.setText(user.address)
        
        tvNameInfo.text = "${user.firstName} ${user.lastName}"
        tvBioInfoHeader.text = user.bio

        // Select City in Spinner
        val cityIndex = cities.indexOfFirst { it.equals(user.city, ignoreCase = true) }
        if (cityIndex >= 0) {
            spinnerCity.setSelection(cityIndex)
        }

        if (!user.profileImageUrl.isNullOrEmpty()) {
            Glide.with(this).load(user.profileImageUrl).placeholder(R.drawable.ic_default_profile).into(ivProfile)
        }
    }

    private fun saveProfile() {
        val user = currentUser ?: return
        
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val username = etUsername.text.toString().trim() // NEW
        val bio = etBio.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val city = spinnerCity.selectedItem.toString()

        val updatedUser = user.copy(
            firstName = firstName,
            lastName = lastName,
            username = if(username.isNotEmpty()) username else null, // Handle optional logic
            bio = bio,
            mobileNumber = phone,
            address = address,
            city = city
        )
        
        viewModel.updatePersonalDetails(updatedUser)
    }
}
