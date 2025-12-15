package com.example.myapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentLandlordPropertyDeleteConfirmBinding
import com.example.myapplication.model.Property
import com.example.myapplication.viewModel.LandlordViewModel

class LandlordPropertyDeleteConfirmFragment : Fragment() {

    private var _binding: FragmentLandlordPropertyDeleteConfirmBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LandlordViewModel by viewModels()
    private var currentProperty: Property? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLandlordPropertyDeleteConfirmBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ///Retrieve the property passed from the previous fragment
        @Suppress("DEPRECATION")
        currentProperty = arguments?.getParcelable("property")

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnCancelDelete.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnConfirmDelete.setOnClickListener {
            val propertyId = currentProperty?.propertyId
            if (propertyId != null) {
                viewModel.deleteProperty(propertyId)
            } else {
                Toast.makeText(requireContext(), "Error: Property not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.isProcessing.observe(viewLifecycleOwner) { isLoading ->
            binding.btnConfirmDelete.isEnabled = !isLoading
            binding.btnCancelDelete.isEnabled = !isLoading
            binding.btnConfirmDelete.text = if (isLoading) "Deleting..." else getString(R.string.btn_ok)
        }

        // Observe User
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
                /// Navigate to Success Screen
                findNavController().navigate(R.id.action_LandlordPropertyDeleteConfirmFragment_to_LandlordPropertyDeleteSuccessFragment)
            }
        }

        viewModel.errorEvent.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}