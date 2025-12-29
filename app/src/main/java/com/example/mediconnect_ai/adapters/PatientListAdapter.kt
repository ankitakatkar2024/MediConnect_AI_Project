package com.example.mediconnect_ai.adapters

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mediconnect_ai.PatientDetailActivity
import com.example.mediconnect_ai.R
import com.example.mediconnect_ai.database.Patient
import com.example.mediconnect_ai.utils.AgeUtils

class PatientListAdapter :
    ListAdapter<Patient, PatientListAdapter.PatientViewHolder>(PatientDiffCallback()) {

    // ✅ STEP 1: Family count map
    private var familyCountMap: Map<String, Int> = emptyMap()

    // ✅ STEP 2: Calculate family sizes whenever list updates
    override fun submitList(list: List<Patient>?) {
        familyCountMap = list
            ?.filter { !it.familyId.isNullOrEmpty() }
            ?.groupingBy { it.familyId!! }
            ?.eachCount()
            ?: emptyMap()

        super.submitList(list)
    }

    class PatientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFamilyHeader: TextView = itemView.findViewById(R.id.tvFamilyHeader)
        val nameTextView: TextView = itemView.findViewById(R.id.tvPatientName)
        val detailsTextView: TextView = itemView.findViewById(R.id.tvPatientDetails)
        val shareButton: ImageButton? = itemView.findViewById(R.id.btnShare)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient_grouped, parent, false)
        return PatientViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val currentPatient = getItem(position)
        val context = holder.itemView.context

        // ================= FAMILY GROUPING LOGIC =================

        val previousPatient = if (position > 0) getItem(position - 1) else null

        val familyId = currentPatient.familyId
        val familySize = familyId?.let { familyCountMap[it] } ?: 0

        // ✅ Show header ONLY if:
        // - familyId exists
        // - more than 1 patient in same family
        // - first patient OR familyId changed
        val showHeader =
            familyId != null &&
                    familySize > 1 &&
                    (previousPatient == null || previousPatient.familyId != familyId)

        if (showHeader) {
            holder.tvFamilyHeader.visibility = View.VISIBLE
            holder.tvFamilyHeader.text = "Family ID: $familyId"
            holder.tvFamilyHeader.setBackgroundColor(Color.parseColor("#5C6BC0"))
        } else {
            holder.tvFamilyHeader.visibility = View.GONE
        }

        // ================= PATIENT CARD DATA =================

        val ageFormatted = AgeUtils.getFullAge(currentPatient.dob)

        holder.nameTextView.text = currentPatient.fullName
        holder.detailsTextView.text =
            "Age: $ageFormatted, Gender: ${currentPatient.gender}"

        // Open Patient Details
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PatientDetailActivity::class.java).apply {
                putExtra(PatientDetailActivity.EXTRA_PATIENT_ID, currentPatient.id)
            }
            context.startActivity(intent)
        }

        // Share Patient Record
        holder.shareButton?.setOnClickListener {
            val shareText = """
                Patient Record:
                Name: ${currentPatient.fullName}
                Age: $ageFormatted
                Gender: ${currentPatient.gender}
                Contact: ${currentPatient.contactNumber.ifEmpty { "N/A" }}
            """.trimIndent()

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(
                Intent.createChooser(shareIntent, "Share Patient Record")
            )
        }
    }

    class PatientDiffCallback : DiffUtil.ItemCallback<Patient>() {
        override fun areItemsTheSame(oldItem: Patient, newItem: Patient): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Patient, newItem: Patient): Boolean {
            return oldItem == newItem
        }
    }
}
