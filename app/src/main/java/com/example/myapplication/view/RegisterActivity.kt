package com.example.myapplication.view

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
import android.view.ViewGroup
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.model.User
import com.example.myapplication.repository.AuthRepository
import com.example.myapplication.utils.SharedPreferencesHelper
import com.example.myapplication.viewModel.AuthViewModel
import com.example.myapplication.viewModel.AuthViewModelFactory
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class RegisterActivity : AppCompatActivity() {

    private var userRole: String? = null
    private var imageUri: Uri? = null
    private lateinit var profileImageView: CircleImageView
    private lateinit var prefsHelper: SharedPreferencesHelper
    private var loadingDialog: AlertDialog? = null // Added Loading Dialog

    // ViewModel setup
    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository())
    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            imageUri?.let {
                Glide.with(this).load(it).into(profileImageView)
            }
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            Glide.with(this).load(it).into(profileImageView)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission is required to take a picture", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        prefsHelper = SharedPreferencesHelper(this)
        userRole = intent.getStringExtra("selected_role")

        profileImageView = findViewById(R.id.image_profile)
        
        // Input Fields (EditText)
        val etFirstName = findViewById<EditText>(R.id.input_first_name)
        val etLastName = findViewById<EditText>(R.id.input_last_name)
        val etEmail = findViewById<EditText>(R.id.input_email_register)
        val etPassword = findViewById<EditText>(R.id.input_password_register)
        val etConfirmPassword = findViewById<EditText>(R.id.input_confirm_password)
        val etMobile = findViewById<EditText>(R.id.input_mobile)
        val etBio = findViewById<EditText>(R.id.input_bio)
        
        // Text Input Layouts (For Errors)
        val layoutFirstName = findViewById<TextInputLayout>(R.id.input_layout_first_name)
        val layoutLastName = findViewById<TextInputLayout>(R.id.input_layout_last_name)
        val layoutEmail = findViewById<TextInputLayout>(R.id.input_layout_email_register)
        val layoutPassword = findViewById<TextInputLayout>(R.id.input_layout_password_register)
        val layoutConfirmPassword = findViewById<TextInputLayout>(R.id.input_layout_confirm_password)
        val layoutMobile = findViewById<TextInputLayout>(R.id.input_layout_mobile)
        val layoutBio = findViewById<TextInputLayout>(R.id.input_layout_bio)
        val layoutCity = findViewById<TextInputLayout>(R.id.input_layout_city)

        val btnRegister = findViewById<Button>(R.id.btn_register)
        val btnCamera = findViewById<ImageButton>(R.id.btn_camera)
        val checkboxTerms = findViewById<MaterialCheckBox>(R.id.checkbox_terms)
        val btnBack = findViewById<ImageButton>(R.id.btn_back) // Back Button

        val cities = resources.getStringArray(R.array.city_array)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cities)
        val cityDropdown = findViewById<AutoCompleteTextView>(R.id.autocomplete_city)
        cityDropdown.setAdapter(adapter)
        cityDropdown.threshold = 1 // Allow filtering from the first character

        // Back Button Logic
        btnBack.setOnClickListener {
            finish()
        }

        // === SETUP TERMS & CONDITIONS POPUPS ===
        setupTermsAndConditions(checkboxTerms)


        // === Real-time Password Validation ===
        val passwordWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val password = etPassword.text.toString()
                val confirm = etConfirmPassword.text.toString()

                if (confirm.isNotEmpty() && password != confirm) {
                    layoutConfirmPassword.error = "Passwords do not match"
                } else {
                    layoutConfirmPassword.error = null
                }
            }
        }
        etPassword.addTextChangedListener(passwordWatcher)
        etConfirmPassword.addTextChangedListener(passwordWatcher)


        setupObservers(etEmail, etFirstName, etLastName)

        btnCamera.setOnClickListener {
            showImageSourceDialog()
        }

        btnRegister.setOnClickListener {
            // Reset Errors
            layoutFirstName.error = null
            layoutLastName.error = null
            layoutEmail.error = null
            layoutPassword.error = null
            layoutConfirmPassword.error = null
            layoutMobile.error = null
            layoutBio.error = null
            layoutCity.error = null

            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()
            val mobile = etMobile.text.toString().trim()
            val bio = etBio.text.toString().trim()
            val selectedCity = cityDropdown.text.toString()

            var isValid = true

            if (firstName.isEmpty()) {
                layoutFirstName.error = "First name is required"
                isValid = false
            }

            if (lastName.isEmpty()) {
                layoutLastName.error = "Last name is required"
                isValid = false
            }

            // ANDROID STANDARD EMAIL VALIDATION (Strict Format Check)
            if (email.isEmpty()) {
                layoutEmail.error = "Email is required"
                isValid = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                layoutEmail.error = "Invalid email address"
                isValid = false
            }

            if (mobile.isEmpty()) {
                layoutMobile.error = "Mobile number is required"
                isValid = false
            } else if (mobile.length != 10) {
                 layoutMobile.error = "Mobile number must be 10 digits"
                 isValid = false
            }

            if (password.isEmpty()) {
                layoutPassword.error = "Password is required"
                isValid = false
            } else if (password.length < 6) {
                layoutPassword.error = "Password must be at least 6 characters"
                isValid = false
            }

            if (confirmPassword.isEmpty()) {
                layoutConfirmPassword.error = "Please confirm your password"
                isValid = false
            } else if (password != confirmPassword) {
                layoutConfirmPassword.error = "Passwords do not match"
                isValid = false
            }

            if (bio.isEmpty()) {
                layoutBio.error = "Bio is required"
                isValid = false
            }

            if (selectedCity.isEmpty() || selectedCity == getString(R.string.city_area)) {
                layoutCity.error = "Please select a city"
                isValid = false
            }

            // Check Terms Checkbox
            if (!checkboxTerms.isChecked) {
                Toast.makeText(this, "You must agree to the Terms & Conditions", Toast.LENGTH_SHORT).show()
                isValid = false
            }

            if (!isValid) return@setOnClickListener

            val newUser = User(
                firstName = firstName,
                lastName = lastName,
                email = email,
                mobileNumber = mobile,
                bio = bio,
                city = selectedCity,
                role = userRole ?: "Tenant"
            )

            // Pass 'this' (Context) for ImgBB logic
            viewModel.registerUser(this, newUser, password, imageUri)
        }
    }

    private fun setupTermsAndConditions(checkbox: MaterialCheckBox) {
        val fullText = "I agree to the Terms & Conditions and Privacy Policy"
        val spannable = SpannableString(fullText)

        // 1. Clickable Link for "Terms"
        val termsStart = fullText.indexOf("Terms")
        val termsEnd = termsStart + "Terms".length
        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                showInfoDialog("Terms of Service", getTermsText())
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = ContextCompat.getColor(this@RegisterActivity, R.color.colorAccent)
            }
        }, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // 2. Clickable Link for "Conditions"
        // Mapping "Conditions" to the same Terms text as it's usually one document "Terms & Conditions"
        val condStart = fullText.indexOf("Conditions")
        val condEnd = condStart + "Conditions".length
        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                showInfoDialog("Terms of Service", getTermsText())
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = ContextCompat.getColor(this@RegisterActivity, R.color.colorAccent)
            }
        }, condStart, condEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        
        // 3. Clickable Link for "Privacy Policy"
        val privStart = fullText.indexOf("Privacy Policy")
        if (privStart != -1) {
            val privEnd = privStart + "Privacy Policy".length
            spannable.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    showInfoDialog("Privacy Policy", getPrivacyPolicyText())
                }
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = true
                    ds.color = ContextCompat.getColor(this@RegisterActivity, R.color.colorAccent)
                }
            }, privStart, privEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        checkbox.text = spannable
        checkbox.movementMethod = LinkMovementMethod.getInstance()
    }
    
    // Custom Dialog Implementation for Terms/Privacy
    private fun showInfoDialog(title: String, message: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_info)
        
        // Essential for rounded corners (CardView) to show properly
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        val tvTitle = dialog.findViewById<TextView>(R.id.dialog_title)
        val tvMessage = dialog.findViewById<TextView>(R.id.dialog_message)
        val btnClose = dialog.findViewById<Button>(R.id.btn_dialog_close)
        
        tvTitle.text = title
        tvMessage.text = message
        
        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()

        // Set Dialog Width to 95% of Screen for Text Info
        val width = (resources.displayMetrics.widthPixels * 0.95).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    // Content for Terms of Service
    private fun getTermsText(): String {
        return "RentSmart Lite - Terms of Service\n\n" +
                "By registering for RentSmart Lite, you agree to comply with and be bound by the following terms and conditions of use. Please review them carefully.\n\n" +
                "1. Service Use\n" +
                "You agree to use the app only for its intended purpose (rental searches, listings, communication).\n\n" +
                "2. User Conduct\n" +
                "You will not post false information, abusive content, or violate the rights of others (Landlords or Tenants).\n\n" +
                "3. Account Security\n" +
                "You are responsible for maintaining the confidentiality of your password and account activities.\n\n" +
                "4. Liability\n" +
                "RentSmart Lite acts as a listing platform and is not responsible for damages resulting from disputes between users or inaccuracies in property listings.\n\n" +
                "5. Termination\n" +
                "RentSmart Lite reserves the right to terminate or suspend your account access for any breach of these Terms.\n\n" +
                "Your agreement to these terms is mandatory for continued use of the service."
    }

    // Content for Privacy Policy
    private fun getPrivacyPolicyText(): String {
        return "RentSmart Lite - Privacy Policy\n\n" +
                "This Privacy Policy explains how RentSmart Lite collects, uses, and protects your personal information.\n\n" +
                "1. Data Collection\n" +
                "We collect data you provide: Name, Email, Mobile Number, City, Password (stored securely), and Role (Landlord/Tenant). We also collect your Profile Image.\n\n" +
                "2. Data Usage\n" +
                "Your data is used for: Account identification (login), Personalized feed display, Role-based features, and Critical security alerts (via email/mobile).\n\n" +
                "3. Data Sharing\n" +
                "We do not sell your personal data. We may share information with law enforcement if legally required.\n\n" +
                "4. Data Storage & Security\n" +
                "Your data is stored on secured Firebase servers. Profile images are stored in Firebase Storage.\n\n" +
                "5. Your Rights\n" +
                "You have the right to update or delete your profile information (managed via the Profile Settings screen).\n\n" +
                "By using the app, you consent to the data practices described in this policy."
    }

    private fun setupObservers(emailView: EditText, fNameView: EditText, lNameView: EditText) {
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                 // Show Blocking Dialog
                 if (loadingDialog == null) {
                     loadingDialog = AlertDialog.Builder(this)
                         .setTitle("Please Wait")
                         .setMessage("Processing Registration...")
                         .setCancelable(false)
                         .create()
                 }
                 loadingDialog?.show()
            } else {
                 loadingDialog?.dismiss()
            }
        }

        viewModel.registrationResult.observe(this) { result ->
            result.onSuccess { uid ->
                loadingDialog?.dismiss() // Ensure dialog is closed
                
                // SECURITY: Message tells them to verify
                Toast.makeText(this, "Registration Successful! Please check email to verify.", Toast.LENGTH_LONG).show()

                // Logout to force them to verify before login
                FirebaseAuth.getInstance().signOut()
                prefsHelper.clearSession()

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }.onFailure { exception ->
                loadingDialog?.dismiss() // Ensure dialog is closed
                Toast.makeText(this, "Registration Failed: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // UPDATED: Custom Image Selection Dialog
    private fun showImageSourceDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_image_picker)
        
        // Essential for rounded corners
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        // Find Views
        val btnCamera = dialog.findViewById<Button>(R.id.btn_take_photo)
        val btnGallery = dialog.findViewById<Button>(R.id.btn_choose_gallery)
        // Removed btnCancel since it is being removed from layout
        
        // Set Listeners
        btnCamera.setOnClickListener {
            checkCameraPermissionAndOpenCamera()
            dialog.dismiss()
        }
        
        btnGallery.setOnClickListener {
            pickImage.launch("image/*")
            dialog.dismiss()
        }
        
        // Removed btnCancel listener
        
        dialog.show()

        // Set Width to 85% of Screen
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun checkCameraPermissionAndOpenCamera() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        try {
            val photoFile = File.createTempFile(
                "JPEG_${System.currentTimeMillis()}_",
                ".jpg",
                getExternalFilesDir(null)
            )
            val newImageUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                photoFile
            )
            imageUri = newImageUri
            takePicture.launch(newImageUri)
        } catch (e: Exception) {
            Toast.makeText(this, "Error creating file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
