package com.example.henrik_sachdeva_myruns3.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ExerciseEntry::class],
    version = 2,                 // ⭐ UPDATED VERSION
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ExerciseEntryDatabase : RoomDatabase() {

    abstract val exerciseEntryDatabaseDao: ExerciseEntryDatabaseDao

    companion object {
        @Volatile
        private var INSTANCE: ExerciseEntryDatabase? = null

        fun getInstance(context: Context): ExerciseEntryDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ExerciseEntryDatabase::class.java,
                        "exercise_database"
                    )
                        .fallbackToDestructiveMigration() // ⭐ IMPORTANT FOR VERSION CHANGE
                        .build()

                    INSTANCE = instance
                }
                return instance!!
            }
        }
    }
}
