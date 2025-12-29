package com.example.mediconnect_ai.firestore

import android.util.Log
import com.example.mediconnect_ai.database.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

object FirebaseTaskHelper {

    private const val TAG = "FirebaseTaskHelper"

    private val db = FirebaseFirestore.getInstance()
    private val collectionRef = db.collection("tasks")

    // Save or update a task
    fun saveTask(task: Task) {
        val docId = task.id.toString()
        collectionRef.document(docId)
            .set(task)
            .addOnSuccessListener {
                Log.d(TAG, "Task synced to cloud. id=$docId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to sync task id=$docId: ${e.localizedMessage}")
            }
    }

    // Optional: fetch all tasks from cloud (for future use)
    fun fetchTasks(callback: (List<Task>?, String?) -> Unit) {
        collectionRef.get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { it.toObject<Task>() }
                callback(list, null)
            }
            .addOnFailureListener { e ->
                callback(null, e.localizedMessage)
            }
    }
}
