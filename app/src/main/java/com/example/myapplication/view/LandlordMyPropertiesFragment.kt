package com.example.myapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentLandlordMyPropertiesBinding
import com.example.myapplication.model.Property
import com.example.myapplication.view.adapter.LandlordPropertiesAdapter
import com.example.myapplication.viewModel.LandlordViewModel

class LandlordMyPropertiesFragment : Fragment() {


    private var _binding: FragmentLandlordMyPropertiesBinding? = null
    private val binding get() = _binding!!


    private val viewModel: LandlordViewModel by viewModels()
    private lateinit var adapter: LandlordPropertiesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLandlordMyPropertiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        ///Initialize Adapter with empty list
        adapter = LandlordPropertiesAdapter(
            emptyList(),
            onEditClick = { property ->
                ///Navigate to Edit Fragment with Property data
                val bundle = bundleOf("property" to property)
                findNavController().navigate(R.id.action_LandlordPropertiesFragment_to_LandlordPropertyEditFormFragment, bundle)
            },
            onDeleteClick = { property ->
                ///Navigate to Delete Confirm Fragment
                val bundle = bundleOf("property" to property)
                findNavController().navigate(R.id.action_LandlordPropertiesFragment_to_LandlordPropertyDeleteConfirmFragment, bundle)
            }
        )

        binding.rvProperties.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProperties.adapter = adapter
    }

    private fun observeViewModel() {
        ///Observe Real Data from Firestore
        viewModel.myProperties.observe(viewLifecycleOwner) { properties ->
            if (properties.isNullOrEmpty()) {
                ///Show empty state if needed, or just clear list
                adapter.updateProperties(emptyList())
            } else {
                adapter.updateProperties(properties)
            }
        }

        /// Observe Current User Data
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                // Set the user's first name to the TextView
                binding.tvUserName.text = user.firstName

                ///Load Profile Image
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

        /// Observe Loading State
        viewModel.isProcessing.observe(viewLifecycleOwner) { isLoading ->
        }

        /// Observe Errors
        viewModel.errorEvent.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}