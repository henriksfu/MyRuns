package com.example.henrik_sachdeva_myruns3.database

import androidx.room.TypeConverter
import java.util.Calendar

/**
 * Utility converters used by Room to store and retrieve unsupported data types.
 */
class Converters {

    @TypeConverter
    fun fromTimestamp(timestamp: Long?): Calendar? {
        // Convert stored milliseconds back to a Calendar instance
        return timestamp?.let {
            Calendar.getInstance().apply { timeInMillis = it }
        }
    }

    @TypeConverter
    fun calendarToTimestamp(calendar: Calendar?): Long? {
        // Store calendar time as milliseconds
        return calendar?.timeInMillis
    }

    companion object {
        // Helper conversion for distance (not used by Room directly)
        fun milesToKilometers(miles: Double): Double = miles * 1.60934
    }
}
