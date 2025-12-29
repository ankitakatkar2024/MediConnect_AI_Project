package com.example.mediconnect_ai.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.mediconnect_ai.RegistrationActivity
import com.example.mediconnect_ai.RegistrationViewModel
import com.example.mediconnect_ai.databinding.FragmentCreateAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CreateAccountFragment : Fragment() {

    private var _binding: FragmentCreateAccountBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RegistrationViewModel by activityViewModels()

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth

        binding.createAccountButton.setOnClickListener {
            if (validateInput()) {
                registerUser()
            }
        }
    }

    private fun registerUser() {
        binding.createAccountButton.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    firebaseUser?.let {
                        saveUserDataToFirestore(it.uid)
                    }
                } else {
                    Log.w("CreateAccountFragment", "Auth failed", task.exception)
                    Toast.makeText(
                        requireContext(),
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()

                    binding.progressBar.visibility = View.GONE
                    binding.createAccountButton.isEnabled = true
                }
            }
    }

    private fun saveUserDataToFirestore(userId: String) {
        // 🆕 Generate short, human-readable ID
        val rolePrefix = when (viewModel.role?.lowercase()) {
            "doctor" -> "DOC"
            "asha_anm_nurse" -> "ASHA"
            "patient_family" -> "PAT"
            else -> "USR"
        }
        val shortId = "$rolePrefix${System.currentTimeMillis().toString().takeLast(4)}"

        val user = hashMapOf(
            "customId" to shortId, // 🆕 Save the readable ID
            "fullName" to viewModel.fullName,
            "age" to viewModel.age,
            "gender" to viewModel.gender,
            "governmentId" to viewModel.governmentId,
            "role" to viewModel.role,
            "qualification" to viewModel.qualification,
            "experience" to viewModel.experience,
            "state" to viewModel.state,
            "district" to viewModel.district,
            "phc" to viewModel.phc,
            "supervisor" to viewModel.supervisor,
            "mobile" to viewModel.mobile,
            "altContact" to viewModel.altContact,
            "email" to binding.emailEditText.text.toString().trim()
        )

        db.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Account created successfully!", Toast.LENGTH_SHORT).show()

                // ✅ Save also in SharedPreferences
                val prefs = requireActivity().getSharedPreferences("MediConnectPrefs", Context.MODE_PRIVATE)
                prefs.edit().apply {
                    putString("USER_ID", shortId) // 🆕 Use short readable ID
                    putString("USER_NAME", viewModel.fullName)
                    putString("USER_ROLE", viewModel.role)
                    putString("ASSIGNED_AREA", viewModel.district ?: "Unassigned")
                    putBoolean("IS_LOGGED_IN", true)
                }.apply()

                (activity as? RegistrationActivity)?.completeRegistration()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error saving user data: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                binding.createAccountButton.isEnabled = true
            }
    }


    private fun validateInput(): Boolean {
        var isValid = true
        binding.emailInputLayout.error = null
        binding.passwordInputLayout.error = null
        binding.confirmPasswordInputLayout.error = null

        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString()
        val confirmPassword = binding.confirmPasswordEditText.text.toString()

        if (email.isEmpty()) {
            binding.emailInputLayout.error = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.error = "Enter a valid email"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.passwordInputLayout.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            binding.passwordInputLayout.error = "At least 6 characters required"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordInputLayout.error = "Confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            binding.confirmPasswordInputLayout.error = "Passwords do not match"
            isValid = false
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
