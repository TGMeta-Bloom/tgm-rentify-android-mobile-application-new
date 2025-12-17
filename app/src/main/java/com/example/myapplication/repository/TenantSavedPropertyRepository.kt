package com.example.myapplication.repository

import android.content.Context
import android.util.Log
import com.example.myapplication.model.SavedProperty
import com.google.firebase.firestore.FirebaseFirestore

class TenantSavedPropertyRepository(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("saved_properties")

    fun getSavedProperties(userId: String, onResult: (List<SavedProperty>) -> Unit) {
        collection.whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    onResult(emptyList())
                    return@addSnapshotListener
                }

                val properties = snapshot.documents.mapNotNull { doc ->
                    try {
                        // --- FIX: MANUAL MAPPING (Bypasses the @DocumentId crash) ---
                        // We read fields one by one instead of using toObject()
                        SavedProperty(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            propertyId = doc.getString("propertyId") ?: "",
                            title = doc.getString("title") ?: "",
                            location = doc.getString("location") ?: "",
                            imageUrl = doc.getString("imageUrl") ?: "",
                            rentAmount = doc.getDouble("rentAmount") ?: 0.0
                        )
                    } catch (e: Exception) {
                        Log.e("SavedPropRepo", "Error mapping document: ${doc.id}", e)
                        null
                    }
                }
                onResult(properties)
            }
    }

    fun removeSavedProperty(docId: String, onResult: (Boolean) -> Unit) {
        collection.document(docId).delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}