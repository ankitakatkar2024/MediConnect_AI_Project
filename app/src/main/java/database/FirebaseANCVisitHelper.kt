package com.example.mediconnect_ai.firestore

import android.util.Log
import com.example.mediconnect_ai.database.ANCVisit
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

object FirebaseANCVisitHelper {

    private const val TAG = "FirebaseANCVisitHelper"

    private val db = FirebaseFirestore.getInstance()

    // Save or update a single ANC visit under a pregnancy
    fun saveVisit(
        visit: ANCVisit,
        pregnancyId: Long,
        callback: (Boolean, String?) -> Unit
    ) {
        val visitRef = db.collection("pregnancies")
            .document(pregnancyId.toString())
            .collection("ancVisits")
            .document(visit.id.toString())

        visitRef.set(visit)
            .addOnSuccessListener {
                Log.d(
                    TAG,
                    "ANC visit synced: pregnancyId=$pregnancyId, visitId=${visit.id}, name=${visit.visitName}"
                )
                callback(true, null)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to sync ANC visit: ${e.localizedMessage}")
                callback(false, e.localizedMessage)
            }
    }

    // Optional: fetch all visits for a pregnancy from Firestore
    fun fetchVisitsForPregnancy(
        pregnancyId: Long,
        callback: (List<ANCVisit>?, String?) -> Unit
    ) {
        db.collection("pregnancies")
            .document(pregnancyId.toString())
            .collection("ancVisits")
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { it.toObject<ANCVisit>() }
                callback(list, null)
            }
            .addOnFailureListener { e ->
                callback(null, e.localizedMessage)
            }
    }
}
