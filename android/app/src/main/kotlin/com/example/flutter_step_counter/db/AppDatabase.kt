package com.example.flutter_step_counter.db

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [StepRecord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun stepDao(): StepDao

    companion object {
        private const val TAG = "AppDatabase"
        private const val DB_NAME = "step_database.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            Log.d(TAG, "🟡 getDatabase() called")

            return INSTANCE ?: synchronized(this) {
                Log.d(TAG, "🔃 Building database instance")
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                ).fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d(TAG, "📦 Database created: $DB_NAME")
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            Log.d(TAG, "📂 Database opened: $DB_NAME")
                        }
                    })
                    .build()

                INSTANCE = instance
                Log.d(TAG, "✅ Database instance created and cached")
                instance
            }
        }
    }
}
