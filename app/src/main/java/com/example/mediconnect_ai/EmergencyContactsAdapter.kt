package com.example.mediconnect_ai

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.example.mediconnect_ai.firestore.FirebaseSosContactLogHelper
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EmergencyContactsAdapter(
    contactsList: List<EmergencyContact>,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val onCallClicked: (String) -> Unit,             // phone number
    private val onProfilePhotoClicked: (String) -> Unit      // phone number (used as id)
) : RecyclerView.Adapter<EmergencyContactsAdapter.ContactViewHolder>() {

    private val contacts: MutableList<EmergencyContact> = contactsList.toMutableList()
    private val photoUriMap: MutableMap<String, String> = mutableMapOf()

    class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView? = view.findViewById(R.id.txtName)
        val role: TextView? = view.findViewById(R.id.txtRole)
        val phone: TextView? = view.findViewById(R.id.txtPhone)
        val callIcon: ImageView? = view.findViewById(R.id.iconCall)
        val profilePhoto: ShapeableImageView? = view.findViewById(R.id.ivLogProfilePhoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_emergency_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]

        holder.name?.text = contact.name
        holder.role?.text = contact.role
        holder.phone?.text = contact.phone

        val photoResId = contact.profilePhotoResId ?: R.drawable.ic_person_log

        // load profile photo from map if present, else placeholder
        try {
            val uriString = photoUriMap[contact.phone]
            if (!uriString.isNullOrBlank()) {
                val uri = Uri.parse(uriString)
                holder.profilePhoto?.setImageURI(uri)
                if (holder.profilePhoto?.drawable == null) {
                    holder.profilePhoto?.setImageResource(photoResId)
                }
            } else {
                holder.profilePhoto?.setImageResource(photoResId)
            }
        } catch (e: Exception) {
            holder.profilePhoto?.setImageResource(photoResId)
        }

        // 📞 CALL: save log locally + sync to Firestore, then let Activity perform the call
        holder.callIcon?.setOnClickListener {
            lifecycleScope.launch {
                val newLog = SosLog(
                    contactName = contact.name,
                    contactPhone = contact.phone,
                    actionType = "CALL",
                    profilePhotoResId = photoResId,
                    timestamp = System.currentTimeMillis()
                )

                // 1) Save to Room on IO thread
                withContext(Dispatchers.IO) {
                    try {
                        SosLogDatabase.getDatabase(holder.itemView.context)
                            .sosLogDao()
                            .insertLog(newLog)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }

                // 2) Sync to Firestore
                FirebaseSosContactLogHelper.saveLog(newLog) { success, _ ->
                    // optional: you can show toast/log on failure
                }

                Toast.makeText(
                    holder.itemView.context,
                    "📜 Log saved for ${contact.name}",
                    Toast.LENGTH_SHORT
                ).show()

                // 3) let Activity handle the actual call (permissions etc.)
                onCallClicked(contact.phone)
            }
        }

        holder.profilePhoto?.setOnClickListener {
            onProfilePhotoClicked(contact.phone)
        }

        // 📲 LONG PRESS: WhatsApp log + Firestore + open WhatsApp
        holder.itemView.setOnLongClickListener {
            lifecycleScope.launch {
                val newLog = SosLog(
                    contactName = contact.name,
                    contactPhone = contact.phone,
                    actionType = "WHATSAPP",
                    profilePhotoResId = photoResId,
                    timestamp = System.currentTimeMillis()
                )

                // 1) Save to Room
                withContext(Dispatchers.IO) {
                    try {
                        SosLogDatabase.getDatabase(holder.itemView.context)
                            .sosLogDao()
                            .insertLog(newLog)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }

                // 2) Sync to Firestore
                FirebaseSosContactLogHelper.saveLog(newLog) { success, _ ->
                    // optional: handle failure
                }

                Toast.makeText(
                    holder.itemView.context,
                    "📜 WhatsApp log saved for ${contact.name}",
                    Toast.LENGTH_SHORT
                ).show()

                // 3) Open WhatsApp chat
                sendWhatsAppMessage(holder.itemView.context, contact)
            }
            true
        }
    }

    override fun getItemCount(): Int = contacts.size

    fun setPhotoUriFor(phone: String, uriString: String) {
        photoUriMap[phone] = uriString
        val index = contacts.indexOfFirst { it.phone == phone }
        if (index >= 0) notifyItemChanged(index) else notifyDataSetChanged()
    }

    fun updateContacts(newList: List<EmergencyContact>) {
        contacts.clear()
        contacts.addAll(newList)
        notifyDataSetChanged()
    }

    fun removeContactByPhone(phone: String) {
        val idx = contacts.indexOfFirst { it.phone == phone }
        if (idx >= 0) {
            contacts.removeAt(idx)
            photoUriMap.remove(phone)
            notifyItemRemoved(idx)
        }
    }

    private fun sendWhatsAppMessage(context: Context, contact: EmergencyContact) {
        try {
            val message = Uri.encode(
                "🚨 *Emergency Alert!*\n\n" +
                        "Contacted by: ASHA Worker\n" +
                        "Please assist immediately.\n" +
                        "📞 Phone: ${contact.phone}"
            )
            val cleanNumber = contact.phone.replace("+", "").replace(" ", "")
            val uri = Uri.parse("https://wa.me/$cleanNumber?text=$message")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
        }
    }
}
