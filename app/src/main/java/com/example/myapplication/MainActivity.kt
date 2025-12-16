package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.utils.SharedPreferencesHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefsHelper: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        prefsHelper = SharedPreferencesHelper(this)
        val isDarkMode = prefsHelper.isDarkMode()
        val mode = if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val userRole = prefsHelper.getUserRole() ?: "Tenant" // Default to Tenant if null

        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

        if (userRole.equals("Landlord", ignoreCase = true)) {
            // Landlord Flow
            navGraph.setStartDestination(R.id.LandlordDashboardFragment)

            // Swap Bottom Menu for Landlord
            binding.bottomNavigation.menu.clear()
            binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu_landlord)
        } else {
            // Tenant Flow
            navGraph.setStartDestination(R.id.feedFragment)

            // Standard Menu
            binding.bottomNavigation.menu.clear()
            binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu)
        }
        navController.graph = navGraph

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.profileFragment) {
                showProfileHeader()
            } else {
                showTenantHeader()
            }
        }

        NavigationUI.setupWithNavController(binding.bottomNavigation, navController)
        NavigationUI.setupWithNavController(binding.navView, navController)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            // Handle Home Button Click based on Role
            if (item.itemId == R.id.feedFragment) {
                if (userRole.equals("Landlord", ignoreCase = true)) {
                    // FIX: Landlords go to Dashboard, not AddProperty
                    navController.navigate(R.id.LandlordDashboardFragment)
                    return@setOnItemSelectedListener true
                }
            }

            // Default behavior for everything else
            NavigationUI.onNavDestinationSelected(item, navController)
            return@setOnItemSelectedListener true
        }
    }

    // --- NEW: Public function to Open Drawer from Fragments ---
    fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun showProfileHeader() {
        // Only swap if we aren't already showing the custom header
        val headerView = binding.navView.getHeaderView(0)

        binding.navView.removeHeaderView(headerView)
        val newHeader = binding.navView.inflateHeaderView(R.layout.nav_drawer_custom_layout)
        setupUserName(newHeader)
    }

    private fun showTenantHeader() {
        val headerView = binding.navView.getHeaderView(0)
        // Ideally check if headerView is ALREADY the tenant header to avoid flickering

        binding.navView.removeHeaderView(headerView)
        val newHeader = binding.navView.inflateHeaderView(R.layout.nav_tenant_header_main)
        setupUserName(newHeader)
    }

    private fun setupUserName(headerView: View) {
        // Try to find the TextView with either ID (depending on which layout is loaded)
        var nameText = headerView.findViewById<TextView>(R.id.tv_nav_user_name)
        if (nameText == null) {
            nameText = headerView.findViewById<TextView>(R.id.drawer_user_name)
        }

        if (nameText != null) {
            val userName = prefsHelper.getUserName()
            if (!userName.isNullOrEmpty()) {
                nameText.text = "Hi, $userName"
            } else {
                nameText.text = "Welcome"
            }
        }
    }
}