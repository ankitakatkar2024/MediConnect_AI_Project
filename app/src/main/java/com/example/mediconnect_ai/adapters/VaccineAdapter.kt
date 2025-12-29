package com.example.mediconnect_ai.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mediconnect_ai.R
import com.example.mediconnect_ai.database.VaccineStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// This adapter is now fully updated to work with the database and interactive UI.
class VaccineAdapter(
    private var vaccineList: MutableList<VaccineStatus>,
    private val listener: OnVaccineStatusUpdateListener
) : RecyclerView.Adapter<VaccineAdapter.VaccineViewHolder>() {

    // Interface to communicate button clicks back to the ImmunizationScheduleActivity
    interface OnVaccineStatusUpdateListener {
        fun onUpdateStatus(vaccineStatus: VaccineStatus)
    }

    class VaccineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: CardView = itemView.findViewById(R.id.card_vaccine_item)
        val vaccineName: TextView = itemView.findViewById(R.id.text_view_vaccine_name)
        val dueDate: TextView = itemView.findViewById(R.id.text_view_due_date)
        val importance: TextView = itemView.findViewById(R.id.text_view_importance)
        val status: TextView = itemView.findViewById(R.id.text_view_status)
        val markCompletedButton: Button = itemView.findViewById(R.id.button_mark_completed)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VaccineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vaccine, parent, false)
        return VaccineViewHolder(view)
    }

    override fun onBindViewHolder(holder: VaccineViewHolder, position: Int) {
        val currentVaccine = vaccineList[position]
        val context = holder.itemView.context

        holder.vaccineName.text = currentVaccine.vaccineName
        holder.importance.text = getImportanceText(currentVaccine.vaccineName)

        val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
        val formattedDate = sdf.format(Date(currentVaccine.dueDate))
        holder.dueDate.text = "Due: $formattedDate"

        val today = Date().time
        var currentStatus = currentVaccine.status

        if (currentStatus == "Scheduled" && currentVaccine.dueDate < today) {
            currentStatus = "Overdue"
        }

        holder.status.text = currentStatus

        // NOTE: If these lines show an error, it means the colors.xml file is not
        // correctly placed or the project needs to be cleaned and rebuilt.
        when (currentStatus) {
            "Completed" -> {
                holder.status.background = ContextCompat.getDrawable(context, R.drawable.status_background_completed)
                holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.status_completed_bg))
                holder.markCompletedButton.visibility = View.GONE
            }
            "Overdue" -> {
                holder.status.background = ContextCompat.getDrawable(context, R.drawable.status_background_overdue)
                holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.status_overdue_bg))
                holder.markCompletedButton.visibility = View.VISIBLE
            }
            else -> { // "Scheduled"
                holder.status.background = ContextCompat.getDrawable(context, R.drawable.status_background_scheduled)
                holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
                holder.markCompletedButton.visibility = View.VISIBLE
            }
        }

        holder.markCompletedButton.setOnClickListener {
            listener.onUpdateStatus(currentVaccine)
        }
    }

    override fun getItemCount() = vaccineList.size

    fun updateVaccineList(newVaccineList: List<VaccineStatus>) {
        vaccineList.clear()
        vaccineList.addAll(newVaccineList)
        notifyDataSetChanged()
    }

    private fun getImportanceText(vaccineName: String): String {
        return when {
            vaccineName.contains("BCG") -> "Prevents tuberculosis."
            vaccineName.contains("Hepatitis B") -> "Fights liver infections."
            vaccineName.contains("OPV") -> "Protects against polio."
            vaccineName.contains("Pentavalent") -> "Protects against diphtheria, pertussis, tetanus, polio, hepatitis B, pneumonia, and diarrheal diseases."
            vaccineName.contains("Rotavirus") -> "Protects against rotavirus diarrhea."
            vaccineName.contains("IPV") -> "Reinforces protection against polio."
            vaccineName.contains("Measles/MR") -> "Measles can be fatal in infants; rubella causes birth defects."
            vaccineName.contains("JE") -> "Protects against Japanese Encephalitis."
            vaccineName.contains("Vitamin A") -> "Prevents vitamin A deficiency."
            vaccineName.contains("DPT") -> "Important booster to prolong childhood protection."
            vaccineName.contains("TT") -> "Immunity reinforcement."
            else -> "Protects against childhood diseases."
        }
    }
}

