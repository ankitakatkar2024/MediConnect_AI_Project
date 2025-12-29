package com.example.mediconnect_ai

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mediconnect_ai.database.TBVisit
import java.text.SimpleDateFormat
import java.util.*

class TBVisitAdapter(
    private val visits: List<TBVisit>,
    private val onItemClick: (TBVisit) -> Unit
) : RecyclerView.Adapter<TBVisitAdapter.TBVisitViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TBVisitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tb_visit, parent, false)
        return TBVisitViewHolder(view)
    }

    override fun onBindViewHolder(holder: TBVisitViewHolder, position: Int) {
        holder.bind(visits[position])
    }

    override fun getItemCount(): Int = visits.size

    inner class TBVisitViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val tvVisitType: TextView = itemView.findViewById(R.id.tvVisitType)
        private val tvPhase: TextView = itemView.findViewById(R.id.tvPhase)
        private val tvDueDate: TextView = itemView.findViewById(R.id.tvDueDate)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvDescription: TextView =
            itemView.findViewById(R.id.tvDescription)



        fun bind(visit: TBVisit) {

            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val today = System.currentTimeMillis()

            tvVisitType.text = visit.visitType
            tvPhase.text = "Phase: ${visit.treatmentPhase}"
            tvDueDate.text = "Due: ${sdf.format(Date(visit.dueDate))}"

            // ---------------- STATUS & COLOR LOGIC ----------------
            when {
                visit.needsReferral -> {
                    tvStatus.text = "Status: Needs Referral"
                    tvStatus.setTextColor(Color.parseColor("#D32F2F")) // Red
                }

                visit.isCompleted -> {
                    tvStatus.text = "Status: Completed"
                    tvStatus.setTextColor(Color.parseColor("#388E3C")) // Green
                }

                visit.dueDate < today -> {
                    tvStatus.text = "Status: Overdue"
                    tvStatus.setTextColor(Color.parseColor("#C62828")) // Dark Red
                }

                else -> {
                    tvStatus.text = "Status: Upcoming"
                    tvStatus.setTextColor(Color.parseColor("#F57C00")) // Orange
                }
            }

            // ---------------- DESCRIPTION ----------------
            if (visit.description.isNotBlank()) {
                tvDescription.text = visit.description
                tvDescription.visibility = View.VISIBLE
            } else {
                tvDescription.visibility = View.GONE
            }

            // ---------------- CLICK BEHAVIOR ----------------
            itemView.setOnClickListener {
                if (!visit.isCompleted) {
                    onItemClick(visit)
                }
            }
        }
    }
}
