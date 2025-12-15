@file:Suppress("SpellCheckingInspection")

package com.example.myapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.NavGraphDirections
import com.example.myapplication.databinding.FragmentLandlordDashboardBinding
import com.example.myapplication.model.Property
import com.example.myapplication.model.PropertyType
import com.example.myapplication.view.adapter.LandlordGridAdapter
import com.example.myapplication.view.adapter.PropertyTypeAdapter
import com.example.myapplication.viewModel.LandlordViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class LandlordDashboardFragment : Fragment() {

    private var _binding: FragmentLandlordDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LandlordViewModel by viewModels()

    private lateinit var propertyTypeAdapter: PropertyTypeAdapter
    private lateinit var landlordGridAdapter: LandlordGridAdapter

    private val propertyTypes = mutableListOf(
        PropertyType("All", true),
        PropertyType("Apartment"),
        PropertyType("House"),
        PropertyType("Villa"),
        PropertyType("Annex")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLandlordDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {

            Snackbar.make(binding.root, "No user logged in.", Snackbar.LENGTH_LONG).show()
        }

        setupAdapters()
        setupListeners()
        observeViewModel()
    }

    private fun setupAdapters() {
        /// Property Type Filters
        propertyTypeAdapter = PropertyTypeAdapter { selectedType ->
            handleTypeFilter(selectedType)
        }
        binding.recyclerViewFilters.apply {
            adapter = propertyTypeAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            propertyTypeAdapter.submitList(propertyTypes.toList())
        }

        /// Landlord Properties Grid
        landlordGridAdapter = LandlordGridAdapter { property ->
            ///Navigation Trigger
            navigateToPropertyDetails(property)
        }
        binding.recyclerViewLandlordPropertiesGrid.apply {
            adapter = landlordGridAdapter
        }
    }

    private fun setupListeners() {
    }

    private fun observeViewModel() {
        ///  Observe 'filteredLandlordProperties' to support filtering
        viewModel.filteredLandlordProperties.observe(viewLifecycleOwner) { properties ->
            landlordGridAdapter.submitList(properties)
        }

    }



    private fun handleTypeFilter(selectedType: PropertyType) {
        val updatedList = propertyTypes.map {
            it.copy(isSelected = it.name == selectedType.name)
        }
        propertyTypes.clear()
        propertyTypes.addAll(updatedList)
        propertyTypeAdapter.submitList(propertyTypes.toList())

        /// Call ViewModel to filter the list
        viewModel.setFilter(selectedType.name)
    }

    private fun navigateToPropertyDetails(property: Property?) {
        if (property == null) return

        try {
            /// Using GLOBAL ACTION via NavGraphDirections to fix "Destination not found" issues
            val action = NavGraphDirections.actionGlobalLandlordDetails(property)
            findNavController().navigate(action)
        } catch (e: Exception) {
            e.printStackTrace()
            Snackbar.make(binding.root, "Nav Error: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
