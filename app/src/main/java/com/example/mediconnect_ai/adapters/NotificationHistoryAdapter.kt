package com.example.mediconnect_ai.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mediconnect_ai.R
import com.example.mediconnect_ai.database.NotificationHistory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationHistoryAdapter(private val notifications: List<NotificationHistory>) :
    RecyclerView.Adapter<NotificationHistoryAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvNotificationTitle)
        val message: TextView = itemView.findViewById(R.id.tvNotificationMessage)
        val timestamp: TextView = itemView.findViewById(R.id.tvNotificationTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification_history, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.title.text = notification.title
        holder.message.text = notification.message
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        holder.timestamp.text = sdf.format(Date(notification.timestamp))
    }

    override fun getItemCount() = notifications.size
}