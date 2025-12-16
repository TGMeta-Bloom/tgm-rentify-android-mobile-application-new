package com.example.myapplication.view

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentTenantMyPostsBinding
import com.example.myapplication.model.TenantPost
import com.example.myapplication.repository.TenantFeedRepository
import com.example.myapplication.view.adapter.TenantMyPostsAdapter
import com.google.firebase.auth.FirebaseAuth

class TenantMyPostsFragment : Fragment() {

    private var _binding: FragmentTenantMyPostsBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: TenantFeedRepository
    private lateinit var adapter: TenantMyPostsAdapter
    private var currentUserId: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTenantMyPostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Initialize Repository
        repository = TenantFeedRepository(requireContext())

        //Get the current User ID to ensure we ONLY fetch this user's data
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        setupRecyclerView()

        //Load data immediately
        loadMyPosts()

        //Back button logic
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        adapter = TenantMyPostsAdapter { post ->
            showDeleteConfirmationDialog(post)
        }
        binding.rvMyPosts.layoutManager = LinearLayoutManager(context)
        binding.rvMyPosts.adapter = adapter
    }

    private fun loadMyPosts() {
        if (currentUserId.isEmpty()) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // FETCH: This calls the repository function that uses .whereEqualTo("userId", currentUserId)
        repository.getUserPosts(currentUserId) { posts ->
            // A. Update the list
            adapter.submitList(posts)

            // B. Update the top stats card
            calculateStats(posts)
        }
    }

    private fun calculateStats(posts: List<TenantPost>) {
        //  Calculate totals dynamically from the live list
        val totalPosts = posts.size
        val totalHelpful = posts.sumOf { it.helpfulCount }

        //  Bind to the IDs in fragment_tenant_my_posts.xml
        binding.tvTotalPosts.text = totalPosts.toString()
        binding.tvTotalHelpful.text = totalHelpful.toString()
    }

    private fun showDeleteConfirmationDialog(post: TenantPost) {
        // Inflate the custom dialog layout
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_confirmation_tenant, null)

        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnConfirm = dialogView.findViewById<Button>(R.id.btn_confirm_delete)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel_delete)

        btnConfirm.setOnClickListener {
            deletePost(post)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun deletePost(post: TenantPost) {
        repository.deletePost(post.id) { success ->
            if (success) {
                Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show()
                loadMyPosts() //Reload the list and stats after deletion
            } else {
                Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}