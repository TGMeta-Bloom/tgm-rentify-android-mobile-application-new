package com.example.myapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentTenantLibraryBinding
import com.example.myapplication.model.LibraryArticle
import com.example.myapplication.model.LibraryCategory
import com.example.myapplication.view.adapter.TenantLibraryAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog

class TenantLibraryFragment : Fragment() {

    private var _binding: FragmentTenantLibraryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTenantLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLibraryList()

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupLibraryList() {
        // --- HARDCODED DATA (Updated with your Professional Colors) ---
        val data = listOf(
            LibraryCategory(
                "Emergency Plumbing",
                R.drawable.ic_home,
                "#EF5350", // @color/red (Professional Soft Red)
                listOf(
                    LibraryArticle("Fix a Leaky Tap", "1. Turn off water supply.\n2. Dismantle the handle.\n3. Replace the washer.\n4. Reassemble."),
                    LibraryArticle("Unclog a Drain", "Use a plunger first. If that fails, try a mixture of vinegar and baking soda."),
                    LibraryArticle("Burst Pipe", "Turn off the main water valve immediately! Call a professional plumber.")
                )
            ),
            LibraryCategory(
                "Electrical Safety",
                R.drawable.ic_tenant_analytics,
                "#026EA3", // @color/silk_blue (Professional Blue)
                listOf(
                    LibraryArticle("Changing a Bulb", "Ensure the switch is off. Let the old bulb cool down before touching it."),
                    LibraryArticle("Breaker Tripped?", "Unplug heavy appliances. Locate your fuse box and flip the switch back to ON.")
                )
            ),
            LibraryCategory(
                "Tenant Rights",
                R.drawable.ic_tenant_post,
                "#039BE5", // @color/sky_blue (Calm Blue)
                listOf(
                    LibraryArticle("Returning Keys", "Ensure you return keys on the agreed date to avoid penalties."),
                    LibraryArticle("Deposit Refund", "Your deposit should be returned within 30 days of moving out, minus damages.")
                )
            )
        )

        val adapter = TenantLibraryAdapter(data) { article ->
            showArticleDialog(article)
        }

        binding.rvLibrary.layoutManager = LinearLayoutManager(context)
        binding.rvLibrary.adapter = adapter
    }

    private fun showArticleDialog(article: LibraryArticle) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_tenant_library_article, null)

        view.findViewById<TextView>(R.id.tv_article_title).text = article.title
        view.findViewById<TextView>(R.id.tv_article_body).text = article.body

        view.findViewById<View>(R.id.btn_close).setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}