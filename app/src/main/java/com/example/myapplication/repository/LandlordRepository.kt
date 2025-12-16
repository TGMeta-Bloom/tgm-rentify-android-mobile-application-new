package com.example.myapplication.repository

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.AppConfig
import com.example.myapplication.model.Property
import com.example.myapplication.model.User
import com.example.myapplication.network.ImgBBClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.UUID

class LandlordRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val propertiesCollection = firestore.collection("properties")
    private val usersCollection = firestore.collection("users")

    /// Get current users'Id
    private val currentUserId: String?
        get() = auth.currentUser?.uid


    ///  Fetch properties for the currently logged-in landlord
    fun getLandlordProperties(): LiveData<List<Property>> {
        val propertiesLiveData = MutableLiveData<List<Property>>()
        val currentId = currentUserId ?: return propertiesLiveData

        propertiesCollection
            .whereEqualTo("userId", currentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("LandlordRepo", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val propertyList = snapshot.documents.mapNotNull { doc ->
                        try {
                            Property(
                                propertyId = doc.getString("propertyId") ?: doc.id,
                                userId = doc.getString("userId") ?: "",
                                title = doc.getString("title") ?: "",
                                description = doc.getString("description") ?: "",
                                location = doc.getString("location") ?: "",
                                rentAmount = doc.getDouble("rentAmount") ?: 0.0,
                                propertyType = doc.getString("propertyType") ?: "Apartment",
                                imageUrls = (doc.get("imageUrls") as? List<String>) ?: null, /// Keep null for now as per previous fix
                                status = doc.getString("status") ?: "Available",
                                contactNumber = doc.getString("contactNumber") ?: ""
                            )
                        } catch (e: Exception) {
                            Log.e("LandlordRepo", "Error mapping document: ${doc.id}", e)
                            null
                        }
                    }
                    propertiesLiveData.value = propertyList
                }
            }
        return propertiesLiveData
    }

    /// Fetch current user profile
    fun getCurrentUser(onResult: (User?) -> Unit) {
        val currentId = currentUserId
        if (currentId == null) {
            onResult(null)
            return
        }

        usersCollection.document(currentId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    onResult(user)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("LandlordRepo", "Error fetching user", e)
                onResult(null)
            }
    }


    ///Add a new property to Firestore
    suspend fun addProperty(property: Property) {
        // Ensure propertyId is consistent if not provided
        val finalProperty = if (property.propertyId.isEmpty()) {
            property.copy(propertyId = UUID.randomUUID().toString())
        } else {
            property
        }

        propertiesCollection.document(finalProperty.propertyId).set(finalProperty).await()
    }


    /// Update an existing property in Firestore
    suspend fun updateProperty(property: Property) {
        propertiesCollection.document(property.propertyId).set(property).await()
    }


    /// Delete a property and its associated images
    suspend fun deleteProperty(propertyId: String) {
        val landlordId = currentUserId ?: throw Exception("User not logged in")

        ///Delete Firestore Document
        propertiesCollection.document(propertyId).delete().await()

        ///Delete Images from Storage
        try {
            val storageRef = storage.reference.child("property_images/$landlordId/$propertyId")
            val listResult = storageRef.listAll().await()
            for (item in listResult.items) {
                item.delete().await()
            }
        } catch (e: Exception) {
            Log.e("LandlordRepo", "Error deleting images for property: $propertyId", e)
        }
    }


    ///Upload an image to Firebase Storage and return the download URL
    suspend fun uploadPropertyImage(uri: Uri, propertyId: String): String {
        val landlordId = currentUserId ?: throw Exception("User not logged in")
        val uniqueFilename = UUID.randomUUID().toString() + ".jpg"

        /// Path
        val ref = storage.reference.child("property_images/$landlordId/$propertyId/$uniqueFilename")

        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }




    private val imgBBApi = ImgBBClient.api /// Assuming ImgBBClient provides the Retrofit API instance

    suspend fun uploadPropertyImage(imageFile: File): String {

        ///  Check if API Key is loaded
        val apiKey = AppConfig.IMGBB_API_KEY
        Log.d("ImgBB", "Using API Key: '$apiKey'")

        if (apiKey.isEmpty() || apiKey == "null") {

            throw Exception("API Key is missing! Check local.properties and Sync Gradle.")
        }

        val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

        val response = imgBBApi.uploadImage(
            apiKey = apiKey,
            image = body
        )

        if (response.isSuccessful) {
            return response.body()?.data?.url ?: throw Exception("Image URL not found in response.")
        } else {
            val errorBody = response.errorBody()?.string()
            Log.e("ImgBB", "Upload Failed: $errorBody")
            throw Exception("Image upload failed: $errorBody")
        }
    }
}