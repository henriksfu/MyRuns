package com.example.henrik_sachdeva_myruns3.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "exercise_table")
data class ExerciseEntry(

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    var inputType: Int = 0,
    var activityType: Int = 0,

    var dateTime: Date = Date(),

    var duration: Int = 0,            // seconds
    var distance: Double = 0.0,       // km
    var avgSpeed: Double = 0.0,       // km/h
    var currentSpeed: Double = 0.0,   // km/h
    var calories: Int = 0,
    var heartRate: Int = 0,

    var comment: String = "",

    // ⭐ GPS path stored as JSON string instead of MutableList<LatLng>
    var gpsJson: String = "[]"
) {

    fun formattedDateTime(): String {
        return java.text.SimpleDateFormat(
            "EEE, MMM d, yyyy h:mm a",
            Locale.getDefault()
        ).format(dateTime)
    }

    fun formattedDuration(): String {
        val mins = duration / 60
        val secs = duration % 60
        return "$mins mins $secs secs"
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

    companion object {
        const val INPUT_TYPE_MANUAL = 0
        const val INPUT_TYPE_GPS = 1
        const val INPUT_TYPE_AUTOMATIC = 2
    }
}
