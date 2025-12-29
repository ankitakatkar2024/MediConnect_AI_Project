package com.example.mediconnect_ai.firestore

import android.util.Log
import com.example.mediconnect_ai.SosAlert
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject

object FirebaseSosAlertHelper {

    private const val TAG = "FirebaseSosAlertHelper"

    private val db = FirebaseFirestore.getInstance()
    private val collectionRef = db.collection("sos_alerts")

    // Save one SOS alert
    fun saveAlert(alert: SosAlert, callback: (Boolean, String?) -> Unit) {
        collectionRef.add(alert)
            .addOnSuccessListener {
                Log.d(TAG, "SOS alert logged, docId=${it.id}")
                callback(true, null)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to log SOS alert: ${e.localizedMessage}")
                callback(false, e.localizedMessage)
            }
    }

    // Optional: fetch alerts (e.g., for admin dashboard)
    fun fetchAlerts(callback: (List<SosAlert>?, String?) -> Unit) {
        collectionRef
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { it.toObject<SosAlert>() }
                callback(list, null)
            }
            .addOnFailureListener { e ->
                callback(null, e.localizedMessage)
            }
    }
}