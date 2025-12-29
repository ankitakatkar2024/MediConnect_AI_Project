package com.example.mediconnect_ai.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
// FIX: Corrected all import paths to point to the 'fragments' sub-package
import com.example.mediconnect_ai.fragments.CreateAccountFragment
import com.example.mediconnect_ai.fragments.Step2APersonalDetailsFragment
import com.example.mediconnect_ai.fragments.Step2BLocationFragment
import com.example.mediconnect_ai.fragments.WelcomeFragment


class RegistrationPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    // Total number of steps in the registration flow
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        // Now that the imports are correct, the adapter can find each fragment
        return when (position) {
            0 -> WelcomeFragment()
            1 -> Step2APersonalDetailsFragment()
            2 -> Step2BLocationFragment()
            3 -> CreateAccountFragment()
            else -> throw IllegalStateException("Invalid position: $position")
        }
    }
}

