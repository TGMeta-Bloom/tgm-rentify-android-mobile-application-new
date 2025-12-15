package com.example.myapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentLandlordPropertyDetailsBinding
import com.example.myapplication.model.Property
import com.google.android.material.snackbar.Snackbar

class LandlordPropertyDetailsFragment : Fragment() {

    private var _binding: FragmentLandlordPropertyDetailsBinding? = null
    private val binding get() = _binding!!

    ///Argument to receive the passed Property object
    private val args: LandlordPropertyDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLandlordPropertyDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val property = args.property
        setupUI(property)
        setupListeners()
    }

    private fun setupUI(property: Property) {
        /// Bind data to UI elements
        binding.textDetailTitle.text = property.title
        binding.textDetailDescription.text = property.description
        binding.textDetailLocation.text = property.location
        ///Use string resource with placeholder
        binding.textDetailPrice.text = getString(R.string.price_format, property.rentAmount.toString())
        binding.textDetailType.text = property.propertyType
        binding.textDetailStatus.text = property.status
        binding.textDetailContact.text = property.contactNumber

        /// Load Image
        Glide.with(this)
            .load(property.imageUrls?.firstOrNull())
            .placeholder(R.drawable.ic_property_image2) // Default
            .error(R.drawable.ic_property_image2)
            .into(binding.imagePropertyDetail)
    }

    private fun setupListeners() {
        ///Ensure the ID matches your XML (btn_back)
        binding.btnBack.setOnClickListener {
            ///navigateUp() goes back to the previous screen in the stack
            findNavController().navigateUp()
        }

        binding.btnSave.setOnClickListener {
            Snackbar.make(binding.root, "Property saved!", Snackbar.LENGTH_SHORT).show()

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}