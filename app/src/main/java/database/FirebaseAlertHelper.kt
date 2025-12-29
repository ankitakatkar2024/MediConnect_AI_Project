package com.example.mediconnect_ai.firestore

import android.util.Log
import com.example.mediconnect_ai.Alert
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject

object FirebaseAlertHelper {

    private const val TAG = "FirebaseAlertHelper"

    private val db = FirebaseFirestore.getInstance()
    private val collectionRef = db.collection("alerts")

    // Save a broadcast alert
    fun saveAlert(alert: Alert, callback: (Boolean, String?) -> Unit) {
        collectionRef.add(alert)
            .addOnSuccessListener {
                Log.d(TAG, "Alert logged, docId=${it.id}")
                callback(true, null)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to log alert: ${e.localizedMessage}")
                callback(false, e.localizedMessage)
            }
    }

    // Optional: fetch alerts (for admin dashboard, etc.)
    fun fetchAlerts(callback: (List<Alert>?, String?) -> Unit) {
        collectionRef
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { it.toObject<Alert>() }
                callback(list, null)
            }
            .addOnFailureListener { e ->
                callback(null, e.localizedMessage)
            }
    }
}
