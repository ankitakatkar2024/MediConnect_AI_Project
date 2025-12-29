package com.example.mediconnect_ai.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mediconnect_ai.R
import com.example.mediconnect_ai.database.Patient
import java.util.Calendar

class ReferredPatientAdapter(
    private val onProfileClick: (Patient) -> Unit,
    private val onResolveClick: (Patient) -> Unit
) : ListAdapter<Patient, ReferredPatientAdapter.ReferredPatientViewHolder>(PatientDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReferredPatientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_referred_patient, parent, false)
        return ReferredPatientViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReferredPatientViewHolder, position: Int) {
        val patient = getItem(position)
        holder.bind(patient, onProfileClick, onResolveClick)
    }

    class ReferredPatientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // IDs matched to your item_referred_patient.xml
        private val patientPhoto: ImageView = itemView.findViewById(R.id.item_patient_avatar)
        private val patientName: TextView = itemView.findViewById(R.id.item_patient_name)
        private val patientMeta: TextView = itemView.findViewById(R.id.item_patient_meta)    // age • gender
        private val referralReason: TextView = itemView.findViewById(R.id.item_reason)
        private val btnViewProfile: MaterialButton = itemView.findViewById(R.id.btn_view_profile)
        private val btnResolveCase: MaterialButton = itemView.findViewById(R.id.btn_resolve)
        private val itemStatus: TextView? = itemView.findViewById(R.id.item_status)

        fun bind(patient: Patient, onProfileClick: (Patient) -> Unit, onResolveClick: (Patient) -> Unit) {
            // Name
            patientName.text = patient.fullName ?: "—"

            // Age calculation (assumes patient.dob is epoch millis)
            val dobCalendar = Calendar.getInstance().apply {
                try { timeInMillis = patient.dob } catch (_: Exception) { timeInMillis = 0L }
            }
            val today = Calendar.getInstance()
            var ageInYears = today.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR)
            if (dobCalendar.timeInMillis != 0L && today.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
                ageInYears--
            }
            val genderText = patient.gender ?: "-"
            patientMeta.text = if (dobCalendar.timeInMillis != 0L) "Age: $ageInYears • $genderText" else genderText

            // Referral reason / item_reason
            referralReason.text = patient.referralReason ?: itemView.context.getString(R.string.no_reason_provided)

            // Status (if present in layout)
            try {
                val statusText = when {
                    // try reflection for common fields (status/isResolved) but safely
                    patient.javaClass.getDeclaredFields().any { it.name == "status" } -> {
                        val f = patient.javaClass.getDeclaredField("status")
                        f.isAccessible = true
                        (f.get(patient) as? String) ?: ""
                    }
                    patient.javaClass.getDeclaredFields().any { it.name == "isResolved" } -> {
                        val f = patient.javaClass.getDeclaredField("isResolved")
                        f.isAccessible = true
                        val v = f.get(patient)
                        if (v is Boolean) if (v) "Resolved" else "Pending" else ""
                    }
                    else -> ""
                }
                if (!statusText.isNullOrBlank()) itemStatus?.text = statusText
            } catch (_: Exception) {
                // ignore reflection errors, status stays as defined in XML
            }

            // Photo placeholder
            patientPhoto.setImageResource(R.drawable.ic_patient) // matches your layout icon

            // Click listeners
            btnViewProfile.setOnClickListener { onProfileClick(patient) }
            btnResolveCase.setOnClickListener { onResolveClick(patient) }
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
