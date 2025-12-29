package com.example.mediconnect_ai

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediconnect_ai.adapters.NotificationHistoryAdapter
import com.example.mediconnect_ai.database.AppDatabase
import com.example.mediconnect_ai.databinding.ActivityNotificationsBinding
import kotlinx.coroutines.launch

class NotificationsActivity : BaseActivity() {

    private lateinit var binding: ActivityNotificationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewNotifications.layoutManager = LinearLayoutManager(this)
        loadNotificationHistory()
    }

    private fun loadNotificationHistory() {
        val dao = AppDatabase.getInstance(applicationContext).notificationHistoryDao()
        lifecycleScope.launch {
            val history = dao.getAllNotifications()
            binding.recyclerViewNotifications.adapter = NotificationHistoryAdapter(history)
        }
    }
}