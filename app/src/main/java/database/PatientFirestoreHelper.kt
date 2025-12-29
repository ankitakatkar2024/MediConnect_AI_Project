package com.example.mediconnect_ai.firestore

import com.example.mediconnect_ai.database.Patient
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

object FirebasePatientHelper {

    private val db = FirebaseFirestore.getInstance()

    // ✔ Use the correct collection name: "patients"
    private val collectionRef = db.collection("patients")

    // Save or update a patient in Firestore
    fun savePatient(patient: Patient, callback: (success: Boolean, error: String?) -> Unit) {
        val docRef = collectionRef.document(patient.id.toString())
        docRef.set(patient)
            .addOnSuccessListener { callback(true, null) }
            .addOnFailureListener { e -> callback(false, e.localizedMessage) }
    }

    // Fetch all patients from Firestore
    fun fetchPatients(callback: (List<Patient>?, String?) -> Unit) {
        collectionRef.get()
            .addOnSuccessListener { snapshot ->
                val patients = snapshot.documents.mapNotNull { it.toObject<Patient>() }
                callback(patients, null)
            }
            .addOnFailureListener { e ->
                callback(null, e.localizedMessage)
            }
    }

    // Fetch a single patient by ID
    fun fetchPatientById(patientId: Long, callback: (Patient?, String?) -> Unit) {
        collectionRef.document(patientId.toString())
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val patient = doc.toObject<Patient>()
                    callback(patient, null)
                } else {
                    callback(null, "Patient not found")
                }
            }
            .addOnFailureListener { e ->
                callback(null, e.localizedMessage)
            }
    }
}
