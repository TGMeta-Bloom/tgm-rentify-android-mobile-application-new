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
import com.example.myapplication.databinding.FragmentLandlordPropertyDeleteSuccessBinding
import com.example.myapplication.viewModel.LandlordViewModel

class LandlordPropertyDeleteSuccessFragment : Fragment() {

    private var _binding: FragmentLandlordPropertyDeleteSuccessBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LandlordViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLandlordPropertyDeleteSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_LandlordPropertyDeleteSuccessFragment_to_LandlordAddPropertyFragment)
        }

        ///Observe User
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}