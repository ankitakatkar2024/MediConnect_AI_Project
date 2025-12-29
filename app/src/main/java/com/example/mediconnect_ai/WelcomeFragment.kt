package com.example.mediconnect_ai.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mediconnect_ai.RegistrationActivity
import com.example.mediconnect_ai.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using View Binding
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the click listener on the "Register Now" button
        binding.registerNowButton.setOnClickListener {
            // This is the correct way for a fragment to ask its host activity to perform an action.
            (activity as? RegistrationActivity)?.goToNextStep()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

