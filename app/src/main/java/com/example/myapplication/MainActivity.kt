package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.myapplication.databinding.ActivityMainBinding
import kotlin.text.clear

import com.example.myapplication.utils.SharedPreferencesHelper
// Explicit import to ensure BuildConfig is resolved if referenced implicitly
import com.example.myapplication.BuildConfig

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefsHelper: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Prefs
        prefsHelper = SharedPreferencesHelper(this)

        // Apply Theme based on Preferences
        val isDarkMode = prefsHelper.isDarkMode()
        val mode = if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
//        val userRole = prefsHelper.getUserRole()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController


        /////////////////////


        // Dynamic Start Destination based on Role
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val userRole = sharedPref.getString("user_role", "tenant")

        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

        if (userRole.equals("Landlord", ignoreCase = true)) {
            // If Landlord, start at Landlord Dashboard (My Properties)
            navGraph.setStartDestination(R.id.LandlordDashboardFragment)

            // Switch Bottom Navigation Menu for Landlord
            // (Home -> LandlordDashboard, Dashboard -> Feed)
            binding.bottomNavigation.menu.clear()
            binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu_landlord)
        } else {
            // If Tenant, start at Feed
            navGraph.setStartDestination(R.id.feedFragment)
            // Default menu is already inflated from XML, but just in case
            binding.bottomNavigation.menu.clear()
            binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu)
        }
        navController.graph = navGraph


        ///////////////////////

//
//        // Use NavigationUI to connect both the bottom and side navigation
//
//        // Dynamic Start Destination based on Role
//        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
//
//        if (userRole == "Landlord") {
//            navGraph.setStartDestination(R.id.landlordAddPropertyFragment)
//        } else {
//            navGraph.setStartDestination(R.id.feedFragment)
//        }
//        navController.graph = navGraph
//
//        // Listen for navigation changes to swap the Sidebar Header
//        navController.addOnDestinationChangedListener { _, destination, _ ->
//            if (destination.id == R.id.profileFragment) {
//                // If on Profile Screen, Show Custom Profile Header
//                showProfileHeader()
//            } else {
//                // If on Tenant Feed (or others), Show Friend's Header (Default)
//                showTenantHeader()
//            }
//        }
//
        // Ensure correct header is loaded immediately
        if (navController.currentDestination?.id == R.id.profileFragment) {
             showProfileHeader()
        } else {
             showTenantHeader()
        }

        // Setup Bottom Nav with Controller first for default behavior/tinting
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController)
        NavigationUI.setupWithNavController(binding.navView, navController)
        
        // OVERRIDE Bottom Navigation Listener to handle Role-Based Home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.feedFragment) {
                // Check Role
                val currentRole = prefsHelper.getUserRole()
                if (currentRole == "Landlord") {
                    // Navigate to Landlord Home
                    // Clear back stack to avoid loops if needed, or just navigate
                    navController.navigate(R.id.landlordAddPropertyFragment)
                    return@setOnItemSelectedListener true
                }
            }
            
            // For all other items (or Tenant role), use standard navigation
            NavigationUI.onNavDestinationSelected(item, navController)
            return@setOnItemSelectedListener true
        }
    }

    private fun showProfileHeader() {
        val currentHeader = if (binding.navView.headerCount > 0) binding.navView.getHeaderView(0) else null
        
        if (currentHeader == null || currentHeader.id != R.id.nav_view_custom) {
            
            while (binding.navView.headerCount > 0) {
                 binding.navView.removeHeaderView(binding.navView.getHeaderView(0))
            }

            // Inflate Custom Header (without toggle logic)
            val newHeader = binding.navView.inflateHeaderView(R.layout.nav_drawer_custom_layout)
            setupUserName(newHeader)
        }
    }

    private fun showTenantHeader() {
        val currentHeader = if (binding.navView.headerCount > 0) binding.navView.getHeaderView(0) else null
        
        if (currentHeader != null && currentHeader.id == R.id.nav_view_custom) {
            binding.navView.removeHeaderView(currentHeader)
            val newHeader = binding.navView.inflateHeaderView(R.layout.nav_tenant_header_main)
            setupUserName(newHeader)
        } else if (currentHeader == null) {
             val newHeader = binding.navView.inflateHeaderView(R.layout.nav_tenant_header_main)
             setupUserName(newHeader)
        }
    }

    private fun setupUserName(headerView: View) {
        var nameText = headerView.findViewById<TextView>(R.id.tv_nav_user_name)
        if (nameText == null) {
            nameText = headerView.findViewById<TextView>(R.id.drawer_user_name)
        }
        
        if (nameText != null) {
            val userName = prefsHelper.getUserName()
            if (!userName.isNullOrEmpty()) {
                nameText.text = userName
            } else {
                nameText.text = "Welcome"
            }
        }
    }
}