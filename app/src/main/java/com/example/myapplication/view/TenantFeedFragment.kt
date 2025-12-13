package com.example.myapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
// import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
// import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
// import com.example.myapplication.databinding.FragmentTenantFeedBinding // Assuming layout exists
// import com.example.myapplication.view.adapter.FeedAdapter
// import com.example.myapplication.viewModel.FeedViewModel

class TenantFeedFragment : Fragment(R.layout.fragment_tenant_feed) {

    // --- Placeholder for Friend's Work (Binding, ViewModel, Adapter) ---
    // private var _binding: FragmentTenantFeedBinding? = null
    // private val binding get() = _binding!!
    // private val viewModel: FeedViewModel by viewModels()
    // private lateinit var feedAdapter: FeedAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setupRecyclerView()
        // observeViewModel()
        // viewModel.loadFeed()

        // Placeholder for Button Listeners (If layout is missing, findViewByID won't work easily without binding)
        // If you need your Menu/AddPost logic, ensure the buttons exist in the layout loaded by R.layout.fragment_router_container
        // or the specific layout you are using.
        
        /*
        binding.btnAddPost.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_addPostFragment)
        }

        binding.btnMenu.setOnClickListener {
            val drawerLayout = activity?.findViewById<DrawerLayout>(R.id.drawer_layout)
            drawerLayout?.openDrawer(GravityCompat.START)
        }
        */
    }

    /*
    private fun setupRecyclerView() {
        feedAdapter = FeedAdapter()
        binding.rvFeed.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = feedAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.feedPosts.observe(viewLifecycleOwner) { posts ->
            posts?.let {
                feedAdapter.submitList(it)
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    */
}
