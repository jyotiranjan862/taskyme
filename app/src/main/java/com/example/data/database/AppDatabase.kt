package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AchievementDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.TaskDao
import com.example.data.model.AchievementEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.Priority
import com.example.data.model.SubtaskEntity
import com.example.data.model.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [
        TaskEntity::class,
        SubtaskEntity::class,
        CategoryEntity::class,
        AchievementEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_isArchived ON tasks(isArchived)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "taskflow_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        prepopulate(database)
                    }
                }
            }

            private suspend fun prepopulate(database: AppDatabase) {
                // Prepopulate categories
                database.categoryDao().insertCategories(CategoryEntity.defaultCategories())

                // Prepopulate achievements
                database.achievementDao().insertAchievements(AchievementEntity.defaultAchievements())

                // Prepopulate helpful starter tasks
                val now = System.currentTimeMillis()
                val cal = Calendar.getInstance()
                cal.timeInMillis = now

                // Task 1: Due in 4 hours today (Urgent next deadline demonstration)
                cal.add(Calendar.HOUR_OF_DAY, 4)
                val taskId1 = database.taskDao().insertTask(
                    TaskEntity(
                        title = "Finalize Q4 Strategy Presentation",
                        description = "Review budget figures with finance team and export slides to PDF.",
                        categoryId = 1, // Work
                        priority = Priority.URGENT.name,
                        deadlineTimestamp = cal.timeInMillis,
                        hasSpecificTime = true,
                        reminderMinutesBefore = 30,
                        isPersistentNotificationEnabled = true,
                        estimatedDurationMinutes = 45,
                        tags = "Presentation, Strategy"
                    )
                )
                database.taskDao().insertSubtasks(
                    listOf(
                        SubtaskEntity(taskId = taskId1, title = "Cross-check ROI metrics", isCompleted = true, position = 0),
                        SubtaskEntity(taskId = taskId1, title = "Design summary infographic", isCompleted = false, position = 1),
                        SubtaskEntity(taskId = taskId1, title = "Send preview link to lead", isCompleted = false, position = 2)
                    )
                )

                // Task 2: Due Tomorrow at 10:00 AM
                val tomorrowCal = Calendar.getInstance()
                tomorrowCal.add(Calendar.DAY_OF_YEAR, 1)
                tomorrowCal.set(Calendar.HOUR_OF_DAY, 10)
                tomorrowCal.set(Calendar.MINUTE, 0)
                val taskId2 = database.taskDao().insertTask(
                    TaskEntity(
                        title = "Morning High-Intensity Workout",
                        description = "30 mins cardio, core strength, and stretching session.",
                        categoryId = 4, // Health
                        priority = Priority.HIGH.name,
                        deadlineTimestamp = tomorrowCal.timeInMillis,
                        hasSpecificTime = true,
                        reminderMinutesBefore = 15,
                        recurringRule = "DAILY",
                        estimatedDurationMinutes = 35,
                        tags = "Fitness, Health"
                    )
                )
                database.taskDao().insertSubtasks(
                    listOf(
                        SubtaskEntity(taskId = taskId2, title = "Warmup & mobility", isCompleted = false, position = 0),
                        SubtaskEntity(taskId = taskId2, title = "Main circuit x 3", isCompleted = false, position = 1)
                    )
                )

                // Task 3: Learning task
                val learningCal = Calendar.getInstance()
                learningCal.add(Calendar.DAY_OF_YEAR, 2)
                learningCal.set(Calendar.HOUR_OF_DAY, 18)
                database.taskDao().insertTask(
                    TaskEntity(
                        title = "Read Chapter 4 of Clean Architecture",
                        description = "Take notes on dependency inversion and boundary layers.",
                        categoryId = 3, // Learning
                        priority = Priority.MEDIUM.name,
                        deadlineTimestamp = learningCal.timeInMillis,
                        hasSpecificTime = true,
                        reminderMinutesBefore = 0,
                        estimatedDurationMinutes = 60,
                        tags = "Books, Engineering"
                    )
                )

                // Completed task to show progress on first launch
                val completedCal = Calendar.getInstance()
                completedCal.add(Calendar.HOUR_OF_DAY, -2)
                database.taskDao().insertTask(
                    TaskEntity(
                        title = "Install TaskFlow & explore features",
                        description = "Set up categories and review upcoming deadlines.",
                        categoryId = 2, // Personal
                        priority = Priority.LOW.name,
                        deadlineTimestamp = now - 3600000,
                        hasSpecificTime = true,
                        isCompleted = true,
                        completedAt = now - 1800000,
                        tags = "Welcome"
                    )
                )
            }
        }
    }
}
