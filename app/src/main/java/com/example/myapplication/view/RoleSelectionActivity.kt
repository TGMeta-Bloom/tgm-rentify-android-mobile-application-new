package com.example.myapplication.view

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.myapplication.R

class RoleSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_selection)

        val landlordCard: CardView = findViewById(R.id.card_landlord)
        val tenantCard: CardView = findViewById(R.id.card_tenant)
        val needHelpLayout: LinearLayout = findViewById(R.id.text_need_help)

        landlordCard.setOnClickListener {
            // Navigate Login/Register with selected role
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("selected_role", "Landlord")
            startActivity(intent)
        }

        tenantCard.setOnClickListener {
            // Navigate Login/Register with selected role
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("selected_role", "Tenant")
            startActivity(intent)
        }

        needHelpLayout.setOnClickListener {
            showHelpDialog()
        }
    }

    private fun showHelpDialog() {
        // Use the custom layout 'dialog_help'
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_help)

        // Essential for rounded corners (CardView) to show
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Find Buttons from the custom layout
        val btnEmail = dialog.findViewById<Button>(R.id.btn_email_support)
        val btnCall = dialog.findViewById<Button>(R.id.btn_call_us)
        val btnFaq = dialog.findViewById<Button>(R.id.btn_visit_faq)
        val btnClose = dialog.findViewById<Button>(R.id.btn_dialog_close)

        // 1. Email Support Action
        btnEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@rentifyapp.com")
                putExtra(Intent.EXTRA_SUBJECT, "Help Request - TGM Rentify")
            }
            try {
                startActivity(intent)
                dialog.dismiss()
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "No email app found.", Toast.LENGTH_SHORT).show()
            }
        }

        // 2. Call Us Action
        btnCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:+94763939423")
            }
            try {
                startActivity(intent)
                dialog.dismiss()
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "No phone app found.", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. Visit FAQ Action
        btnFaq.setOnClickListener {
            val url = "https://www.google.com/search?q=rentify+faq" // Placeholder FAQ URL
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            try {
                startActivity(intent)
                dialog.dismiss()
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "No web browser found.", Toast.LENGTH_SHORT).show()
            }
        }

        // Close Button
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

        // === Set Dialog Width to 95% of Screen (User Friendly) ===
        val width = (resources.displayMetrics.widthPixels * 0.95).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
