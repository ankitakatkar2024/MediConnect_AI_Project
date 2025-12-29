package com.example.mediconnect_ai.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mediconnect_ai.database.PNCVisit
import com.example.mediconnect_ai.databinding.ItemPncVisitBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PNCVisitAdapter(
    private val onActionClick: (PNCVisit) -> Unit
) : ListAdapter<PNCVisit, PNCVisitAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPncVisitBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemPncVisitBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(visit: PNCVisit) {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

            binding.tvVisitName.text = visit.visitName
            binding.tvDueDate.text = "Due Date: ${sdf.format(Date(visit.dueDate))}"

            // NEW: show description / instructions for ASHA
            binding.tvVisitDescription.text = getDescriptionForVisit(visit)

            val now = System.currentTimeMillis()
            val overdueThreshold = visit.dueDate + (3L * 24 * 60 * 60 * 1000)

            if (visit.isCompleted) {
                binding.cardView.setCardBackgroundColor(Color.parseColor("#E8F5E9"))
                binding.tvStatusBadge.text = "COMPLETED"
                binding.tvStatusBadge.background.setTint(Color.parseColor("#2E7D32"))
                binding.btnMarkDone.visibility = View.GONE
            } else if (now > overdueThreshold) {
                binding.cardView.setCardBackgroundColor(Color.parseColor("#FFEBEE"))
                binding.tvStatusBadge.text = "OVERDUE"
                binding.tvStatusBadge.background.setTint(Color.parseColor("#C62828"))
                binding.btnMarkDone.visibility = View.VISIBLE
            } else if (now >= visit.dueDate - (2L * 24 * 60 * 60 * 1000)) {
                binding.cardView.setCardBackgroundColor(Color.parseColor("#FFFDE7"))
                binding.tvStatusBadge.text = "DUE NOW"
                binding.tvStatusBadge.background.setTint(Color.parseColor("#F9A825"))
                binding.btnMarkDone.visibility = View.VISIBLE
            } else {
                binding.cardView.setCardBackgroundColor(Color.WHITE)
                binding.tvStatusBadge.text = "UPCOMING"
                binding.tvStatusBadge.background.setTint(Color.GRAY)
                binding.btnMarkDone.visibility = View.GONE
            }

            binding.btnMarkDone.setOnClickListener { onActionClick(visit) }
        }

        // NEW: instructions for each PNC visit
        private fun getDescriptionForVisit(visit: PNCVisit): String {
            return when (visit.dayNumber) {

                1 -> "Within 24 hrs: check mother's bleeding, blood pressure, fever, and convulsions; assess baby's breathing, temperature, and breastfeeding."

                3 -> "Day 3: assess cord for pus or foul smell, check for jaundice, and observe feeding; ask mother about weakness, fever, or abnormal bleeding."

                7 -> "Day 7: ensure baby is gaining weight and the cord is healthy or detached; look for fever, fast breathing, or feeding problems."

                14 -> "Day 14: review mother’s recovery and emotional wellbeing, ensure bleeding has reduced; reinforce exclusive breastfeeding."

                21 -> "Day 21: check baby's weight, skin, and general health; assess for cough, fever, or diarrhoea, and check breastfeeding progress."

                28 -> "Day 28: review immunization status, baby’s weight gain, feeding, and any signs of illness or danger."

                42 -> "Day 42: conduct final postnatal check—mother's BP, bleeding, mental health, family planning; evaluate baby's growth and immunizations."

                else -> "Check mother and baby for danger signs, feeding, weight gain, and immunizations; provide appropriate counselling."
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PNCVisit>() {
        override fun areItemsTheSame(oldItem: PNCVisit, newItem: PNCVisit) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PNCVisit, newItem: PNCVisit) =
            oldItem == newItem
    }
}
