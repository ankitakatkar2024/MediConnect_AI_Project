package com.example.mediconnect_ai

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediconnect_ai.adapters.TaskListAdapter
import com.example.mediconnect_ai.database.AppDatabase
import com.example.mediconnect_ai.database.Task
import com.example.mediconnect_ai.database.TaskDao
import com.example.mediconnect_ai.databinding.ActivityDailyTasksBinding
import com.example.mediconnect_ai.firestore.FirebaseTaskHelper
import com.example.mediconnect_ai.notifications.PNCNotificationHelper  // 🔔 NEW IMPORT

import kotlinx.coroutines.launch
import java.util.Calendar

class DailyTasksActivity : BaseActivity() {

    private lateinit var binding: ActivityDailyTasksBinding
    private lateinit var taskDao: TaskDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailyTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        taskDao = AppDatabase.getInstance(this).taskDao()
        binding.recyclerViewTasks.layoutManager = LinearLayoutManager(this)

        binding.fabAddTask.setOnClickListener {
            startActivity(Intent(this, NewTaskActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        generateAndLoadDailyTasks()
    }

    private fun generateAndLoadDailyTasks() {
        lifecycleScope.launch {
            // 1. Generate ANC tasks
            generateTasksFromANCVisits()

            // 2. Generate PNC tasks (now also shows notifications)
            generateTasksFromPNCVisits()

            // 3. Generate Immunization tasks
            generateTasksFromImmunizations()
            // ✅ NEW: TB Tasks
            generateTasksFromTBVisits()


            // 4. Load final list
            loadTasksForToday()
        }
    }

    // ---------------- LOAD TODAY'S TASKS ----------------
    private suspend fun loadTasksForToday() {
        val calendar = Calendar.getInstance()
        val startOfToday = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfToday = calendar.apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis

        val dailyTasks = taskDao.getTasksForToday(startOfToday, endOfToday)

        if (dailyTasks.isEmpty()) {
            binding.recyclerViewTasks.visibility = View.GONE
            binding.tvNoTasks.visibility = View.VISIBLE
        } else {
            binding.recyclerViewTasks.visibility = View.VISIBLE
            binding.tvNoTasks.visibility = View.GONE
            binding.recyclerViewTasks.adapter = TaskListAdapter(dailyTasks) { task ->
                updateTaskStatus(task)
            }
        }
    }

    // ================= TB TASK GENERATOR =================
    private suspend fun generateTasksFromTBVisits() {
        val db = AppDatabase.getInstance(applicationContext)
        val tbDao = db.tbDao()
        val patientDao = db.patientDao()

        val (start, end) = todayRange()
        val visits = tbDao.getTBVisitsDueToday(start, end)

        for (visit in visits) {

            val profile = tbDao.getTBProfileById(visit.tbProfileId) ?: continue
            val patient = patientDao.getPatientById(profile.patientId) ?: continue

            val description = "TB Follow-up (${visit.visitType})"

            val exists = taskDao.taskExists(
                patient.id,
                description,
                visit.dueDate
            )

            if (!exists) {
                val task = Task(
                    patientId = patient.id,
                    patientName = patient.fullName,
                    taskDescription = description,
                    dueDate = visit.dueDate
                )

                val id = taskDao.insert(task)
                FirebaseTaskHelper.saveTask(task.copy(id = id))
            }
        }
    }

    // ---------------- ANC TASK GENERATOR ----------------
    private suspend fun generateTasksFromANCVisits() {
        val db = AppDatabase.getInstance(applicationContext)
        val ancVisitDao = db.ancVisitDao()
        val pregnancyDao = db.pregnancyDao()
        val taskDao = db.taskDao()

        val calendar = Calendar.getInstance()
        val startOfToday = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val endOfToday = calendar.apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis

        val upcomingVisits = ancVisitDao.getUpcomingANCVisits(startOfToday, endOfToday)

        for (visit in upcomingVisits) {
            val pregnancy = pregnancyDao.getPregnancyById(visit.pregnancyId)
            val activePregnancy = pregnancy?.takeIf { it.isActive } ?: continue

            val description = visit.visitName
            val taskExists = taskDao.taskExists(
                activePregnancy.patientId,
                description,
                visit.dueDate
            )

            if (!taskExists) {
                val newTask = Task(
                    patientId = activePregnancy.patientId,
                    patientName = activePregnancy.patientName,
                    taskDescription = description,
                    dueDate = visit.dueDate
                )
                val newId = taskDao.insert(newTask)
                val taskWithId = newTask.copy(id = newId)
                FirebaseTaskHelper.saveTask(taskWithId)
            }
        }
    }

    // ---------------- PNC TASK GENERATOR (WITH NOTIFICATIONS) ----------------
    private suspend fun generateTasksFromPNCVisits() {
        val db = AppDatabase.getInstance(applicationContext)
        val pncVisitDao = db.pncVisitDao()
        val patientDao = db.patientDao()
        val taskDao = db.taskDao()

        val calendar = Calendar.getInstance()
        val startOfToday = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val endOfToday = calendar.apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis

        val upcomingVisits = pncVisitDao.getUpcomingPNCVisits(startOfToday, endOfToday)

        for (visit in upcomingVisits) {
            val patient = patientDao.getPatientById(visit.patientId) ?: continue

            val description = visit.visitName
            val taskExists = taskDao.taskExists(
                visit.patientId,
                description,
                visit.dueDate
            )

            if (!taskExists) {
                val newTask = Task(
                    patientId = visit.patientId,
                    patientName = patient.fullName,
                    taskDescription = description,
                    dueDate = visit.dueDate
                )
                val newId = taskDao.insert(newTask)
                val taskWithId = newTask.copy(id = newId)
                FirebaseTaskHelper.saveTask(taskWithId)

                // 🔔 NEW: show notification for this PNC visit
                PNCNotificationHelper.showPNCReminder(
                    context = this@DailyTasksActivity,
                    patientId = visit.patientId,
                    patientName = patient.fullName,
                    visitName = description
                )
            }
        }
    }

    // ---------------- IMMUNIZATION TASK GENERATOR ----------------
    private suspend fun generateTasksFromImmunizations() {
        val db = AppDatabase.getInstance(applicationContext)
        val patientDao = db.patientDao()
        val vaccineStatusDao = db.vaccineStatusDao()
        val taskDao = db.taskDao()

        val fiveYearsAgo = Calendar.getInstance().apply {
            add(Calendar.YEAR, -5)
        }.timeInMillis
        val infants = patientDao.getPatientsBornAfter(fiveYearsAgo)

        val calendar = Calendar.getInstance()
        val startOfToday = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val endOfToday = calendar.apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis

        for (infant in infants) {
            val schedule = vaccineStatusDao.getScheduleForPatientAsList(infant.id)
            for (vaccine in schedule) {
                if (
                    vaccine.status == "Scheduled" &&
                    vaccine.dueDate >= startOfToday &&
                    vaccine.dueDate < endOfToday
                ) {
                    val description =
                        "Administer ${vaccine.vaccineName} for ${infant.fullName}"
                    val taskExists = taskDao.taskExists(
                        infant.id,
                        description,
                        vaccine.dueDate
                    )
                    if (!taskExists) {
                        val newTask = Task(
                            patientId = infant.id,
                            patientName = infant.fullName,
                            taskDescription = description,
                            dueDate = vaccine.dueDate
                        )
                        val newId = taskDao.insert(newTask)
                        val taskWithId = newTask.copy(id = newId)
                        FirebaseTaskHelper.saveTask(taskWithId)
                    }
                }
            }
        }
    }

    // ---------------- VISIT STATUS UPDATER ----------------
    private fun updateTaskStatus(task: Task) {
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(applicationContext)

            // 1. Update local task
            db.taskDao().update(task)

            // 2. Sync to Firestore
            FirebaseTaskHelper.saveTask(task)

            // 3. If completed, sync back to ANC / PNC tables
            if (task.isCompleted) {
                when {
                    task.taskDescription.contains("ANC") -> syncCompletionToAncTable(task)
                    task.taskDescription.contains("PNC") -> syncCompletionToPncTable(task)
                    task.taskDescription.contains("TB")  -> syncCompletionToTbTable(task)
                }
            }
        }
    }

    // ================= TB COMPLETION SYNC =================
    private suspend fun syncCompletionToTbTable(task: Task) {
        val db = AppDatabase.getInstance(applicationContext)
        val tbDao = db.tbDao()

        val profile = tbDao.getActiveTBProfile(task.patientId) ?: return
        val visits = tbDao.getVisitsForProfile(profile.id)

        val matchingVisit = visits.find {
            task.taskDescription.contains(it.visitType) && !it.isCompleted
        }

        if (matchingVisit != null) {
            tbDao.updateTBVisit(
                matchingVisit.copy(
                    isCompleted = true,
                    completionDate = System.currentTimeMillis()
                )
            )
            Toast.makeText(this, "TB follow-up updated", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun syncCompletionToAncTable(task: Task) {
        val db = AppDatabase.getInstance(applicationContext)
        val pregnancy = db.pregnancyDao().getActivePregnancyForPatient(task.patientId)

        if (pregnancy != null) {
            val visits = db.ancVisitDao().getVisitsForPregnancy(pregnancy.id)
            val matchingVisit = visits.find { it.visitName == task.taskDescription }

            if (matchingVisit != null && !matchingVisit.isCompleted) {
                val updatedVisit = matchingVisit.copy(
                    isCompleted = true,
                    completionDate = System.currentTimeMillis()
                )
                db.ancVisitDao().update(updatedVisit)
                Toast.makeText(this, "ANC Schedule updated!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun syncCompletionToPncTable(task: Task) {
        val db = AppDatabase.getInstance(applicationContext)
        val visits = db.pncVisitDao().getPNCVisitsForPatient(task.patientId)
        val matchingVisit = visits.find { it.visitName == task.taskDescription }

        if (matchingVisit != null && !matchingVisit.isCompleted) {
            val updatedVisit = matchingVisit.copy(
                isCompleted = true,
                completionDate = System.currentTimeMillis()
            )
            db.pncVisitDao().update(updatedVisit)
            Toast.makeText(this, "PNC Schedule updated!", Toast.LENGTH_SHORT).show()
        }
    }
}
// ================= DATE UTILITY =================
private fun todayRange(): Pair<Long, Long> {
    val cal = Calendar.getInstance()
    val start = cal.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val end = cal.apply { add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
    return start to end
}
