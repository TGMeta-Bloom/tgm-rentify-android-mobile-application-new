package com.example.myapplication.view

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.FragmentTenantAddPostBinding
import com.example.myapplication.model.User
import com.example.myapplication.repository.TenantFeedRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

// Camera/Permission imports
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import android.app.AlertDialog

class TenantAddPostFragment : Fragment() {

    private var _binding: FragmentTenantAddPostBinding? = null
    private val binding get() = _binding!!

    // State variables
    private var selectedImageUri: Uri? = null
    private lateinit var repository: TenantFeedRepository
    private var currentUser: User? = null
    private lateinit var currentPhotoUri: Uri // URI for camera output file

    // --- ACTIVITY AND PERMISSION LAUNCHERS ---

    // 1. Launcher for picking from Gallery
    private val pickImageFromGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            handleImageUri(uri)
        }
    }

    // 2. Launcher for Camera Capture
    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            handleImageUri(currentPhotoUri)
        }
    }

    // 3. Launcher for Permissions
    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                launchCamera() // Permission granted, proceed to camera
            } else {
                Toast.makeText(requireContext(), "Camera permission denied.", Toast.LENGTH_SHORT).show()
            }
        }

    // --- FRAGMENT LIFECYCLE ---

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Log.d("AddPost", "onCreateView started")
        _binding = FragmentTenantAddPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // INITIALIZE: Pass Context to the Repository for ImgBB/FileUtils
        repository = TenantFeedRepository(requireContext())

        fetchCurrentUser()

        // Navigation Back
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Image Handlers: Show dialog for Camera or Gallery
        binding.btnUploadImage.setOnClickListener {
            showImageSourceDialog()
        }

        binding.btnRemoveImage.setOnClickListener {
            selectedImageUri = null
            showImagePreview(false, null)
        }

        // Post Button
        binding.btnPostNow.setOnClickListener {
            val caption = binding.etCaption.text.toString().trim()
            if (caption.isEmpty() && selectedImageUri == null) {
                Toast.makeText(context, "Please add text or a photo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (currentUser == null) {
                Toast.makeText(context, "User data not loaded yet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            setLoading(true)
            repository.addPost(caption, selectedImageUri, currentUser!!) { success, error ->
                setLoading(false)
                if (success) {
                    Toast.makeText(context, "Post created!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                    Log.e("AddPost", "Post Failed: $error")
                }
            }
        }
    }

    // --- NEW IMAGE LOGIC ---

    private fun handleImageUri(uri: Uri) {
        showImagePreview(true, uri)
    }

    private fun showImagePreview(show: Boolean, uri: Uri?) {
        if (show && uri != null) {
            selectedImageUri = uri
            binding.ivSelectedImage.visibility = View.VISIBLE
            binding.btnRemoveImage.visibility = View.VISIBLE
            binding.btnUploadImage.visibility = View.GONE
            binding.ivSelectedImage.setImageURI(uri)
        } else {
            selectedImageUri = null
            binding.ivSelectedImage.visibility = View.GONE
            binding.btnRemoveImage.visibility = View.GONE
            binding.btnUploadImage.visibility = View.VISIBLE
        }
    }

    // Dialog to choose image source
    private fun showImageSourceDialog() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Image Source")
        builder.setItems(options) { dialog, item ->
            when (item) {
                0 -> checkCameraPermission()
                1 -> pickImageFromGallery.launch("image/*")
            }
            dialog.dismiss()
        }
        builder.show()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        currentPhotoUri = createImageUri()
        takePicture.launch(currentPhotoUri)
    }

    private fun createImageUri(): Uri {
        // Create a temporary file in the external cache directory
        val imageFile = File(requireContext().externalCacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            requireContext(),
            // FIX: Use .provider to match your AndroidManifest.xml authority:
            "${requireContext().packageName}.provider",
            imageFile
        )
    }

    // --- EXISTING USER/VIEW MODEL LOGIC ---

    private fun fetchCurrentUser() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    currentUser = User(
                        userId = userId,
                        firstName = document.getString("firstName") ?: "",
                        lastName = document.getString("lastName") ?: "",
                        email = document.getString("email") ?: "",
                        profileImageUrl = document.getString("profileImageUrl")
                    )

                    val fullName = "${currentUser?.firstName} ${currentUser?.lastName}".trim()
                    binding.tvCurrentUserName.text = if (fullName.isNotEmpty()) fullName else "User"

                    val avatarUrl = currentUser?.profileImageUrl
                    if (!avatarUrl.isNullOrEmpty() && context != null) {
                        Glide.with(requireContext()).load(avatarUrl).circleCrop().into(binding.ivCurrentUserAvatar)
                    }
                }
            }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnPostNow.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.etCaption.isEnabled = !isLoading
        binding.btnUploadImage.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}