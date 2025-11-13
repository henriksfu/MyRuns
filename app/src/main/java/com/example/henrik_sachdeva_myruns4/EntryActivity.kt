package com.example.henrik_sachdeva_myruns4

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.example.henrik_sachdeva_myruns4.database.*

class EntryActivity : AppCompatActivity() {

    private lateinit var viewModel: ExerciseViewModel
    private lateinit var sharedPrefs: SharedPreferences

    private var entryId: Long = -1L

    private lateinit var deleteButton: Button
    private lateinit var backButton: Button
    private lateinit var viewMapButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)

        deleteButton = findViewById(R.id.delete_button)
        backButton = findViewById(R.id.cancel_button)
        viewMapButton = findViewById(R.id.view_map_button)

        entryId = intent.getLongExtra("ENTRY_ID", -1L)

        initViewModel()
        observeEntry()

        backButton.setOnClickListener { finish() }
    }

    private fun initViewModel() {
        val dao = ExerciseEntryDatabase.getInstance(this).exerciseEntryDatabaseDao()
        val repo = ExerciseRepository(dao)
        val factory = ExerciseViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory)[ExerciseViewModel::class.java]
    }

    private fun observeEntry() {
        viewModel.getEntryById(entryId).observe(this, Observer { entry ->
            entry ?: return@Observer

            findViewById<TextView>(R.id.tv_entry_type).text = entry.getEntryTypeString()
            findViewById<TextView>(R.id.tv_activity_type).text = entry.getActivityTypeString()
            findViewById<TextView>(R.id.tv_date_time).text = entry.formattedDateTime()
            findViewById<TextView>(R.id.tv_duration).text = entry.formattedDuration()
            findViewById<TextView>(R.id.tv_distance).text = formatDistance(entry.distance)
            findViewById<TextView>(R.id.tv_calories).text = "${entry.calories} cals"
            findViewById<TextView>(R.id.tv_heart_rate).text = "${entry.heartRate} bpm"

            if (entry.inputType == ExerciseEntry.INPUT_TYPE_GPS ||
                entry.inputType == ExerciseEntry.INPUT_TYPE_AUTOMATIC
            ) {
                viewMapButton.visibility = View.VISIBLE
                viewMapButton.setOnClickListener {
                    startActivity(
                        Intent(this, MapActivity::class.java).apply {
                            putExtra("mode", "history")
                            putExtra("entry_id", entry.id)
                        }
                    )
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

    private fun formatDistance(distanceKm: Double): String {
        val unit = sharedPrefs.getString("unit_preference", "Miles") ?: "Miles"
        return if (unit == "Miles" || unit == "Imperial") {
            val miles = distanceKm / 1.60934
            String.format("%.2f Miles", miles)
        } else {
            String.format("%.2f Kilometers", distanceKm)
        }
    }
}
