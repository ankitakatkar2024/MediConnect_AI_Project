package com.example.mediconnect_ai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SosLogsActivity :  BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SosLogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sos_logs)

        recyclerView = findViewById(R.id.recyclerSosLogs) // ✅ updated ID to match layout
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch logs from Room database on background thread
        lifecycleScope.launch {
            val logs = withContext(Dispatchers.IO) {
                SosLogDatabase.getDatabase(this@SosLogsActivity)
                    .sosLogDao()
                    .getAllLogs()  // ✅ Make sure DAO returns List<SosLog>
            }

            adapter = SosLogAdapter(logs)
            recyclerView.adapter = adapter
        }
    }
}
