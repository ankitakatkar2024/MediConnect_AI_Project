package com.example.mediconnect_ai

import android.os.Bundle
import androidx.core.text.HtmlCompat
import com.example.mediconnect_ai.databinding.ActivityDetailBinding

class ImmunizationInfoActivity : BaseActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This correctly inflates your reusable detail layout
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- UPDATED LOGIC FOR COLLAPSING TOOLBAR ---
        // 1. Set the support action bar to your new toolbar
        setSupportActionBar(binding.toolbarDetail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 2. Set the title on the CollapsingToolbarLayout for the collapsing effect
        val pageTitle = getString(R.string.resources_immunization_title)
        binding.toolbarLayout.title = pageTitle
        // --- END OF UPDATE ---

        // 3. Set the unique header image, title, and content for this page
        binding.headerImage.setImageResource(R.drawable.ic_immunization_header)
        binding.tvDetailTitle.text = pageTitle

        // This correctly formats the text from your strings.xml
        val formattedText = HtmlCompat.fromHtml(
            getString(R.string.content_immunization_info),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        binding.tvDetailContent.text = formattedText
    }

    // This function handles the click of the back arrow in the toolbar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

