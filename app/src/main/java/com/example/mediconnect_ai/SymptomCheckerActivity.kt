package com.example.mediconnect_ai

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.Gravity
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.mediconnect_ai.database.AppDatabase
import com.example.mediconnect_ai.database.ChatMessage
import com.example.mediconnect_ai.database.ChatMessageDao
import com.example.mediconnect_ai.databinding.ActivitySymptomCheckerBinding
import com.example.mediconnect_ai.network.RetrofitClient
import com.example.mediconnect_ai.network.SymptomRequest
import com.example.mediconnect_ai.network.SymptomResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class SymptomCheckerActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySymptomCheckerBinding
    // Database variables
    private lateinit var db: AppDatabase
    private lateinit var chatMessageDao: ChatMessageDao

    private val SPEECH_REQUEST_CODE = 123
    private val RECORD_AUDIO_PERMISSION_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySymptomCheckerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the database and DAO
        db = AppDatabase.getInstance(applicationContext)
        chatMessageDao = db.chatMessageDao()

        setupLanguageSpinner()

        // Load previous chat history when the screen opens
        loadChatHistory()

        binding.btnSendSymptom.setOnClickListener {
            val symptomText = binding.etSymptomInput.text.toString()
            if (symptomText.isNotBlank()) {
                addMessageToChat(symptomText, true)
                getSymptomSuggestion(symptomText)
                binding.etSymptomInput.text.clear()
            } else {
                Toast.makeText(this, "Please describe a symptom", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnVoiceInput.setOnClickListener {
            checkPermissionAndStartVoiceInput()
        }
    }

    // Function to load all messages from the database
    private fun loadChatHistory() {
        lifecycleScope.launch {
            val messages = chatMessageDao.getAllMessages()
            for (message in messages) {
                // Use a different function to add to UI without saving again
                addMessageToUi(message.message, message.isUserMessage)
            }
        }
    }

    // Function to save a new message to the database
    private fun saveMessageToDatabase(message: String, isUserMessage: Boolean) {
        lifecycleScope.launch {
            val chatMessage = ChatMessage(message = message, isUserMessage = isUserMessage)
            chatMessageDao.insert(chatMessage)
        }
    }

    // This function now saves the message after adding it to the UI
    private fun addMessageToChat(message: String, isUserMessage: Boolean) {
        addMessageToUi(message, isUserMessage)
        saveMessageToDatabase(message, isUserMessage)
    }

    private fun getSymptomSuggestion(symptom: String) {
        addMessageToChat("Analyzing your symptom...", false)
        val selectedLanguage = binding.languageSpinner.selectedItem.toString()
        val request = SymptomRequest(symptom = symptom, language = selectedLanguage)
        RetrofitClient.instance.checkSymptom(request).enqueue(object : Callback<SymptomResponse> {
            override fun onResponse(call: Call<SymptomResponse>, response: Response<SymptomResponse>) {
                binding.chatContainer.removeViewAt(binding.chatContainer.childCount - 1)
                if (response.isSuccessful) {
                    val suggestion = response.body()?.suggestion ?: "Sorry, I couldn't get a suggestion."
                    addMessageToChat(suggestion, false)
                } else {
                    addMessageToChat("Error: Could not get a valid response from the server.", false)
                }
            }
            override fun onFailure(call: Call<SymptomResponse>, t: Throwable) {
                binding.chatContainer.removeViewAt(binding.chatContainer.childCount - 1)
                addMessageToChat("Failed to connect to the server. Please check your internet connection.", false)
                Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    // This function only handles adding a message to the UI
    private fun addMessageToUi(message: String, isUserMessage: Boolean) {
        val textView = TextView(this)
        textView.text = message
        textView.setPadding(32, 16, 32, 16)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = 16
        }
        if (isUserMessage) {
            layoutParams.gravity = Gravity.END
            textView.setBackgroundResource(R.drawable.user_chat_bubble_background)
            textView.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        } else {
            layoutParams.gravity = Gravity.START
            textView.setBackgroundResource(R.drawable.bot_chat_bubble_background)
            textView.setTextColor(ContextCompat.getColor(this, android.R.color.black))
        }
        textView.layoutParams = layoutParams
        binding.chatContainer.addView(textView)
        binding.chatScrollView.post { binding.chatScrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    // --- The rest of the functions for Language Spinner, Permissions, and Voice Input are unchanged ---

    private fun setupLanguageSpinner() {
        ArrayAdapter.createFromResource(
            this,
            R.array.languages_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.languageSpinner.adapter = adapter
        }
    }

    private fun checkPermissionAndStartVoiceInput() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startVoiceInput()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceInput()
            } else {
                Toast.makeText(this, "Microphone permission is required to use voice input", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startVoiceInput() {
        Toast.makeText(this, "Listening...", Toast.LENGTH_SHORT).show()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Please speak your symptoms")
        }
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE)
        } catch (e: Exception) {
            Toast.makeText(this, "Your device does not support voice input", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val spokenText: String? =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let { results ->
                    results[0]
                }
            binding.etSymptomInput.setText(spokenText)
        }
    }
}