package com.example.myapplication.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentTenantFeedBinding
import com.example.myapplication.view.adapter.TenantFeedAdapter
import com.example.myapplication.viewModel.TenantFeedViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TenantFeedFragment : Fragment() {

    private var _binding: FragmentTenantFeedBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TenantFeedViewModel
    private lateinit var adapter: TenantFeedAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTenantFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Initialize ViewModel
        viewModel = ViewModelProvider(this)[TenantFeedViewModel::class.java]

        // 2. Setup RecyclerView
        setupRecyclerView()

        // 3. Load User Profile Header (Name & Pic)
        loadUserProfile()

        // 4. Observe Data from ViewModel
        viewModel.feedPosts.observe(viewLifecycleOwner) { posts ->
            if (posts.isEmpty()) {
                Log.d("TenantFeed", "No posts found in Feed.")
            } else {
                Log.d("TenantFeed", "Loaded ${posts.size} posts.")
            }
            adapter.submitList(posts)
        }

        // 5. Navigation Logic
        binding.btnAddPost.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_addPostFragment)
        }

        // Open Side Drawer
        binding.btnMenu.setOnClickListener {
            // This calls the openDrawer() function inside your MainActivity
            (activity as? MainActivity)?.openDrawer()
        }
    }

    private fun setupRecyclerView() {
        adapter = TenantFeedAdapter(
            onHelpfulClicked = { postId, newCount ->
                viewModel.updateHelpfulCount(postId, newCount)
            },
            onHidePost = { postId ->
                viewModel.hidePost(postId)
            }
        )

        binding.rvFeed.layoutManager = LinearLayoutManager(context)
        binding.rvFeed.adapter = adapter
    }

    private fun loadUserProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            binding.tvGreeting.text = "Hi Guest!"
            return
        }

        val db = FirebaseFirestore.getInstance()

        // Fetch user document
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // A. Update Name
                    val firstName = document.getString("firstName") ?: "User"
                    binding.tvGreeting.text = "Hi $firstName!"

                    // B. Update Profile Picture
                    val imageUrl = document.getString("profileImageUrl")
                    Log.d("TenantFeed", "Fetched Profile URL: '$imageUrl'")

                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_tenant_profile_placeholder) // Ensure this exists
                            .error(R.drawable.ic_tenant_profile_placeholder)
                            .circleCrop()
                            .into(binding.ivProfileAvatar)
                    } else {
                        // If URL is empty/null, explicitly set placeholder
                        binding.ivProfileAvatar.setImageResource(R.drawable.ic_tenant_profile_placeholder)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("TenantFeed", "Error loading profile", e)
                binding.tvGreeting.text = "Hi User!"
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}