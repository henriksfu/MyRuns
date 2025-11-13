package com.example.henrik_sachdeva_myruns3.database

import android.icu.text.SimpleDateFormat
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.Locale

@Entity(tableName = "exercise_table")
data class ExerciseEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // 0 = Manual Input, 1 = GPS, 2 = Automatic
    val inputType: Int,

    val activityType: Int,

    val dateTime: Calendar,

    val duration: Double,      // minutes
    val distance: Double,      // miles
    val calories: Double,
    val heartRate: Double,
    val comment: String,

    // ⭐ NEW FOR MYRUNS4 ⭐
    // Stored as JSON string of LatLng points. Example: "[[lat,lon],[lat,lon],...]"
    val gpsData: String? = null
) {

    fun getFormattedDateTime(): String {
        val formatter = SimpleDateFormat("EEE, MMM d, yyyy h:mm a", Locale.getDefault())
        return formatter.format(dateTime.time)
    }

    fun getEntryTypeString(): String {
        return when (inputType) {
            INPUT_TYPE_MANUAL -> "Manual Entry"
            INPUT_TYPE_GPS -> "GPS"
            INPUT_TYPE_AUTOMATIC -> "Automatic"
            else -> "Unknown"
        }
    }

    fun getActivityTypeString(): String {
        return when (activityType) {
            0 -> "Running"
            1 -> "Walking"
            2 -> "Standing"
            3 -> "Cycling"
            4 -> "Hiking"
            5 -> "Downhill Skiing"
            6 -> "Cross-Country Skiing"
            7 -> "Snowboarding"
            8 -> "Skating"
            9 -> "Swimming"
            10 -> "Mountain Biking"
            11 -> "Wheelchair"
            12 -> "Elliptical"
            13 -> "Other"
            else -> "Unknown"
        }
    }

    fun getFormattedDuration(): String {
        val mins = duration.toInt()
        val secs = ((duration - mins) * 60).toInt()
        return "${mins} mins ${secs} secs"
    }

    companion object {
        const val INPUT_TYPE_MANUAL = 0
        const val INPUT_TYPE_GPS = 1
        const val INPUT_TYPE_AUTOMATIC = 2
    }
}
