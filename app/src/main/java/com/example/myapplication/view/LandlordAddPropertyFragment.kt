package com.example.myapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentLandlordAddPropertyBinding
import com.example.myapplication.viewModel.LandlordViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class LandlordAddPropertyFragment : Fragment() {

    private var _binding: FragmentLandlordAddPropertyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LandlordViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLandlordAddPropertyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.menu?.findItem(R.id.profileFragment)?.isChecked = true
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.fabAddProperty.setOnClickListener {
            findNavController().navigate(R.id.action_LandlordAddPropertyFragment_to_LandlordPostPropertyFormFragment)
        }
        binding.fabViewProperties.setOnClickListener {
            /// Navigate to Landlord Properties Fragment (My Properties)
            findNavController().navigate(R.id.action_LandlordAddPropertyFragment_to_LandlordPropertiesFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                ///The ID in this specific layout is text_landlord_name
                binding.textLandlordName.text = user.firstName

                /// Load Profile Image
                val profileUrl = user.profileImageUrl
                if (!profileUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(profileUrl)
                        .placeholder(R.drawable.ic_property_profile_image)
                        .error(R.drawable.ic_property_profile_image)
                        .into(binding.imageProfile) /// Use the ID from the image_profile(XML file)
                } else {
                    binding.imageProfile.setImageResource(R.drawable.ic_property_profile_image)
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
