package com.example.henrik_sachdeva_myruns4

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.henrik_sachdeva_myruns4.database.ExerciseEntry
import com.example.henrik_sachdeva_myruns4.database.ExerciseEntryDatabase
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var polyline: Polyline

    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var deleteButton: Button
    private lateinit var statusText: TextView

    private var isHistoryMode = false
    private var entryId: Long = -1L

    private var trackingService: TrackingService? = null
    private var bound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(component: ComponentName?, binder: IBinder?) {
            val b = binder as TrackingService.LocalBinder
            trackingService = b.getService()
            bound = true

            trackingService!!.liveEntry.observe(this@MapActivity) { entry ->
                updateLiveUI(entry)
            }
        }

        override fun onServiceDisconnected(component: ComponentName?) {
            bound = false
            trackingService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
        statusText = findViewById(R.id.type_stats)
        deleteButton = findViewById(R.id.deleteButton)

        statusText.text = """
            Activity:
            Avg Speed: 0.00 km/h
            Current Speed: 0.00 km/h
            Distance: 0.00 km
            Calories: 0
            Duration: 0 mins 0 secs
        """.trimIndent()

        val mode = intent.getStringExtra("mode") ?: "live"
        isHistoryMode = mode == "history"
        entryId = intent.getLongExtra("entry_id", -1L)

        val mapFrag = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFrag.getMapAsync(this)

        cancelButton.setOnClickListener { finish() }
        setupSaveButton()

        requestLocationPermission()
    }

    private fun setupSaveButton() {
        saveButton.setOnClickListener {
            if (!isHistoryMode && trackingService != null) {
                val entry = trackingService!!.getFinalEntry()

                Thread {
                    val dao = ExerciseEntryDatabase.getInstance(this)
                        .exerciseEntryDatabaseDao()
                    dao.insertEntry(entry)
                }.start()

                Toast.makeText(this, "GPS entry saved", Toast.LENGTH_SHORT).show()
            } else if (!isHistoryMode) {
                Toast.makeText(this, "No GPS data to save", Toast.LENGTH_SHORT).show()
            }

            setResult(RESULT_OK)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()

        if (!isHistoryMode) {
            val i = Intent(this, TrackingService::class.java).apply {
                putExtra("inputType", intent.getIntExtra("inputType", 0))
                putExtra("activityType", intent.getIntExtra("activityType", 0))
            }

            ContextCompat.startForegroundService(this, i)
            bindService(i, connection, BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (bound) {
            unbindService(connection)
            bound = false
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        polyline = map.addPolyline(
            PolylineOptions()
                .color(0xFF0000FF.toInt())
                .width(12f)
        )

        enableUserLocation()

        if (isHistoryMode) {
            loadHistoryEntry()
        }
    }

    private fun enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true

            val client = LocationServices.getFusedLocationProviderClient(this)
            client.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    val pos = LatLng(loc.latitude, loc.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 17f))
                }
            }
        }
    }

    private fun updateLiveUI(entry: ExerciseEntry?) {
        entry ?: return

        val pts = parsePoints(entry.gpsJson)
        if (pts.isNotEmpty()) {
            polyline.points = pts
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(pts.last(), 17f))
        }

        val stats = """
            Activity: ${entry.getActivityTypeString()}
            Avg Speed: %.2f km/h
            Current Speed: %.2f km/h
            Distance: %.2f km
            Calories: ${entry.calories}
            Duration: ${entry.formattedDuration()}
        """.trimIndent().format(
            entry.avgSpeed,
            entry.currentSpeed,
            entry.distance
        )

        statusText.text = stats
    }

    private fun loadHistoryEntry() {
        saveButton.visibility = View.GONE
        deleteButton.visibility = View.VISIBLE
        cancelButton.text = "Back"

        deleteButton.setOnClickListener {
            val dao = ExerciseEntryDatabase.getInstance(this).exerciseEntryDatabaseDao()
            Thread {
                dao.deleteEntry(entryId)
                runOnUiThread {
                    Toast.makeText(this, "Entry deleted", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }.start()
        }

        val dao = ExerciseEntryDatabase.getInstance(this).exerciseEntryDatabaseDao()

        Thread {
            val entry = dao.getEntryNow(entryId)

            runOnUiThread {
                val pts = parsePoints(entry.gpsJson)
                polyline.points = pts

                if (pts.isNotEmpty()) {
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(pts.first(), 17f)
                    )
                }

                val stats = """
                    Activity: ${entry.getActivityTypeString()}
                    Distance: %.2f km
                    Calories: ${entry.calories}
                    Duration: ${entry.formattedDuration()}
                """.trimIndent().format(entry.distance)

                statusText.text = stats
            }
        }.start()
    }

    private fun parsePoints(json: String?): List<LatLng> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<LatLng>>() {}.type
        return Gson().fromJson(json, type)
    }

    private fun requestLocationPermission() {
        val granted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        }
    }
}
