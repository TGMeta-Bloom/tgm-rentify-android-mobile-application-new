package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.utils.SharedPreferencesHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefsHelper: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize Helpers
        prefsHelper = SharedPreferencesHelper(this)

        // 2. Set Theme (Dark/Light)
        val isDarkMode = prefsHelper.isDarkMode()
        val mode = if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)

        // 3. Inflate Layout
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 4. Force System Bars to Dark Blue and Text/Icons to WHITE (From Code 3)
        val navColor = ContextCompat.getColor(this, R.color.app_navbar_blue)
        window.navigationBarColor = navColor
        window.statusBarColor = navColor
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = 0

        // 5. Setup Navigation Controller
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Get Role
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val userRole = sharedPref.getString("user_role", "tenant")

        // 6. Dynamic Start Destination (Only on fresh start to prevent resets)
        if (savedInstanceState == null) {
            val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

            if (userRole.equals("Landlord", ignoreCase = true)) {
                navGraph.setStartDestination(R.id.LandlordDashboardFragment)
            } else {
                navGraph.setStartDestination(R.id.feedFragment)
            }
            navController.graph = navGraph
        }

        // 7. Setup Menus based on Role
        if (userRole.equals("Landlord", ignoreCase = true)) {
            binding.bottomNavigation.menu.clear()
            binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu_landlord)
        } else {
            binding.bottomNavigation.menu.clear()
            binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu)
        }

        // 8. Listen for navigation changes to swap the Sidebar Header
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.profileFragment) {
                showProfileHeader()
            } else {
                showTenantHeader()
            }
        }

        // Ensure correct header is loaded immediately
        if (navController.currentDestination?.id == R.id.profileFragment) {
            showProfileHeader()
        } else {
            showTenantHeader()
        }

        // 9. Setup Navigation UI
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController)
        NavigationUI.setupWithNavController(binding.navView, navController)

        // Standard Bottom Nav Click Listener (Removed buggy override)
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            NavigationUI.onNavDestinationSelected(item, navController)
            return@setOnItemSelectedListener true
        }
    }

    // --- CRITICAL: Allows Fragments to Open the Side Menu (From Code 2) ---
    fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    // --- Header & Menu Management Functions (From Code 3 + Code 2) ---

    private fun showProfileHeader() {
        // Clear standard menu items because Profile uses a custom layout inside the header
        binding.navView.menu.clear()

        val currentHeader = if (binding.navView.headerCount > 0) binding.navView.getHeaderView(0) else null

        if (currentHeader == null || currentHeader.id != R.id.nav_view_custom) {

            while (binding.navView.headerCount > 0) {
                binding.navView.removeHeaderView(binding.navView.getHeaderView(0))
            }

            // Inflate Custom Header
            val newHeader = binding.navView.inflateHeaderView(R.layout.nav_drawer_custom_layout)
            setupHeader(newHeader)
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
            setupHeader(newHeader)

            // RESTORE Standard Menu (Critical Fix from Code 3)
            binding.navView.menu.clear()
            binding.navView.inflateMenu(R.menu.nav_drawer_menu_tenant)

        } else {
            // Header is already correct, but ensure MENU is visible
            if (binding.navView.menu.size() == 0) {
                binding.navView.inflateMenu(R.menu.nav_drawer_menu_tenant)
            }
            // Even if header exists, refresh data (name/pic might have changed)
            setupHeader(currentHeader)
        }
    }

    // Combined Setup Function (Merges Logic from Code 2 & 3)
    private fun setupHeader(headerView: View) {
        // 1. Setup Name
        var nameText = headerView.findViewById<TextView>(R.id.tv_nav_user_name)
        if (nameText == null) {
            nameText = headerView.findViewById(R.id.drawer_user_name)
        }

        if (nameText != null) {
            val userName = prefsHelper.getUserName()
            nameText.text = if (!userName.isNullOrEmpty()) "Hi, $userName" else "Welcome"
        }

        // 2. Setup Profile Picture (From Code 2)
        // Checks for multiple common IDs to be safe
        var profileImage = headerView.findViewById<ImageView>(R.id.iv_profile_avatar)
        if (profileImage == null) {
            profileImage = headerView.findViewById(R.id.imageView)
        }

        if (profileImage != null) {
            val imageUrl = prefsHelper.getProfileImageUrl()
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_tenant_profile_placeholder) // Use your actual placeholder drawable
                    .error(R.drawable.ic_tenant_profile_placeholder)
                    .circleCrop()
                    .into(profileImage)
            }
        }
    }
}