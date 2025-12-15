package com.example.myapplication

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.myapplication.databinding.ActivityMainBinding
import kotlin.text.clear

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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


        // Use NavigationUI to connect both the bottom and side navigation
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController)
        NavigationUI.setupWithNavController(binding.navView, navController)
    }
}
