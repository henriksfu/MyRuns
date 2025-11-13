package com.example.henrik_sachdeva_myruns3

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.henrik_sachdeva_myruns3.database.ExerciseEntry
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import kotlin.math.max

class TrackingService : Service() {

    // -----------------------------
    // Binder for MapActivity
    // -----------------------------
    private val binder = LocalBinder()
    inner class LocalBinder : Binder() {
        fun getService(): TrackingService = this@TrackingService
    }
    override fun onBind(intent: Intent?): IBinder = binder

    // -----------------------------
    // Live Entry Updates
    // -----------------------------
    private val _liveEntry = MutableLiveData<ExerciseEntry>()
    val liveEntry: LiveData<ExerciseEntry> get() = _liveEntry

    // GPS data container
    private lateinit var entry: ExerciseEntry

    // Local GPS path list (Room cannot store LatLng)
    private val gpsPoints = mutableListOf<LatLng>()

    private var lastLocation: Location? = null

    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    private var startTimestamp: Long = 0L

    // -----------------------------
    // On Create
    // -----------------------------
    override fun onCreate() {
        super.onCreate()

        entry = ExerciseEntry()  // fresh DB entity
        fusedClient = LocationServices.getFusedLocationProviderClient(this)

        setupLocationRequest()
        startForegroundServiceNotification()
    }

    // -----------------------------
    // Service Start
    // -----------------------------
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        entry.inputType = intent?.getIntExtra("inputType", -1) ?: -1
        entry.activityType = intent?.getIntExtra("activityType", -1) ?: -1

        startTimestamp = System.currentTimeMillis()
        entry.dateTime = java.util.Date(startTimestamp)

        startLocationUpdates()

        return START_STICKY
    }

    // -----------------------------
    // Location Request Setup
    // -----------------------------
    private fun setupLocationRequest() {
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000L
        ).setMinUpdateDistanceMeters(1f).build()
    }

    // -----------------------------
    // Location Callback
    // -----------------------------
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            for (loc in result.locations) updateEntryWithNewLocation(loc)
        }
    }

    // -----------------------------
    // Update ExerciseEntry with new GPS data
    // -----------------------------
    private fun updateEntryWithNewLocation(loc: Location) {

        val point = LatLng(loc.latitude, loc.longitude)
        gpsPoints.add(point)

        // Distance & Speed
        lastLocation?.let { prev ->
            val distKm = prev.distanceTo(loc) / 1000.0
            entry.distance += distKm

            val deltaSec = (loc.time - prev.time) / 1000.0
            if (deltaSec > 0) entry.currentSpeed = distKm / (deltaSec / 3600.0)
        }
        lastLocation = loc

        // Duration
        entry.duration = ((System.currentTimeMillis() - startTimestamp) / 1000).toInt()

        // Avg speed
        val hours = entry.duration / 3600.0
        if (hours > 0) entry.avgSpeed = entry.distance / hours

        // Calories approximation
        entry.calories = max(1.0, entry.distance * 60).toInt()

        // Update JSON field
        entry.gpsJson = Gson().toJson(gpsPoints)

        _liveEntry.postValue(entry)
    }

    // -----------------------------
    // Start GPS updates
    // -----------------------------
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return

        fusedClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    // -----------------------------
    // Foreground Notification
    // -----------------------------
    private fun startForegroundServiceNotification() {
        val channelId = "tracking_service_channel"

        // Create channel on Android 8+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Tracking Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notif = NotificationCompat.Builder(this, channelId)
            .setContentTitle("MyRuns Tracking")
            .setContentText("Recording GPS Activity…")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

        startForeground(1, notif)
    }

    // -----------------------------
    // Return final entry to MapActivity
    // -----------------------------
    fun getFinalEntry(): ExerciseEntry = entry

    override fun onDestroy() {
        super.onDestroy()
        fusedClient.removeLocationUpdates(locationCallback)
    }
}
