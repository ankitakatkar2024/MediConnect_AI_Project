package com.example.mediconnect_ai.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mediconnect_ai.R
import com.example.mediconnect_ai.models.EventType
import com.example.mediconnect_ai.models.HistoryEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(private val events: List<HistoryEvent>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvHistoryDate)
        val tvTitle: TextView = view.findViewById(R.id.tvHistoryTitle)
        val tvDesc: TextView = view.findViewById(R.id.tvHistoryDesc)
        val tvStatus: TextView = view.findViewById(R.id.tvHistoryStatus)
        val imgIcon: ImageView = view.findViewById(R.id.imgHistoryIcon)
        val viewLine: View = view.findViewById(R.id.viewTimelineLine)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_card, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val event = events[position]
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        holder.tvDate.text = sdf.format(Date(event.dateTimestamp))
        holder.tvTitle.text = event.title

        // --- UPDATED LOGIC: Cleaner Date Format for LMP ---
        if (event.eventType == EventType.PREGNANCY_START && event.description.contains("GMT")) {
            // Reformat raw Java Date string to "LMP: 11 Jun 2025"
            val formattedLmp = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(event.dateTimestamp))
            holder.tvDesc.text = "LMP: $formattedLmp"
        } else {
            holder.tvDesc.text = event.description
        }
        // --------------------------------------------------

        holder.tvStatus.text = event.status

        // ✅ Use system icons to prevent crashes if custom drawables are missing
        when (event.eventType) {
            EventType.VACCINE -> {
                holder.imgIcon.setImageResource(android.R.drawable.ic_menu_info_details)
                holder.imgIcon.setColorFilter(Color.parseColor("#1976D2"))
            }

            EventType.DELIVERY, EventType.PREGNANCY_START -> {
                holder.imgIcon.setImageResource(android.R.drawable.ic_menu_my_calendar)
                holder.imgIcon.setColorFilter(Color.parseColor("#E91E63"))
            }

            EventType.ANC_VISIT, EventType.PNC_VISIT -> {
                holder.imgIcon.setImageResource(android.R.drawable.ic_menu_agenda)
                holder.imgIcon.setColorFilter(Color.parseColor("#4CAF50"))
            }

            else -> {
                holder.imgIcon.setImageResource(android.R.drawable.ic_menu_view)
                holder.imgIcon.setColorFilter(Color.GRAY)
            }
        }

        // Status color handling
        when {
            event.status.equals("Missed", true) ||
                    event.status.equals("Overdue", true) -> {
                holder.tvStatus.setTextColor(Color.RED)
            }

            event.status.equals("Completed", true) ||
                    event.status.equals("Given", true) -> {
                holder.tvStatus.setTextColor(Color.parseColor("#388E3C"))
            }

            else -> {
                holder.tvStatus.setTextColor(Color.DKGRAY)
            }
        }

        // Timeline line visibility (Hide for last item)
        holder.viewLine.visibility =
            if (position == events.size - 1) View.INVISIBLE else View.VISIBLE
    }

    override fun getItemCount(): Int = events.size
}