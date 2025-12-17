package com.example.myapplication.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.example.myapplication.model.OnboardingItem
import com.example.myapplication.utils.SharedPreferencesHelper
import kotlin.math.abs

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnAction: Button
    private lateinit var sharedPrefs: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.view_pager_onboarding)
        btnAction = findViewById(R.id.btn_onboarding_action)
        sharedPrefs = SharedPreferencesHelper(this)

        val onboardingItems = listOf(
            OnboardingItem(
                imageResId = R.drawable.img_onboarding_1,
                title = "Find Your Dream Home",
                description = "Explore thousands of rentals nearby, simple, fast, and stress-free with TGM Rentify."
            ),
            OnboardingItem(
                imageResId = R.drawable.img_onboarding_2,
                title = "Browse, Compare & Manage",
                description = "View listings, compare homes, and manage your properties — all in one app with TGM Rentify."
            )
        )

        val adapter = OnboardingPagerAdapter(onboardingItems)
        viewPager.adapter = adapter
        
        // Add Animation Transformer
        viewPager.setPageTransformer(DepthPageTransformer())

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == onboardingItems.size - 1) {
                    btnAction.text = "Get Started"
                    // Simple animation for button change
                    btnAction.alpha = 0f
                    btnAction.animate().alpha(1f).setDuration(300).start()
                } else {
                    btnAction.text = "Next"
                    btnAction.alpha = 1f
                }
            }
        })

        btnAction.setOnClickListener {
            if (viewPager.currentItem + 1 < adapter.itemCount) {
                viewPager.currentItem += 1
            } else {
                sharedPrefs.setOnboardingCompleted(true)
                val intent = Intent(this, RoleSelectionActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    // Custom Page Transformer for Depth/Zoom Animation
    class DepthPageTransformer : ViewPager2.PageTransformer {
        private val MIN_SCALE = 0.75f

        override fun transformPage(view: View, position: Float) {
            view.apply {
                val pageWidth = width
                when {
                    position < -1 -> { // [-Infinity,-1)
                        // This page is way off-screen to the left.
                        alpha = 0f
                    }
                    position <= 0 -> { // [-1,0]
                        // Use the default slide transition when moving to the left page
                        alpha = 1f
                        translationX = 0f
                        translationZ = 0f
                        scaleX = 1f
                        scaleY = 1f
                    }
                    position <= 1 -> { // (0,1]
                        // Fade the page out.
                        alpha = 1f - position

                        // Counteract the default slide transition
                        translationX = pageWidth * -position
                        // Move it behind the left page
                        translationZ = -1f

                        // Scale the page down (between MIN_SCALE and 1)
                        val scaleFactor = (MIN_SCALE + (1 - MIN_SCALE) * (1 - abs(position)))
                        scaleX = scaleFactor
                        scaleY = scaleFactor
                    }
                    else -> { // (1,+Infinity]
                        // This page is way off-screen to the right.
                        alpha = 0f
                    }
                }
            }
        }
    }
}
