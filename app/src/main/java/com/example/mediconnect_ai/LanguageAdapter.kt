package com.example.mediconnect_ai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LanguageAdapter(
    private val languages: List<Language>,
    private var selectedLanguageCode: String,
    private val onLanguageSelected: (Language) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_language, parent, false)
        return LanguageViewHolder(view)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val language = languages[position]
        holder.bind(language, language.code == selectedLanguageCode)
        holder.itemView.setOnClickListener {
            val previousSelectedPosition = languages.indexOfFirst { it.code == selectedLanguageCode }
            selectedLanguageCode = language.code
            notifyItemChanged(previousSelectedPosition)
            notifyItemChanged(holder.adapterPosition)
            onLanguageSelected(language)
        }
    }

    override fun getItemCount(): Int = languages.size

    class LanguageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nativeName: TextView = itemView.findViewById(R.id.language_native_name)
        private val englishName: TextView = itemView.findViewById(R.id.language_english_name)
        private val radioButton: RadioButton = itemView.findViewById(R.id.language_radio_button)

        fun bind(language: Language, isSelected: Boolean) {
            nativeName.text = language.nativeName
            englishName.text = language.englishName
            radioButton.isChecked = isSelected
        }
    }
}
