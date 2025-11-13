package com.example.henrik_sachdeva_myruns3

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private var activityTypeId: Int = 0

    private lateinit var map: GoogleMap
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val gpsPoints = mutableListOf<LatLng>()
    private lateinit var polyline: Polyline

    private val REQUEST_LOCATION = 1001
    private val gson = Gson()

    private var mode: String = "record"
    private var startTime: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)

        mode = intent.getStringExtra("mode") ?: "record"
        activityTypeId = intent.getIntExtra("ACTIVITY_TYPE_ID", 0)

        fusedClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        cancelButton.setOnClickListener { finish() }

        saveButton.setOnClickListener {
            if (mode == "record") {
                val json = gson.toJson(gpsPoints)
                val distance = calculateTotalDistance()
                val duration = calculateDurationMinutes()

                val result = Intent().apply {
                    putExtra("gps_json", json)
                    putExtra("gps_distance", distance)
                    putExtra("gps_duration", duration)
                    putExtra("activity_type", activityTypeId)
                }

                setResult(RESULT_OK, result)
            }
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::locationCallback.isInitialized) {
            fusedClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.mapType = GoogleMap.MAP_TYPE_NORMAL

        polyline = map.addPolyline(
            PolylineOptions().width(10f).color(Color.BLUE)
        )

        if (mode == "view") setupViewMode()
        else requestLocationPermission()
    }

    private fun setupViewMode() {
        saveButton.visibility = View.GONE
        cancelButton.text = "Back"

        val json = intent.getStringExtra("gps_path") ?: return

        val listType = object : TypeToken<List<LatLng>>() {}.type
        val saved: List<LatLng> = gson.fromJson(json, listType)

        gpsPoints.addAll(saved)
        polyline.points = gpsPoints

        if (gpsPoints.isNotEmpty()) {
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(gpsPoints.first(), 17f)
            )
        }
    }

    private fun requestLocationPermission() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            startGPSUpdates()
            return
        }

        if (ContextCompat.checkSelfPermission(this, permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_LOCATION)
        } else {
            startGPSUpdates()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) startGPSUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startGPSUpdates() {
        map.isMyLocationEnabled = true
        startTime = System.currentTimeMillis()

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            500L
        ).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { updateLocation(it) }
            }
        }

        fusedClient.requestLocationUpdates(request, locationCallback, mainLooper)
    }

    private fun updateLocation(loc: Location) {
        val point = LatLng(loc.latitude, loc.longitude)
        gpsPoints.add(point)

        polyline.points = gpsPoints

        if (gpsPoints.size == 1) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 17f))
        }
    }

    private fun calculateTotalDistance(): Double {
        if (gpsPoints.size < 2) return 0.0

        var meters = 0f

        for (i in 1 until gpsPoints.size) {
            val l1 = Location("").apply {
                latitude = gpsPoints[i - 1].latitude
                longitude = gpsPoints[i - 1].longitude
            }
            val l2 = Location("").apply {
                latitude = gpsPoints[i].latitude
                longitude = gpsPoints[i].longitude
            }
            meters += l1.distanceTo(l2)
        }

        return meters / 1609.34 // meters → miles
    }

    private fun calculateDurationMinutes(): Double {
        val end = System.currentTimeMillis()
        return (end - startTime) / 1000.0 / 60.0
    }
}
