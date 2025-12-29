package com.example.mediconnect_ai.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.mediconnect_ai.RegistrationActivity
import com.example.mediconnect_ai.RegistrationViewModel
import com.example.mediconnect_ai.databinding.FragmentStep2aPersonalDetailsBinding
import com.google.android.material.button.MaterialButton

class Step2APersonalDetailsFragment : Fragment() {

    private var _binding: FragmentStep2aPersonalDetailsBinding? = null
    private val binding get() = _binding!!

    // Shared ViewModel
    private val viewModel: RegistrationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStep2aPersonalDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRoleDropdown()
        setupNavigationButtons()
    }

    private fun setupRoleDropdown() {
        val roles = listOf("ASHA Worker", "Doctor", "Patient")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, roles)
        binding.roleAutoComplete.setAdapter(adapter)
    }

    private fun setupNavigationButtons() {
        binding.nextButton.setOnClickListener {
            if (validateAndSaveData()) {
                (activity as? RegistrationActivity)?.goToNextStep()
            }
        }
        binding.backButton.setOnClickListener {
            (activity as? RegistrationActivity)?.goToPreviousStep()
        }
    }

    private fun validateAndSaveData(): Boolean {
        var isFormValid = true

        val fullName = binding.fullNameEditText.text.toString().trim()
        val age = binding.ageEditText.text.toString().trim()
        val governmentId = binding.idEditText.text.toString().trim()
        val role = binding.roleAutoComplete.text.toString().trim()
        val genderId = binding.genderToggleGroup.checkedButtonId

        val gender = if (genderId != -1) {
            val checkedButton: MaterialButton = binding.root.findViewById(genderId)
            checkedButton.text.toString()
        } else null

        // Validations
        if (fullName.isEmpty()) {
            binding.fullNameLayout.error = "Full Name is required"
            isFormValid = false
        } else binding.fullNameLayout.error = null

        if (age.isEmpty()) {
            binding.ageLayout.error = "Age is required"
            isFormValid = false
        } else binding.ageLayout.error = null

        if (gender == null) {
            Toast.makeText(requireContext(), "Please select a gender", Toast.LENGTH_SHORT).show()
            isFormValid = false
        }

        if (governmentId.isEmpty()) {
            binding.idLayout.error = "ID is required"
            isFormValid = false
        } else binding.idLayout.error = null

        if (role.isEmpty()) {
            binding.roleLayout.error = "Role is required"
            isFormValid = false
        } else binding.roleLayout.error = null

        val qualification = binding.qualificationEditText.text.toString().trim()
        val experience = binding.experienceEditText.text.toString().trim()

        if (isFormValid) {
            // Normalize role before saving
            val normalizedRole = when (role) {
                "Doctor" -> "doctor"
                "Patient" -> "patient_family"
                "ASHA Worker" -> "asha_anm_nurse"
                else -> role.lowercase()
            }

            viewModel.fullName = fullName
            viewModel.age = age
            viewModel.gender = gender
            viewModel.governmentId = governmentId
            viewModel.role = normalizedRole
            viewModel.qualification = qualification
            viewModel.experience = experience
        }

        return isFormValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
