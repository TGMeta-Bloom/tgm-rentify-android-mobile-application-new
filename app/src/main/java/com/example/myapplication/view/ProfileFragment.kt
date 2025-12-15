package com.example.myapplication.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.model.User
import com.example.myapplication.utils.SharedPreferencesHelper
import com.example.myapplication.viewModel.ProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var ivCoverPhoto: ImageView
    private lateinit var btnEditCoverPhoto: ImageButton
    private lateinit var ivProfilePhoto: CircleImageView
    private lateinit var btnEditProfilePhoto: ImageButton
    private lateinit var tvProfileName: TextView

    // Separate Views for Icon and Text
    private lateinit var ivRoleIcon: ImageView
    private lateinit var tvUserRole: TextView

    private lateinit var tvProfileBio: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvEmailContact: TextView
    private lateinit var tvPhoneContact: TextView
    private lateinit var btnManageProperties: Button
    private lateinit var btnMenu: ImageButton

    private val viewModel: ProfileViewModel by viewModels()
    private var currentUser: User? = null

    // Camera/Gallery Logic State
    private var isEditingProfilePhoto = true
    private var tempImageUri: Uri? = null

    // Launcher for Gallery
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { handleImageSelection(it) }
    }

    // Launcher for Camera
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempImageUri != null) {
            handleImageSelection(tempImageUri!!)
        }
    }

    // Launcher for Camera Permission
    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission is required to take photos.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivCoverPhoto = view.findViewById(R.id.iv_cover_photo)
        btnEditCoverPhoto = view.findViewById(R.id.btn_edit_cover_photo)
        ivProfilePhoto = view.findViewById(R.id.iv_profile_photo)
        btnEditProfilePhoto = view.findViewById(R.id.btn_edit_profile_photo)
        tvProfileName = view.findViewById(R.id.tv_profile_name)

        // Updated Bindings
        ivRoleIcon = view.findViewById(R.id.iv_role_icon)
        tvUserRole = view.findViewById(R.id.tv_user_role)

        tvProfileBio = view.findViewById(R.id.tv_profile_bio)
        tvLocation = view.findViewById(R.id.tv_location)
        tvEmailContact = view.findViewById(R.id.tv_email_contact)
        tvPhoneContact = view.findViewById(R.id.tv_phone_contact)
        btnManageProperties = view.findViewById(R.id.btn_manage_properties)
        btnMenu = view.findViewById(R.id.btn_menu)

        // Long Press to Toggle Role (Frontend Testing)
        tvProfileName.setOnLongClickListener {
            viewModel.switchRole()
            true
        }

        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                currentUser = user
                updateUI(user)
            }
        }

        viewModel.updateResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(requireContext(), "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Setup Image Edit Listeners
        btnEditProfilePhoto.setOnClickListener {
            isEditingProfilePhoto = true
            showImageSourceDialog()
        }

        btnEditCoverPhoto.setOnClickListener {
            isEditingProfilePhoto = false
            showImageSourceDialog()
        }

        ivCoverPhoto.setOnClickListener {
            isEditingProfilePhoto = false
            showImageSourceDialog()
        }

        // Menu Logic - Updated to handle Selection Highlight
        btnMenu.setOnClickListener {
            val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
            val navView = requireActivity().findViewById<NavigationView>(R.id.nav_view)

            if (navView != null) {
                navView.menu.clear()
                if (navView.headerCount > 0) {
                    navView.removeHeaderView(navView.getHeaderView(0))
                }
                // Inflate custom layout
                val headerView = navView.inflateHeaderView(R.layout.nav_drawer_custom_layout)

                // Bind User Info to Header
                currentUser?.let { user ->
                    headerView.findViewById<TextView>(R.id.drawer_user_name)?.text = "${user.firstName} ${user.lastName}"
                    val headerImage = headerView.findViewById<ImageView>(R.id.drawer_profile_photo)
                    if (!user.profileImageUrl.isNullOrEmpty() && headerImage != null) {
                        Glide.with(this).load(user.profileImageUrl).placeholder(R.drawable.ic_default_profile).into(headerImage)
                    }
                }

                // === Logic for Highlight Selection ===
                val navItems = listOf(
                    headerView.findViewById<View>(R.id.nav_profile_info),
                    headerView.findViewById<View>(R.id.nav_privacy_security),
                    headerView.findViewById<View>(R.id.nav_account_delete),
                    headerView.findViewById<View>(R.id.nav_app_settings)
                )

                // Helper to reset all items to unselected
                fun clearSelection() {
                    navItems.forEach { it?.isSelected = false }
                }

                navItems.forEachIndexed { index, item ->
                    item?.setOnClickListener {
                        clearSelection()
                        item.isSelected = true

                        when (index) {
                            0 -> { // Profile Info
                                val intent = Intent(requireContext(), ProfileInfoActivity::class.java)
                                startActivity(intent)
                            }
                            1 -> { // Privacy & Security
                                val intent = Intent(requireContext(), AccountPrivacyActivity::class.java)
                                startActivity(intent)
                            }
                            2 -> { // Account Delete
                                val intent = Intent(requireContext(), AccountDeleteActivity::class.java)
                                startActivity(intent)
                            }
                            3 -> { // App Settings
                                val intent = Intent(requireContext(), AppSettingsActivity::class.java)
                                startActivity(intent)
                            }
                        }
                        drawerLayout?.closeDrawer(GravityCompat.START)
                    }
                }

                // Logout Logic - Updated to Material Design
                headerView.findViewById<View>(R.id.nav_logout)?.setOnClickListener {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Log Out")
                        .setMessage("Are you sure you want to log out?")
                        .setIcon(R.drawable.ic_lock_power_off)
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Log Out") { _, _ ->
                            // Clear SharedPreferences
                            val sharedPrefsHelper = SharedPreferencesHelper(requireContext())
                            sharedPrefsHelper.clear()

                            // Perform Logout
                            val intent = Intent(requireContext(), LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            requireActivity().finishAffinity()
                        }
                        .show()
                }

                // Set "Profile Info" as selected by default
                navItems.getOrNull(0)?.isSelected = true
            }
            drawerLayout?.openDrawer(GravityCompat.START)
        }

        btnManageProperties.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_LandlordAddPropertyFragment)
        }

        viewModel.loadProfileData()
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        // Replaced AlertDialog.Builder with MaterialAlertDialogBuilder to fix unresolved reference
        // and maintain consistency with the user-friendly Material design.
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Image")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndLaunch()
                    1 -> pickImageLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is granted
                launchCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                // Show an explanation to the user
                Toast.makeText(requireContext(), "Camera permission is needed to take profile photos.", Toast.LENGTH_LONG).show()
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                // Request permission
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun launchCamera() {
        try {
            val tempFile = File.createTempFile("temp_image", ".jpg", requireContext().cacheDir)
            tempImageUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                tempFile
            )
            takePictureLauncher.launch(tempImageUri)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error launching camera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleImageSelection(uri: Uri) {
        if (isEditingProfilePhoto) {
            ivProfilePhoto.setImageURI(uri) // Optimistic update
            currentUser?.let { user -> viewModel.updateProfile(user, uri) }
        } else {
            ivCoverPhoto.setImageURI(uri) // Optimistic update
            currentUser?.let { user -> viewModel.updateCoverImage(user, uri) }
        }
    }

    override fun onResume() {
        super.onResume()
        val navView = activity?.findViewById<NavigationView>(R.id.nav_view)
        if (navView != null) {
            navView.menu.clear()
            if (navView.headerCount > 0) {
                navView.removeHeaderView(navView.getHeaderView(0))
            }
            navView.inflateHeaderView(R.layout.nav_drawer_custom_layout)
        }
    }

    override fun onPause() {
        super.onPause()
        val navView = activity?.findViewById<NavigationView>(R.id.nav_view)
        if (navView != null) {
            navView.menu.clear()
            navView.inflateMenu(R.menu.nav_drawer_menu)
            if (navView.headerCount > 0) {
                navView.removeHeaderView(navView.getHeaderView(0))
            }
            navView.inflateHeaderView(R.layout.nav_header_main)
        }
    }

    private fun updateUI(user: User) {
        tvProfileName.text = "${user.firstName} ${user.lastName}"
        tvProfileBio.text = user.bio.ifEmpty { "No bio provided." }
        tvLocation.text = if (user.city.isNotEmpty()) "Lives in ${user.city}" else "Location not set"
        tvEmailContact.text = user.email
        tvPhoneContact.text = user.mobileNumber

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

        val role = user.role.trim()

        // Update Role Label dynamically
        tvUserRole.text = role
        tvUserRole.visibility = View.VISIBLE

        if (role.equals("Landlord", ignoreCase = true)) {
            // Landlord: Show Button, Blue Text, Check Icon
            btnManageProperties.visibility = View.VISIBLE
            tvUserRole.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))

            ivRoleIcon.visibility = View.VISIBLE
            ivRoleIcon.setImageResource(R.drawable.ic_check_circle_outline)
            ivRoleIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        } else {
            // Tenant: Hide Button, Grey Text, User Icon
            btnManageProperties.visibility = View.GONE
            tvUserRole.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))

            ivRoleIcon.visibility = View.VISIBLE
            ivRoleIcon.setImageResource(R.drawable.ic_check_circle_outline)
            ivRoleIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        }
    }
}
