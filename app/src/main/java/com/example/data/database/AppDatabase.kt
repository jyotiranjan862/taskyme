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
import com.example.data.model.SubtaskEntity
import com.example.data.model.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
            }
        }
    }
}
