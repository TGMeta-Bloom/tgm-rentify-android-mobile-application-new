package com.example.myapplication.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R

class ProfileFragment : Fragment() {

    private lateinit var btnManageProperties: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize only the Manage Properties Button
        btnManageProperties = view.findViewById(R.id.btn_manage_properties)

        // Force visibility to VISIBLE for testing/placeholder purposes
        // In the final app, this would check for Landlord role
        btnManageProperties.visibility = View.VISIBLE

        btnManageProperties.setOnClickListener {
            try {
                // Navigate to LandlordAddPropertyFragment
                findNavController().navigate(R.id.action_profileFragment_to_LandlordAddPropertyFragment)
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Navigation failed: ${e.message}")
                Toast.makeText(requireContext(), "Navigation Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}