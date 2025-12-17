package com.example.myapplication.view

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentTenantSavedPropertiesBinding
import com.example.myapplication.model.SavedProperty
import com.example.myapplication.repository.TenantSavedPropertyRepository
import com.example.myapplication.view.adapter.TenantSavedPropertyAdapter
import com.google.firebase.auth.FirebaseAuth

class TenantSavedPropertyFragment : Fragment() {

    private var _binding: FragmentTenantSavedPropertiesBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: TenantSavedPropertyRepository
    private lateinit var adapter: TenantSavedPropertyAdapter
    private var currentUserId: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTenantSavedPropertiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = TenantSavedPropertyRepository(requireContext())
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        setupRecyclerView()
        loadProperties()

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        adapter = TenantSavedPropertyAdapter { property ->
            showRemoveDialog(property)
        }
        binding.rvSavedProperties.layoutManager = GridLayoutManager(context, 1)
        binding.rvSavedProperties.adapter = adapter
    }

    private fun loadProperties() {
        if (currentUserId.isEmpty()) return

        repository.getSavedProperties(currentUserId) { list ->
            adapter.submitList(list)
        }
    }

    private fun showRemoveDialog(property: SavedProperty) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_remove_saved_property, null)

        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnConfirm = dialogView.findViewById<Button>(R.id.btn_confirm_remove)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel_remove)

        btnConfirm.setOnClickListener {
            removeProperty(property)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun removeProperty(property: SavedProperty) {
        repository.removeSavedProperty(property.id) { success ->
            if (success) {
                Toast.makeText(context, "Removed from saved list", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to remove", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}