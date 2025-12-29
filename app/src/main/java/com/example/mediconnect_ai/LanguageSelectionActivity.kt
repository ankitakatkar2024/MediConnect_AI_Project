package com.example.mediconnect_ai

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LanguageSelectionActivity : BaseActivity() { // ✅ Changed to BaseActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_selection)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val currentLanguageCode = intent.getStringExtra("CURRENT_LANG_CODE") ?: "en"

        // ✅ Get the list from the single source of truth: LanguageList
        val languages = LanguageList.languages

        val recyclerView: RecyclerView = findViewById(R.id.language_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = LanguageAdapter(languages, currentLanguageCode) { selectedLanguage ->
            val resultIntent = Intent()
            resultIntent.putExtra("SELECTED_LANG_CODE", selectedLanguage.code)
            resultIntent.putExtra("SELECTED_LANG_NAME", selectedLanguage.nativeName)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
        recyclerView.adapter = adapter
    }
}
