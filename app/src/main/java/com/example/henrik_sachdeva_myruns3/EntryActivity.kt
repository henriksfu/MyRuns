package com.example.henrik_sachdeva_myruns3

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.example.henrik_sachdeva_myruns3.database.*

class EntryActivity : AppCompatActivity() {

    private lateinit var viewModel: ExerciseViewModel
    private lateinit var sharedPrefs: SharedPreferences

    private lateinit var deleteButton: Button
    private lateinit var cancelButton: Button
    private lateinit var viewMapButton: Button

    private var mode: String = "view"
    private var entryId: Long = -1L

    /** Launcher to receive GPS data from MapActivity */
    private val gpsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val gpsJson = result.data?.getStringExtra("gps_result")
                if (gpsJson != null) saveGpsEntry(gpsJson)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)

        deleteButton = findViewById(R.id.delete_button)
        cancelButton = findViewById(R.id.cancel_button)
        viewMapButton = findViewById(R.id.view_map_button)

        mode = intent.getStringExtra("MODE") ?: "view"
        entryId = intent.getLongExtra("ENTRY_ID", -1)

        val dao = ExerciseEntryDatabase.getInstance(this).exerciseEntryDatabaseDao
        val repo = ExerciseRepository(dao)
        val factory = ExerciseViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory)[ExerciseViewModel::class.java]

        when (mode) {
            "new_gps" -> {
                deleteButton.visibility = View.GONE
                viewMapButton.visibility = View.GONE

                val intent = Intent(this, MapActivity::class.java).apply {
                    putExtra("mode", "record")
                    putExtra("ACTIVITY_TYPE_ID", selectedActivityType)
                }

                gpsLauncher.launch(intent)
            }


            "view" -> setUpExistingEntry()
        }

        cancelButton.setOnClickListener { finish() }
    }

    /** Display existing entry */
    private fun setUpExistingEntry() {
        viewModel.getEntryById(entryId).observe(this, Observer { entry ->
            entry ?: return@Observer

            findViewById<TextView>(R.id.tv_entry_type).text = entry.getEntryTypeString()
            findViewById<TextView>(R.id.tv_activity_type).text = entry.getActivityTypeString()
            findViewById<TextView>(R.id.tv_date_time).text = entry.getFormattedDateTime()
            findViewById<TextView>(R.id.tv_duration).text = entry.getFormattedDuration()
            findViewById<TextView>(R.id.tv_distance).text = formatDistance(entry.distance)
            findViewById<TextView>(R.id.tv_calories).text = "${entry.calories.toInt()} cals"
            findViewById<TextView>(R.id.tv_heart_rate).text = "${entry.heartRate.toInt()} bpm"

            if (entry.inputType == ExerciseEntry.INPUT_TYPE_GPS && entry.gpsData != null) {
                viewMapButton.visibility = View.VISIBLE
                viewMapButton.setOnClickListener {
                    val intent = Intent(this, MapActivity::class.java)
                    intent.putExtra("mode", "view")
                    intent.putExtra("gps_path", entry.gpsData)
                    startActivity(intent)
                }
            } else {
                viewMapButton.visibility = View.GONE
            }

            deleteButton.setOnClickListener {
                viewModel.deleteEntry(entry.id)
                Toast.makeText(this, "Entry deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    /** Launcher result from MapActivity */
    private val gpsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val gpsJson = result.data?.getStringExtra("gps_json") ?: return@registerForActivityResult
                val dist  = result.data?.getDoubleExtra("gps_distance", 0.0) ?: 0.0
                val dur   = result.data?.getDoubleExtra("gps_duration", 0.0) ?: 0.0

                saveGpsEntry(gpsJson, dist, dur)
            }
        }

    /** Save new GPS entry */
    private fun saveGpsEntry(gpsJson: String) {

        // Read everything returned from MapActivity
        val distance = intent.getDoubleExtra("gps_distance", 0.0)
        val duration = intent.getDoubleExtra("gps_duration", 0.0)
        val activityType = intent.getIntExtra("activity_type", 0)

        // Build the GPS entry
        val entry = ExerciseEntry(
            inputType = ExerciseEntry.INPUT_TYPE_GPS,
            activityType = activityType,
            dateTime = Calendar.getInstance(),
            duration = duration,
            distance = distance,
            calories = 0.0,
            heartRate = 0.0,
            comment = "",
            gpsData = gpsJson
        )

        // Insert into DB
        Thread {
            viewModel.insert(entry)
            runOnUiThread { finish() }
        }.start()
    }



    /** Convert distance based on user preference */
    private fun formatDistance(distanceMiles: Double): String {
        val unit = sharedPrefs.getString("unit_preference", "Miles") ?: "Miles"

        return if (unit == "Imperial" || unit == "Miles") {
            String.format("%.2f Miles", distanceMiles)
        } else {
            val km = distanceMiles * 1.60934
            String.format("%.2f Kilometers", km)
        }
    }

}
