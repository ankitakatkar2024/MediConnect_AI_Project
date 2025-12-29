package com.example.mediconnect_ai.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.mediconnect_ai.RegistrationActivity
import com.example.mediconnect_ai.RegistrationViewModel
import com.example.mediconnect_ai.databinding.FragmentStep2bLocationBinding
import org.json.JSONObject
import java.io.InputStream

class Step2BLocationFragment : Fragment() {

    private var _binding: FragmentStep2bLocationBinding? = null
    private val binding get() = _binding!!

    // Shared ViewModel
    private val viewModel: RegistrationViewModel by activityViewModels()

    private lateinit var statesList: List<String>
    private lateinit var districtsByState: Map<String, List<String>>

    companion object {
        private const val TAG = "ValidationDebug"
        private const val PREFS_NAME = "MediConnectPrefs"
        private const val KEY_USER_CONTACT = "USER_CONTACT"
        private const val KEY_USER_NAME = "USER_NAME"
        private const val KEY_USER_AREA = "USER_AREA"
        private const val KEY_USER_ROLE_DISPLAY = "USER_ROLE_DISPLAY"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStep2bLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadStatesAndDistricts()

        // State adapter
        val stateAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, statesList)
        binding.stateAutoComplete.setAdapter(stateAdapter)

        binding.districtLayout.isEnabled = false

        // When state is selected -> populate districts
        binding.stateAutoComplete.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val selectedState = statesList[position]
                val districts = districtsByState[selectedState].orEmpty()

                val districtAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    districts
                )
                binding.districtAutoComplete.setAdapter(districtAdapter)
                binding.districtAutoComplete.setText("", false)
                binding.districtLayout.isEnabled = districts.isNotEmpty()

                // Clear errors
                binding.stateLayout.error = null
                binding.districtLayout.error = null
            }

        // Back button
        binding.backButton.setOnClickListener {
            (activity as? RegistrationActivity)?.goToPreviousStep()
        }

        // Next button
        binding.nextButton.setOnClickListener {
            if (validateAndSaveData()) {
                (activity as? RegistrationActivity)?.goToNextStep()
            }
        }

        // Show supervisor only for ASHA workers
        binding.supervisorLayout.visibility =
            if ("ASHA Worker".equals(viewModel.role, ignoreCase = true)) View.VISIBLE
            else View.GONE
    }

    private fun loadStatesAndDistricts() {
        val jsonString = loadJSONFromAsset("states-and-districts.json")
        val jsonObject = JSONObject(jsonString)
        val jsonArray = jsonObject.getJSONArray("states")

        val states = mutableListOf<String>()
        val map = mutableMapOf<String, List<String>>()

        for (i in 0 until jsonArray.length()) {
            val stateObj = jsonArray.getJSONObject(i)
            val stateName = stateObj.getString("state")
            val districtsArray = stateObj.getJSONArray("districts")
            val districts = MutableList(districtsArray.length()) { idx ->
                districtsArray.getString(idx)
            }
            states.add(stateName)
            map[stateName] = districts
        }

        statesList = states
        districtsByState = map
    }

    private fun loadJSONFromAsset(fileName: String): String {
        return try {
            val inputStream: InputStream = requireContext().assets.open(fileName)
            inputStream.bufferedReader().use { it.readText() }
        } catch (ex: Exception) {
            ex.printStackTrace()
            "{}"
        }
    }

    private fun validateAndSaveData(): Boolean {
        var isFormValid = true

        val state = binding.stateAutoComplete.text.toString().trim()
        val district = binding.districtAutoComplete.text.toString().trim()
        val phc = binding.phcAutoComplete.text.toString().trim()
        val supervisor = binding.supervisorEditText.text?.toString()?.trim().orEmpty()
        val mobile = binding.mobileEditText.text?.toString()?.trim().orEmpty()
        val email = binding.emailEditText.text?.toString()?.trim().orEmpty()
        val altContact = binding.altContactEditText.text?.toString()?.trim().orEmpty()

        Log.d(TAG, "Validating form for role: ${viewModel.role}")

        // State Validation
        if (state.isEmpty()) {
            binding.stateLayout.error = "State is required"
            Log.d(TAG, "State validation FAILED: is empty")
            isFormValid = false
        } else binding.stateLayout.error = null

        // District Validation
        if (district.isEmpty()) {
            binding.districtLayout.error = "District is required"
            Log.d(TAG, "District validation FAILED: is empty")
            isFormValid = false
        } else binding.districtLayout.error = null

        // PHC Validation
        if (phc.isEmpty()) {
            binding.phcLayout.error = "PHC / Hospital is required"
            Log.d(TAG, "PHC validation FAILED: is empty")
            isFormValid = false
        } else binding.phcLayout.error = null

        // Supervisor validation (only for ASHA Worker) — optional
        if ("ASHA Worker".equals(viewModel.role, ignoreCase = true)) {
            binding.supervisorLayout.error = null
        }

        // Mobile validation
        if (mobile.isEmpty()) {
            binding.mobileLayout.error = "Mobile number is required"
            Log.d(TAG, "Mobile validation FAILED: is empty")
            isFormValid = false
        } else if (mobile.length != 10) {
            binding.mobileLayout.error = "Enter a valid 10-digit number"
            Log.d(TAG, "Mobile validation FAILED: length is ${mobile.length}, not 10")
            isFormValid = false
        } else binding.mobileLayout.error = null

        // Email validation (optional)
        if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = "Enter a valid email"
            Log.d(TAG, "Email validation FAILED: invalid format")
            isFormValid = false
        } else binding.emailLayout.error = null

        // Alt contact validation (optional)
        if (altContact.isNotEmpty() && altContact.length != 10) {
            binding.altContactLayout.error = "Enter a valid 10-digit number"
            Log.d(TAG, "Alt Contact validation FAILED: length is ${altContact.length}, not 10")
            isFormValid = false
        } else binding.altContactLayout.error = null

        if (isFormValid) {
            Log.d(TAG, "Validation PASSED. Saving data to ViewModel.")

            // Save values to shared ViewModel
            viewModel.state = state
            viewModel.district = district
            viewModel.phc = phc
            viewModel.supervisor = supervisor
            viewModel.mobile = mobile
            viewModel.email = email
            viewModel.altContact = altContact

            // --- SAVE to SharedPreferences so dashboard can read phone/name/area/role ---
            val prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(KEY_USER_CONTACT, mobile)                      // required: phone shown on dashboard
                .putString(KEY_USER_NAME, viewModel.fullName ?: "")       // optional: saves name if available
                .putString(KEY_USER_AREA, viewModel.phc ?: "")            // optional: saves area/PHC
                .putString(KEY_USER_ROLE_DISPLAY, viewModel.role ?: "")   // optional: saves role display text
                .apply()

        } else {
            Log.d(TAG, "Validation FAILED. Showing toast.")
            Toast.makeText(requireContext(), "Please fix the errors to continue", Toast.LENGTH_SHORT).show()
        }

        return isFormValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
