package com.example.myapplication.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.utils.SharedPreferencesHelper

class FeedFragment : Fragment(R.layout.fragment_router_container) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (childFragmentManager.findFragmentById(R.id.role_specific_container) == null) {

            // 1. Retrieve Role using the Helper (Guarantees same file as Registration)
            val prefsHelper = SharedPreferencesHelper(requireContext())
            val userRole = prefsHelper.getUserRole() ?: "Tenant"


            // Toast.makeText(context, "Welcome $userRole", Toast.LENGTH_SHORT).show()

            // 2. Determine which fragment to show
            val targetFragment: Fragment = if (userRole == "Landlord") {
                LandlordDashboardFragment()
            } else {
                TenantFeedFragment()
            }

            // 3. Embed the child fragment
            childFragmentManager.beginTransaction()
                .replace(R.id.role_specific_container, targetFragment)
                .commit()
        }
    }
}
