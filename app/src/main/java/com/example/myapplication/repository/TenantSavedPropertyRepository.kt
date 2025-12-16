package com.example.myapplication.repository

import android.content.Context
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
                // Firestore automatically maps fields if names match exactly
                val properties = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(SavedProperty::class.java)
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