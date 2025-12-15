package com.example.myapplication.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentLandlordPostPropertyFormBinding
import com.example.myapplication.model.Property
import com.example.myapplication.viewModel.LandlordViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class LandlordPostPropertyFormFragment : Fragment() {


    private var _binding: FragmentLandlordPostPropertyFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LandlordViewModel by viewModels()

    private var selectedImageUri: Uri? = null

    /// Launcher for taking a picture (returns a thumbnail Bitmap)
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            binding.ivPropertyImagePreview.setImageBitmap(bitmap)
            /// Convert Bitmap to Uri for upload
            selectedImageUri = getImageUri(requireContext(), bitmap)
        }
    }

    /// Launcher for picking an image from the gallery (returns a Uri)
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            binding.ivPropertyImagePreview.setImageURI(uri)
            selectedImageUri = uri
        }
    }

    ///Permission Launcher
    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            takePictureLauncher.launch(null)
        } else {
            Toast.makeText(requireContext(), "Camera permission is required to take photos.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ///Prevents the app from switching to Dark Mode when Camera opens
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLandlordPostPropertyFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinner()
        setupListeners()
        observeViewModel()
    }

    private fun setupSpinner() {
        val propertyTypes = listOf("Apartment", "House", "Villa", "Annex")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, propertyTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPropertyType.adapter = adapter

        // Setup Availability Spinner
        val availability = listOf("Available", "Rented", "Maintenance")
        val availabilityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, availability)
        availabilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerAvailability.adapter = availabilityAdapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnPostProperty.setOnClickListener {
            ///validation to check for 10-digit contact number
            if (validateForm()) {
                savePropertyData()
            }
        }

        binding.btnAddImage.setOnClickListener {
            showImageSourceDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.isProcessing.observe(viewLifecycleOwner) { isLoading ->
            binding.btnPostProperty.isEnabled = !isLoading
            binding.btnPostProperty.text = if (isLoading) "Posting..." else "Post Property"
        }

        // Observe current user to set name and profile image
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvUserName.text = user.firstName

                val profileUrl = user.profileImageUrl
                if (!profileUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(profileUrl)
                        .placeholder(R.drawable.ic_property_profile_image)
                        .error(R.drawable.ic_property_profile_image)
                        .into(binding.ivProfileImage)
                } else {
                    binding.ivProfileImage.setImageResource(R.drawable.ic_property_profile_image)
                }
            }
        }

        viewModel.operationSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                viewModel.clearSuccess()
                // Provide feedback to user about where to find the data
                Toast.makeText(context, "Property Posted Successfully!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_LandlordPostPropertyFormFragment_to_LandlordPostPropertySuccessFragment)
            }
        }

        viewModel.errorEvent.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun savePropertyData() {
        val title = binding.etPropertyTitle.text.toString().trim()
        val description = binding.etPropertyDescription.text.toString().trim()
        val district = binding.etPropertyDistrict.text.toString().trim()
        val priceStr = binding.etPropertyPrice.text.toString().trim()
        val contact = binding.etContactNumber.text.toString().trim()
        val type = binding.spinnerPropertyType.selectedItem.toString()
        val status = binding.spinnerAvailability.selectedItem.toString()
        val rentAmount = priceStr.toDoubleOrNull() ?: 0.0

        val auth = FirebaseAuth.getInstance()
        // Ensure strictly that we use the current user
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Snackbar.make(binding.root, "Error: You must be logged in to post a property.", Snackbar.LENGTH_LONG).show()
            return
        }

        val currentUserId = currentUser.uid
        Log.d("LandlordPost", "Posting property for UserId: $currentUserId")

        val propertyId = UUID.randomUUID().toString()

        val propertyBase = Property(
            propertyId = propertyId,
            userId = currentUserId,
            title = title,
            description = description,
            location = district,
            rentAmount = rentAmount,
            propertyType = type,
            status = status, // Use the selected status from the spinner
            contactNumber = contact,
            imageUrls = emptyList() // Default to empty if no image
        )

        if (selectedImageUri != null) {
            // Use local conversion to File so we can use ImgBB upload
            val imageFile = uriToFile(requireContext(), selectedImageUri!!)
            if (imageFile != null) {
                // Call the new ImgBB upload function (taking File)
                viewModel.uploadImage(imageFile) { downloadUrl ->
                    if (downloadUrl != null) {
                        // Success: Save property with the ImgBB URL
                        val propertyWithImage = propertyBase.copy(imageUrls = listOf(downloadUrl))
                        viewModel.saveProperty(propertyWithImage, isNew = true)
                    } else {
                        // Failed: User requested "remove all the image null", implying we should NOT save if image fails?
                        // OR they just meant "don't let it be null in DB".
                        // Assuming we should FAIL gracefully and NOT save the property if upload fails, 
                        // so the user can try again, instead of saving a broken record.
                        // However, viewModel.uploadImage already sets _errorEvent on failure, so UI will show toast.
                        // We do NOTHING here to prevent saving a record with no image.
                        Log.e("LandlordPost", "Image upload failed, aborting property save.")
                    }
                }
            } else {
                Toast.makeText(context, "Error processing image file.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // No image selected. The user said "remove all the image null".
            // If they mean "I want to allow posting without image", then we save with empty list (or null).
            // If they mean "Properties MUST have images", we should block here.
            // Assuming they meant "If I pick an image, it shouldn't be null".
            // But usually property apps allow no-image. I'll save it with null/empty.
            viewModel.saveProperty(propertyBase, isNew = true)
        }
    }

    // Helper to convert Uri to File for ImgBB
    private fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = "temp_upload_${System.currentTimeMillis()}.jpg"
            val tempFile = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun validateForm(): Boolean {
        ///Enforce 10-digit validation for contact number
        val contact = binding.etContactNumber.text.toString().trim()
        val title = binding.etPropertyTitle.text.toString().trim()
        val price = binding.etPropertyPrice.text.toString().trim()

        if (title.isEmpty()) {
            binding.etPropertyTitle.error = "Title is required"
            return false
        }
        if (price.isEmpty()) {
            binding.etPropertyPrice.error = "Price is required"
            return false
        }
        if (contact.length != 10) {
            binding.etContactNumber.error = "Contact must be 10 digits"
            Toast.makeText(requireContext(), "Contact number must be exactly 10 digits", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Camera", "Gallery")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Image Source")
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
                takePictureLauncher.launch(null)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Toast.makeText(requireContext(), "Camera permission is needed to take property photos.", Toast.LENGTH_LONG).show()
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    /// UPDATED: Save to Cache instead of MediaStore to avoid permission issues
    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val file = File(inContext.cacheDir, "temp_prop_img_" + System.currentTimeMillis() + ".jpg")
        file.createNewFile()

        val bos = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 90, bos)
        val bitmapdata = bos.toByteArray()

        val fos = FileOutputStream(file)
        fos.write(bitmapdata)
        fos.flush()
        fos.close()

        return Uri.fromFile(file)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}