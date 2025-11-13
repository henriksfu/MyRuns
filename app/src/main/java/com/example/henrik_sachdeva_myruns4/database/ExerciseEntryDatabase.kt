package com.example.henrik_sachdeva_myruns4.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ExerciseEntry::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ExerciseEntryDatabase : RoomDatabase() {

    abstract fun exerciseEntryDatabaseDao(): ExerciseEntryDatabaseDao

    companion object {

        @Volatile
        private var INSTANCE: ExerciseEntryDatabase? = null

        // Singleton DB instance
        fun getInstance(context: Context): ExerciseEntryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExerciseEntryDatabase::class.java,
                    "exercise_database"
                )
                    .fallbackToDestructiveMigration() // handle schema changes safely
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
