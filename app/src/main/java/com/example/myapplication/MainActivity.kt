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
import com.example.myapplication.utils.SharedPreferencesHelper
// Explicit import to ensure BuildConfig is resolved
import com.example.myapplication.BuildConfig

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefsHelper: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Prefs
        prefsHelper = SharedPreferencesHelper(this)

        // FIX: FORCE DARK MODE ALWAYS (Ignore internal setting)
        // This ensures the app always looks like "Image 2" (Dark Theme)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Dynamic Start Destination based on Role
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val userRole = sharedPref.getString("user_role", "tenant")

        if (savedInstanceState == null) {
            val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

            if (userRole.equals("Landlord", ignoreCase = true)) {
                navGraph.setStartDestination(R.id.LandlordDashboardFragment)
            } else {
                navGraph.setStartDestination(R.id.feedFragment)
            }
            navController.graph = navGraph
        }

        // Setup Menus based on Role (UI only, doesn't affect navigation state)
        if (userRole.equals("Landlord", ignoreCase = true)) {
            binding.bottomNavigation.menu.clear()
            binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu_landlord)
        } else {
            binding.bottomNavigation.menu.clear()
            binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu)
        }

        // Listen for navigation changes to swap the Sidebar Header
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.profileFragment) {
                // If on Profile Screen, Show Custom Profile Header
                showProfileHeader()
            } else {
                // If on Tenant Feed (or others), Show Friend's Header (Default)
                showTenantHeader()
            }
        }

        // Ensure correct header is loaded immediately
        if (navController.currentDestination?.id == R.id.profileFragment) {
             showProfileHeader()
        } else {
             showTenantHeader()
        }

        // Setup Bottom Nav with Controller first for default behavior/tinting
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController)
        NavigationUI.setupWithNavController(binding.navView, navController)
        
        // Standard NavigationUI behavior
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            NavigationUI.onNavDestinationSelected(item, navController)
            return@setOnItemSelectedListener true
        }
    }

    private fun showProfileHeader() {
        // Clear standard menu items because Profile uses a custom layout inside the header
        binding.navView.menu.clear()

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
        
        // Check if we need to swap the header OR if the menu is missing
        if (currentHeader == null || currentHeader.id == R.id.nav_view_custom) {
            // We are switching FROM Profile TO Tenant (or initial load)
            if (currentHeader != null) {
                binding.navView.removeHeaderView(currentHeader)
            }
            val newHeader = binding.navView.inflateHeaderView(R.layout.nav_tenant_header_main)
            setupUserName(newHeader)
            
            // RESTORE Standard Menu
            binding.navView.menu.clear()
            binding.navView.inflateMenu(R.menu.nav_drawer_menu_tenant)
            
        } else {
             // Header is already correct (Tenant Header), but CHECK THE MENU
             if (binding.navView.menu.size() == 0) {
                 binding.navView.inflateMenu(R.menu.nav_drawer_menu_tenant)
             }
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