package com.example.myapplication.view

import android.content.Context
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentLandlordPropertyEditFormBinding
import com.example.myapplication.model.Property
import com.example.myapplication.viewModel.LandlordViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class LandlordPropertyEditFormFragment : Fragment() {

    private var _binding: FragmentLandlordPropertyEditFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LandlordViewModel by viewModels()
    private var currentProperty: Property? = null
    private var selectedImageUri: Uri? = null

    ///Launcher for taking a picture (returns a thumbnail Bitmap)
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            binding.ivNewImagePreview.setImageBitmap(bitmap)
            selectedImageUri = getImageUri(requireContext(), bitmap)
        }
    }

    ///Launcher for picking an image from the gallery (returns a Uri)
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            binding.ivNewImagePreview.setImageURI(uri)
            selectedImageUri = uri
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
        _binding = FragmentLandlordPropertyEditFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ///Retrieve the property passed from the previous fragment
        @Suppress("DEPRECATION")
        currentProperty = arguments?.getParcelable("property")

        setupSpinner()

        ///Pre-fill fields if property exists
        currentProperty?.let { populateFields(it) }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnEditProperty.setOnClickListener {
            if (validateForm()) {
                updateProperty()
            }
        }

        binding.btnChangeImage.setOnClickListener {
            showImageSourceDialog()
        }

        observeViewModel()
    }

    private fun populateFields(property: Property) {
        binding.etPropertyTitle.setText(property.title)
        binding.etPropertyDescription.setText(property.description)
        binding.etPropertyDistrict.setText(property.location)
        binding.etPropertyPrice.setText(property.rentAmount.toInt().toString())
        binding.etContactNumber.setText(property.contactNumber)

        // Set Spinner Selection
        val typeAdapter = binding.spinnerPropertyType.adapter as ArrayAdapter<String>
        val typePosition = typeAdapter.getPosition(property.propertyType)
        if (typePosition >= 0) {
            binding.spinnerPropertyType.setSelection(typePosition)
        }

        val statusAdapter = binding.spinnerAvailability.adapter as ArrayAdapter<String>
        val statusPosition = statusAdapter.getPosition(property.status)
        if (statusPosition >= 0) {
            binding.spinnerAvailability.setSelection(statusPosition)
        }

        ///Load existing image
        val imageUrl = property.imageUrls?.firstOrNull()
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_property_image2)
                .into(binding.ivNewImagePreview)
        }
    }

    private fun updateProperty() {
        // Ensure user is logged in
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            Snackbar.make(binding.root, "Error: You must be logged in to edit a property.", Snackbar.LENGTH_LONG).show()
            return
        }

        val property = currentProperty ?: return

        val title = binding.etPropertyTitle.text.toString().trim()
        val description = binding.etPropertyDescription.text.toString().trim()
        val district = binding.etPropertyDistrict.text.toString().trim()
        val priceStr = binding.etPropertyPrice.text.toString().trim()
        val contact = binding.etContactNumber.text.toString().trim()
        val type = binding.spinnerPropertyType.selectedItem.toString()
        val status = binding.spinnerAvailability.selectedItem.toString()
        val rentAmount = priceStr.toDoubleOrNull() ?: 0.0

        ///Check if anything changed
        val hasChanges = title != property.title ||
                description != property.description ||
                district != property.location ||
                rentAmount != property.rentAmount ||
                contact != property.contactNumber ||
                type != property.propertyType ||
                status != property.status ||
                selectedImageUri != null

        if (!hasChanges) {
            Toast.makeText(requireContext(), "You must edit at least one field to update.", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri != null) {
            // Upload new image first using ImgBB (requires File)
            val imageFile = uriToFile(requireContext(), selectedImageUri!!)
            if (imageFile != null) {
                viewModel.uploadImage(imageFile) { downloadUrl ->
                    if (downloadUrl != null) {
                        // Success: Use new ImgBB URL
                        val updatedUrlList = listOf(downloadUrl)
                        val updatedProperty = property.copy(
                            title = title,
                            description = description,
                            location = district,
                            rentAmount = rentAmount,
                            propertyType = type,
                            status = status,
                            contactNumber = contact,
                            imageUrls = updatedUrlList
                        )
                        viewModel.saveProperty(updatedProperty, isNew = false)
                    } else {
                        // Failed: Do NOT save to avoid losing old image or saving partial data.
                        Log.e("LandlordEdit", "ImgBB upload failed, aborting update.")
                        // Error event is already set by ViewModel, so UI will show toast.
                    }
                }
            } else {
                Toast.makeText(context, "Error processing image file.", Toast.LENGTH_SHORT).show()
            }
        } else {
            ///Update without changing image
            val updatedProperty = property.copy(
                title = title,
                description = description,
                location = district,
                rentAmount = rentAmount,
                propertyType = type,
                status = status,
                contactNumber = contact
            )
            viewModel.saveProperty(updatedProperty, isNew = false)
        }
    }

    // Helper to convert Uri to File for ImgBB
    private fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = "temp_edit_upload_${System.currentTimeMillis()}.jpg"
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

    private fun observeViewModel() {
        viewModel.isProcessing.observe(viewLifecycleOwner) { isLoading ->
            binding.btnEditProperty.isEnabled = !isLoading
            binding.btnEditProperty.text = if (isLoading) "Updating..." else "Edit Property"
        }

        // Observe current user to set name and profile image
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvUserName.text = user.firstName

                // Load Profile Image
                val profileUrl = user.profileImageUrl
                if (!profileUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(profileUrl)
                        .placeholder(R.drawable.ic_property_profile_image)
                        .error(R.drawable.ic_property_profile_image)
                        .into(binding.ivProfileImage)
                } else {
                    // Reset to default if null
                    binding.ivProfileImage.setImageResource(R.drawable.ic_property_profile_image)
                }
            }
        }

        viewModel.operationSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                viewModel.clearSuccess()
                // Navigate to Success Screen
                findNavController().navigate(R.id.action_LandlordPropertyEditFormFragment_to_LandlordPropertyEditSuccessFragment)
            }
        }

        viewModel.errorEvent.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun validateForm(): Boolean {
        // Only validate the contact number format
        val contact = binding.etContactNumber.text.toString().trim()

        if (contact.length != 10) {
            Toast.makeText(requireContext(), "Contact number must be exactly 10 digits", Toast.LENGTH_SHORT).show()
            return false
        }


        return true
    }

    private fun setupSpinner() {
        val propertyTypes = listOf("Apartment", "House", "Villa", "Annex")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, propertyTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPropertyType.adapter = adapter

        val availability = listOf("Available", "Rented", "Maintenance")
        val availabilityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, availability)
        availabilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerAvailability.adapter = availabilityAdapter
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Camera", "Gallery")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Image Source")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> takePictureLauncher.launch(null) // Camera
                    1 -> pickImageLauncher.launch("image/*") // Gallery
                }
            }
            .show()
    }

    ///Helper to get Uri from Bitmap
    private fun getImageUri(inContext: android.content.Context, inImage: Bitmap): Uri {
        val file = File(inContext.cacheDir, "temp_prop_edit_" + System.currentTimeMillis() + ".jpg")
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