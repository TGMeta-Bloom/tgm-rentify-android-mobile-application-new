package com.example.myapplication.view.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemTenantLibraryCategoryBinding
import com.example.myapplication.model.LibraryCategory
import com.example.myapplication.model.LibraryArticle
import com.google.android.material.chip.Chip

class TenantLibraryAdapter(
    private val categories: List<LibraryCategory>,
    private val onArticleClick: (LibraryArticle) -> Unit
) : RecyclerView.Adapter<TenantLibraryAdapter.LibraryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryViewHolder {
        val binding = ItemTenantLibraryCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LibraryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LibraryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount() = categories.size

    inner class LibraryViewHolder(private val binding: ItemTenantLibraryCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: LibraryCategory) {
            val context = binding.root.context

            //Set Title & Icon
            binding.tvCategoryTitle.text = category.title
            binding.ivCategoryIcon.setImageResource(category.iconRes)

            // --- PROFESSIONAL STYLING (Enforcing Brand Colors) ---

            // Icon Background: Use 'app_blue' for a consistent pop of color
            binding.cardIconBg.setCardBackgroundColor(
                ContextCompat.getColor(context, R.color.app_blue)
            )
            // Icon Tint: White for contrast
            binding.ivCategoryIcon.setColorFilter(
                ContextCompat.getColor(context, R.color.white)
            )

            //Build Chips
            binding.chipGroupArticles.removeAllViews()

            for (article in category.articles) {
                val chip = Chip(context)
                chip.text = article.title

                // --- Chip Styling: "Soft Tag" Look ---
                // Background: Very light blue (window_background_light_blue)
                chip.chipBackgroundColor = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.window_background_light_blue)
                )

                // Text: Deep Royal Blue for readability
                chip.setTextColor(
                    ContextCompat.getColor(context, R.color.royal_blue)
                )

                // Remove Stroke (Border) for a cleaner "Tag" look
                chip.chipStrokeWidth = 0f

                // Touch styling
                chip.rippleColor = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.app_blue)
                )
                chip.typeface = Typeface.DEFAULT_BOLD
                chip.setEnsureMinTouchTargetSize(false)

                chip.setOnClickListener {
                    onArticleClick(article)
                }

                binding.chipGroupArticles.addView(chip)
            }
        }
    }
}