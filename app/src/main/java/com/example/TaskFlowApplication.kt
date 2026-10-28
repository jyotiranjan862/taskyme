package com.example

import android.app.Application
import com.example.data.database.AppDatabase
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.TaskRepository
import com.example.notification.NotificationHelper

class TaskFlowApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var taskRepository: TaskRepository
        private set

    lateinit var preferencesRepository: PreferencesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)

        database = AppDatabase.getDatabase(this)
        preferencesRepository = PreferencesRepository(this)
        taskRepository = TaskRepository(
            context = this,
            taskDao = database.taskDao(),
            categoryDao = database.categoryDao(),
            achievementDao = database.achievementDao()
        )
    }
}
