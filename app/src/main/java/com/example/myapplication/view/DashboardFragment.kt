package com.example.myapplication.view

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.myapplication.R

class DashboardFragment : Fragment(R.layout.fragment_router_container) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check against the ID in fragment_router_container.xml
        if (childFragmentManager.findFragmentById(R.id.role_specific_container) == null) {

            // 1. Get the User Role
            val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            val userRole = sharedPref.getString("user_role", "tenant")

            // 2. Pick the Screen based on logic
            val targetFragment: Fragment = if (userRole == "tenant") {
                // Tenant wants to Browse Properties (The code you pasted earlier)
                LandlordDashboardFragment()
            } else {
                // Landlord wants to see the Notice Board (Reusing your existing Feed)
                TenantFeedFragment()
            }

            // 3. Show it
            childFragmentManager.beginTransaction()
                .replace(R.id.role_specific_container, targetFragment)
                .commit()
        }
    }
}