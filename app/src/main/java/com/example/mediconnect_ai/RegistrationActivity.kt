package com.example.mediconnect_ai

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.example.mediconnect_ai.adapters.RegistrationPagerAdapter
import com.example.mediconnect_ai.databinding.ActivityRegistrationBinding

// --- FIX: Extends your BaseActivity, which now correctly extends AppCompatActivity ---
class RegistrationActivity : BaseActivity() {

    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var pagerAdapter: RegistrationPagerAdapter

    // Initialize the shared ViewModel
    val viewModel: RegistrationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // This will no longer crash
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pagerAdapter = RegistrationPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.isUserInputEnabled = false // Disable swiping between fragments

        // Set the progress bar's max steps dynamically from the adapter
        binding.registrationProgress.max = pagerAdapter.itemCount
        binding.registrationProgress.progress = 1 // Start at step 1
    }

    // This function is called by fragments to move to the next page
    fun goToNextStep() {
        val currentItem = binding.viewPager.currentItem
        if (currentItem < pagerAdapter.itemCount - 1) {
            binding.viewPager.setCurrentItem(currentItem + 1, true)
            binding.registrationProgress.progress = currentItem + 2 // Update progress
        }
    }

    // This function is called by fragments to move to the previous page
    fun goToPreviousStep() {
        val currentItem = binding.viewPager.currentItem
        if (currentItem > 0) {
            binding.viewPager.setCurrentItem(currentItem - 1, true)
            binding.registrationProgress.progress = currentItem // Update progress
        }
    }

    // This function is called by the final fragment to complete the process
    fun completeRegistration() {
        // After the final step, navigate to the Login screen
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

