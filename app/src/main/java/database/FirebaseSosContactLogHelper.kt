package com.example.mediconnect_ai.firestore

import android.util.Log
import com.example.mediconnect_ai.SosLog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject

object FirebaseSosContactLogHelper {

    private const val TAG = "FirebaseSosContactLogHelper"

    private val db = FirebaseFirestore.getInstance()
    private val collectionRef = db.collection("emergency_contact_logs")

    fun saveLog(log: SosLog, callback: (Boolean, String?) -> Unit) {
        // We use auto document IDs here; Room id is only for local DB
        collectionRef.add(log)
            .addOnSuccessListener {
                Log.d(TAG, "Contact SOS log synced, docId=${it.id}")
                callback(true, null)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to sync contact SOS log: ${e.localizedMessage}")
                callback(false, e.localizedMessage)
            }
    }

    // Optional: fetch from cloud
    fun fetchLogs(callback: (List<SosLog>?, String?) -> Unit) {
        collectionRef
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { it.toObject<SosLog>() }
                callback(list, null)
            }
            .addOnFailureListener { e ->
                callback(null, e.localizedMessage)
            }
    }
}
