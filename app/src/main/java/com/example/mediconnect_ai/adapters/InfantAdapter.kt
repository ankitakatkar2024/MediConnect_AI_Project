package com.example.mediconnect_ai.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mediconnect_ai.database.Patient
import com.example.mediconnect_ai.databinding.ItemInfantBinding
import com.example.mediconnect_ai.utils.AgeUtils
import java.text.SimpleDateFormat
import java.util.Locale

class InfantAdapter(private val onItemClicked: (Patient) -> Unit) :
    ListAdapter<Patient, InfantAdapter.InfantViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfantViewHolder {
        val binding = ItemInfantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InfantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InfantViewHolder, position: Int) {
        val current = getItem(position)
        holder.itemView.setOnClickListener {
            onItemClicked(current)
        }
        holder.bind(current)
    }

    class InfantViewHolder(private val binding: ItemInfantBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(patient: Patient) {
            binding.infantNameTextView.text = patient.fullName
            binding.infantAgeTextView.text = "Age: ${AgeUtils.getFullAge(patient.dob)}"
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Patient>() {
            override fun areItemsTheSame(oldItem: Patient, newItem: Patient): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Patient, newItem: Patient): Boolean {
                return oldItem == newItem
            }
        }
    }
}
