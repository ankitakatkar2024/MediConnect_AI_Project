package com.example.mediconnect_ai

import android.content.Intent
import android.os.Bundle
import com.example.mediconnect_ai.databinding.ActivityResourcesBinding

class ResourcesActivity : BaseActivity() {

    // The binding object gives you direct access to the views in your XML layout.
    private lateinit var binding: ActivityResourcesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using View Binding and set it as the screen content.
        binding = ActivityResourcesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup the toolbar using the ID from the XML file.
        setSupportActionBar(binding.toolbarResources)
        // This adds the back arrow (<-) to the toolbar.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Health Resources" // Set the title for the screen

        // A clean way to organize the click listeners.
        setupClickListeners()
    }

    /**
     * Sets up the click listeners for all the interactive cards on the screen.
     */
    private fun setupClickListeners() {
        binding.cardMaternalHealth.setOnClickListener {
            startActivity(Intent(this, MaternalHealthActivity::class.java))
        }

        binding.cardChildHealth.setOnClickListener {
            startActivity(Intent(this, ChildHealthActivity::class.java))
        }

        binding.cardImmunizationInfo.setOnClickListener {
            startActivity(Intent(this, ImmunizationInfoActivity::class.java))
        }

        binding.cardGovSchemes.setOnClickListener {
            startActivity(Intent(this, GovSchemesActivity::class.java))
        }
    }

    /**
     * This function is called when the user presses the back arrow in the toolbar.
     * It simply navigates the user back to the previous screen.
     */
    override fun onSupportNavigateUp(): Boolean {
        // This is a modern way to handle the back press.
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}