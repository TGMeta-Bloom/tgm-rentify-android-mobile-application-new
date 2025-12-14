package com.example.myapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentTenantFeedBinding
import com.example.myapplication.view.adapter.TenantFeedAdapter
import com.example.myapplication.viewmodel.TenantFeedViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TenantFeedFragment : Fragment() {

    private var _binding: FragmentTenantFeedBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TenantFeedViewModel by viewModels()
    private lateinit var feedAdapter: TenantFeedAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTenantFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Setup Adapter with BOTH Click Listeners
        feedAdapter = TenantFeedAdapter(
            onHelpfulClicked = { postId, newCount ->
                // Called when "Helpful" button is clicked -> Updates Database
                viewModel.updateHelpfulCount(postId, newCount)
            },
            onHidePost = { postId ->
                // Called when "Hide Post" menu item is clicked -> Removes from screen
                viewModel.hidePost(postId)
            }
        )

        binding.rvFeed.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFeed.adapter = feedAdapter

        // 2. Observe Data
        viewModel.feedPosts.observe(viewLifecycleOwner) { posts ->
            feedAdapter.submitList(posts)
        }
        viewModel.loadFeed()

        // 3. API: Fetch User Name for Greeting
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name") ?: "User"
                        binding.tvGreeting.text = "Hi $name!"
                    }
                }
        }

        // 4. Navigation Handlers
        binding.btnAddPost.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_addPostFragment)
        }

        binding.btnMenu.setOnClickListener {
            activity?.findViewById<DrawerLayout>(R.id.drawer_layout)?.openDrawer(GravityCompat.START)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}