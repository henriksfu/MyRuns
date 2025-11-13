package com.example.henrik_sachdeva_myruns3.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ExerciseEntry::class],
    version = 4,            // 🔥 bump version again to avoid cache issues
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ExerciseEntryDatabase : RoomDatabase() {

    abstract fun exerciseEntryDatabaseDao(): ExerciseEntryDatabaseDao

    companion object {

        @Volatile
        private var INSTANCE: ExerciseEntryDatabase? = null

        fun getInstance(context: Context): ExerciseEntryDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExerciseEntryDatabase::class.java,
                    "exercise_database"
                )
                    // 🔥 Prevent crashes when schema changes
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
