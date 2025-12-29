package com.example.mediconnect_ai.firestore

import android.util.Log
import com.example.mediconnect_ai.database.Pregnancy
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

object FirebasePregnancyHelper {

    private const val TAG = "FirebasePregnancyHelper"

    private val db = FirebaseFirestore.getInstance()
    private val collectionRef = db.collection("pregnancies")

    // Save or update a pregnancy
    fun savePregnancy(pregnancy: Pregnancy, callback: (Boolean, String?) -> Unit) {
        val docId = pregnancy.id.toString()

        collectionRef.document(docId)
            .set(pregnancy)
            .addOnSuccessListener {
                Log.d(TAG, "Pregnancy synced. id=$docId")
                callback(true, null)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to sync pregnancy id=$docId: ${e.localizedMessage}")
                callback(false, e.localizedMessage)
            }
    }

    // Optional: fetch all pregnancies
    fun fetchPregnancies(callback: (List<Pregnancy>?, String?) -> Unit) {
        collectionRef.get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { it.toObject<Pregnancy>() }
                callback(list, null)
            }
            .addOnFailureListener { e ->
                callback(null, e.localizedMessage)
            }
    }

    // Optional: fetch one pregnancy by ID
    fun fetchPregnancyById(id: Long, callback: (Pregnancy?, String?) -> Unit) {
        collectionRef.document(id.toString())
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    callback(doc.toObject<Pregnancy>(), null)
                } else {
                    callback(null, "Pregnancy not found")
                }
            }
            .addOnFailureListener { e ->
                callback(null, e.localizedMessage)
            }
    }
}
