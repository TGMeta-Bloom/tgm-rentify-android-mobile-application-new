package com.example.myapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentLandlordPropertyDetailsBinding
import com.example.myapplication.model.Property
import com.example.myapplication.model.TenantPost
import com.example.myapplication.model.User
import com.example.myapplication.repository.TenantFeedRepository
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class LandlordPropertyDetailsFragment : Fragment() {

    private var _binding: FragmentLandlordPropertyDetailsBinding? = null
    private val binding get() = _binding!!

    // Argument to receive the passed Property object
    private val args: LandlordPropertyDetailsFragmentArgs by navArgs()

    // --- NEW: Repository for Firebase operations ---
    private lateinit var repository: TenantFeedRepository
    private var isSaved = false
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLandlordPropertyDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Initialize Repository
        repository = TenantFeedRepository(requireContext())

        val property = args.property

        // 2. Setup UI & Check Database Status
        setupUI(property)
        checkIfSaved(property.propertyId) // Check if this specific property is already saved

        // 3. Setup Listeners
        setupListeners(property)
    }

    private fun setupUI(property: Property) {
        // Bind data to UI elements
        binding.textDetailTitle.text = property.title
        binding.textDetailDescription.text = property.description
        binding.textDetailLocation.text = property.location

        // Use string resource with placeholder (Format: Rs. 15000.0)
        binding.textDetailPrice.text = getString(R.string.price_format, property.rentAmount.toString())

        binding.textDetailType.text = property.propertyType
        binding.textDetailStatus.text = property.status
        binding.textDetailContact.text = property.contactNumber

        // Load Image
        Glide.with(this)
            .load(property.imageUrls?.firstOrNull())
            .placeholder(R.drawable.ic_property_image2)
            .error(R.drawable.ic_property_image2)
            .into(binding.imagePropertyDetail)
    }

    private fun checkIfSaved(propertyId: String?) {
        if (currentUserId == null || propertyId == null) return

        repository.isPropertySaved(propertyId, currentUserId) { saved ->
            isSaved = saved
            updateSaveIcon()
        }
    }

    private fun setupListeners(property: Property) {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSave.setOnClickListener {
            toggleSaveState(property)
        }
    }

    private fun toggleSaveState(property: Property) {
        if (currentUserId == null) {
            Snackbar.make(binding.root, "Please login to save properties", Snackbar.LENGTH_SHORT).show()
            return
        }

        val propertyId = property.propertyId
        if (propertyId.isNullOrEmpty()) {
            Snackbar.make(binding.root, "Error: Invalid Property ID", Snackbar.LENGTH_SHORT).show()
            return
        }

        binding.btnSave.isEnabled = false // Prevent double clicks

        if (isSaved) {
            // --- UNSAVE LOGIC ---
            repository.unsaveProperty(currentUserId, propertyId) { success ->
                binding.btnSave.isEnabled = true
                if (success) {
                    isSaved = false
                    updateSaveIcon()
                    Snackbar.make(binding.root, "Removed from Saved Properties", Snackbar.LENGTH_SHORT).show()
                }
            }
        } else {
            // --- SAVE LOGIC ---
            // We map 'Property' -> 'TenantPost' format for the repository
            // Note: We map 'title' to 'caption' because that's what the generic repo uses
            val tempPost = TenantPost(
                id = propertyId,
                caption = property.title ?: "Property",
                postImageUrl = property.imageUrls?.firstOrNull() ?: ""
            )

            val user = User(userId = currentUserId, firstName = "", lastName = "")

            repository.saveProperty(user, tempPost) { success ->
                binding.btnSave.isEnabled = true
                if (success) {
                    isSaved = true
                    updateSaveIcon()
                    Snackbar.make(binding.root, "Property Saved Successfully!", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateSaveIcon() {
        if (isSaved) {
            // Active (Blue Tint)
            binding.btnSave.setColorFilter(ContextCompat.getColor(requireContext(), R.color.app_blue))
        } else {
            // Inactive (Dark Gray/Black Tint)
            binding.btnSave.setColorFilter(ContextCompat.getColor(requireContext(), R.color.black))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}