package com.example.myapplication.view

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentTenantAnalyticsBinding
import com.example.myapplication.model.SavedProperty
import com.example.myapplication.repository.TenantSavedPropertyRepository
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.auth.FirebaseAuth

class TenantAnalyticsFragment : Fragment() {

    private var _binding: FragmentTenantAnalyticsBinding? = null
    private val binding get() = _binding!!

    private lateinit var savedRepository: TenantSavedPropertyRepository
    private var currentUserId: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTenantAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedRepository = TenantSavedPropertyRepository(requireContext())
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        setupToolbar()
        loadCategoryAnalytics()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun loadCategoryAnalytics() {
        if (currentUserId.isEmpty()) return

        savedRepository.getSavedProperties(currentUserId) { savedList ->
            setupCategoryPieChart(savedList)
            binding.tvTotalSaved.text = savedList.size.toString()
        }
    }

    private fun setupCategoryPieChart(savedList: List<SavedProperty>) {
        var houseCount = 0
        var apartmentCount = 0
        var annexCount = 0
        var otherCount = 0

        // "Guess" category from Title
        for (property in savedList) {
            val title = property.title.lowercase()
            when {
                title.contains("house") || title.contains("home") || title.contains("villa") -> houseCount++
                title.contains("apartment") || title.contains("flat") || title.contains("condo") -> apartmentCount++
                title.contains("annex") || title.contains("room") -> annexCount++
                else -> otherCount++
            }
        }

        val entries = ArrayList<PieEntry>()
        if (houseCount > 0) entries.add(PieEntry(houseCount.toFloat(), "Houses"))
        if (apartmentCount > 0) entries.add(PieEntry(apartmentCount.toFloat(), "Apartments"))
        if (annexCount > 0) entries.add(PieEntry(annexCount.toFloat(), "Annexes"))
        if (otherCount > 0) entries.add(PieEntry(otherCount.toFloat(), "Others"))

        if (entries.isEmpty()) {
            entries.add(PieEntry(1f, "No Data"))
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            ContextCompat.getColor(requireContext(), R.color.app_blue),
            ContextCompat.getColor(requireContext(), R.color.royal_blue),
            ContextCompat.getColor(requireContext(), R.color.bright_yellow),
            ContextCompat.getColor(requireContext(), R.color.app_text_gray)
        )

        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 14f
        dataSet.sliceSpace = 3f

        val data = PieData(dataSet)
        binding.pieChart.apply {
            this.data = data
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            holeRadius = 55f
            transparentCircleRadius = 60f

            centerText = "Interests"
            setCenterTextSize(16f)
            setCenterTextColor(ContextCompat.getColor(context, R.color.app_navbar_blue))

            legend.isEnabled = true
            legend.textColor = ContextCompat.getColor(context, R.color.app_text_gray)

            animateY(1200)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}