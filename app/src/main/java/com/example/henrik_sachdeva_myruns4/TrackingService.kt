package com.example.henrik_sachdeva_myruns4

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.*
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.henrik_sachdeva_myruns4.database.ExerciseEntry
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import kotlin.math.max

class TrackingService : Service() {

    private val binder = LocalBinder()
    inner class LocalBinder : Binder() {
        fun getService(): TrackingService = this@TrackingService
    }
    override fun onBind(intent: Intent?): IBinder = binder

    private val _liveEntry = MutableLiveData<ExerciseEntry>()
    val liveEntry: LiveData<ExerciseEntry> get() = _liveEntry

    private lateinit var entry: ExerciseEntry
    private val gpsPoints = mutableListOf<LatLng>()
    private var lastLocation: Location? = null

    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    private var startTimestamp = 0L

    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            updateDuration()
            timerHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        entry = ExerciseEntry()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)

        setupLocationRequest()
        startForegroundNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        entry.inputType = intent?.getIntExtra("inputType", -1) ?: -1
        entry.activityType = intent?.getIntExtra("activityType", -1) ?: -1

        startTimestamp = System.currentTimeMillis()
        entry.dateTime = java.util.Date(startTimestamp)

        startLocationUpdates()
        timerHandler.post(timerRunnable)

        return START_STICKY
    }

    private fun updateDuration() {
        entry.duration = ((System.currentTimeMillis() - startTimestamp) / 1000).toInt()

        val hours = entry.duration / 3600.0
        if (hours > 0) entry.avgSpeed = entry.distance / hours

        _liveEntry.postValue(entry)
    }

    private fun setupLocationRequest() {
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000L
        ).setMinUpdateDistanceMeters(1f).build()
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.locations.forEach { updateWithLocation(it) }
        }
    }

    private fun updateWithLocation(loc: Location) {
        val point = LatLng(loc.latitude, loc.longitude)
        gpsPoints.add(point)

        lastLocation?.let { prev ->
            val distKm = prev.distanceTo(loc) / 1000.0
            entry.distance += distKm

            val deltaSec = (loc.time - prev.time) / 1000.0
            if (deltaSec > 0) {
                entry.currentSpeed = distKm / (deltaSec / 3600.0)
            }
        }
        lastLocation = loc

        entry.duration = ((System.currentTimeMillis() - startTimestamp) / 1000).toInt()

        val hours = entry.duration / 3600.0
        if (hours > 0) entry.avgSpeed = entry.distance / hours

        entry.calories = max(1.0, entry.distance * 60).toInt()
        entry.gpsJson = Gson().toJson(gpsPoints)

        _liveEntry.postValue(entry)
    }

    private fun startLocationUpdates() {
        val granted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) return

        fusedClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun startForegroundNotification() {
        val channelId = "tracking_service_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Tracking Service",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        val notif = NotificationCompat.Builder(this, channelId)
            .setContentTitle("MyRuns Tracking")
            .setContentText("Recording GPS Activity…")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

        startForeground(1, notif)
    }

    fun getFinalEntry(): ExerciseEntry = entry

    override fun onDestroy() {
        super.onDestroy()
        fusedClient.removeLocationUpdates(locationCallback)
        timerHandler.removeCallbacks(timerRunnable)
    }
}
