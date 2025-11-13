package com.example.henrik_sachdeva_myruns3

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.henrik_sachdeva_myruns3.database.ExerciseEntry
import com.example.henrik_sachdeva_myruns3.database.ExerciseEntryDatabase
import com.example.henrik_sachdeva_myruns3.database.ExerciseEntryDatabaseDao
import com.example.henrik_sachdeva_myruns3.database.ExerciseRepository
import com.example.henrik_sachdeva_myruns3.database.ExerciseViewModel
import com.example.henrik_sachdeva_myruns3.database.ExerciseViewModelFactory
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.util.Calendar

class TabMainActivity : AppCompatActivity() {

    private lateinit var viewPager2: ViewPager2
    private lateinit var tabLayout: TabLayout

    // Room + ViewModel components (needed for GPS save)
    private lateinit var database: ExerciseEntryDatabase
    private lateinit var dao: ExerciseEntryDatabaseDao
    private lateinit var repository: ExerciseRepository
    private lateinit var viewModel: ExerciseViewModel

    /** ActivityResult launcher for GPS MapActivity */
    private val gpsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK && result.data != null) {

                val json = result.data!!.getStringExtra("gps_json") ?: return@registerForActivityResult
                val distance = result.data!!.getDoubleExtra("gps_distance", 0.0)
                val duration = result.data!!.getDoubleExtra("gps_duration", 0.0)
                val activityType = result.data!!.getIntExtra("gps_activity_type", 0)

                val entry = ExerciseEntry(
                    id = 0L,
                    inputType = 2,          // GPS
                    activityType = activityType,
                    dateTime = Calendar.getInstance(),
                    duration = duration,
                    distance = distance,
                    calories = 0.0,         // MyRuns4: set later in MyRuns5
                    heartRate = 0.0,
                    comment = json          // store GPS JSON in comment for now
                )

                viewModel.insertEntry(entry)

                Toast.makeText(this, "GPS Entry Saved!", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tab_main)

        // Room Initialization
        database = ExerciseEntryDatabase.getInstance(this)
        dao = database.exerciseEntryDatabaseDao
        repository = ExerciseRepository(dao)
        val factory = ExerciseViewModelFactory(repository)
        viewModel = factory.create(ExerciseViewModel::class.java)

        setUpTabs()
    }

    private fun setUpTabs() {
        viewPager2 = findViewById(R.id.viewpager)
        tabLayout = findViewById(R.id.tablayout)

        val fragments = arrayListOf(
            StartFragment(),
            HistoryFragment(),
            SettingsFragment()
        )

        val fragmentAdapter = MyFragmentStateAdapter(this, fragments)
        viewPager2.adapter = fragmentAdapter

        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            tab.text = when (position) {
                0 -> "Start"
                1 -> "History"
                2 -> "Settings"
                else -> ""
            }
        }.attach()
    }

    /** Called when user taps “Start” button in StartFragment */
    fun onStartButtonClicked(view: View?) {

        val inputTypeSpinner = findViewById<Spinner>(R.id.input_type_spinner)
        val activityTypeSpinner = findViewById<Spinner>(R.id.activity_type_spinner)

        val inputType = inputTypeSpinner.selectedItem.toString()
        val activityTypeId = activityTypeSpinner.selectedItemId.toInt()

        when (inputType) {

            "Manual Entry" -> {
                val intent = Intent(this, ManualEntryActivity::class.java).apply {
                    putExtra("SELECTED_ACTIVITY_TYPE_ID", activityTypeId)
                }
                startActivity(intent)
            }

            "GPS" -> {
                val intent = Intent(this, EntryActivity::class.java).apply {
                    putExtra("MODE", "new_gps")
                    putExtra("ACTIVITY_TYPE_ID", selectedActivityTypeId)
                }
                startActivity(intent)
            }


            "Automatic" -> {
                val intent = Intent(this, MapActivity::class.java).apply {
                    putExtra("mode", "record")
                    putExtra("gps_activity_type", activityTypeId)
                }
                gpsLauncher.launch(intent)
            }
        }
    }
}
