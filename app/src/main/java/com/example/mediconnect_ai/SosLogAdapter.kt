package com.example.mediconnect_ai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView // ✅ Import ShapeableImageView
import java.text.SimpleDateFormat
import java.util.*

class SosLogAdapter(private val logs: List<SosLog>) :
    RecyclerView.Adapter<SosLogAdapter.LogViewHolder>() {

    class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // ✅ Add the ShapeableImageView property
        val contactPhoto: ShapeableImageView = view.findViewById(R.id.ivLogProfilePhoto)
        val contactName: TextView = view.findViewById(R.id.tvLogContactName)
        val contactPhone: TextView = view.findViewById(R.id.tvLogPhone)
        val actionType: TextView = view.findViewById(R.id.tvLogAction)
        val timestamp: TextView = view.findViewById(R.id.tvLogTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sos_log, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = logs[position]

        // ✅ Set the profile photo if available, otherwise use a default
        if (log.profilePhotoResId != null) {
            holder.contactPhoto.setImageResource(log.profilePhotoResId)
        } else {
            // The background and tint on the ShapeableImageView in your XML will give the circular teal background and the white person icon.
            // You can set the image resource to null or a default resource if a profile photo isn't available.
            holder.contactPhoto.setImageResource(R.drawable.ic_person_log)
        }

        holder.contactName.text = log.contactName
        holder.contactPhone.text = log.contactPhone
        holder.actionType.text = "Action: ${log.actionType}"

        // ✅ Convert timestamp (Long) to readable string
        holder.timestamp.text = formatTimestamp(log.timestamp)
    }

    override fun getItemCount(): Int = logs.size

    // ✅ Helper function to format timestamp nicely
    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}