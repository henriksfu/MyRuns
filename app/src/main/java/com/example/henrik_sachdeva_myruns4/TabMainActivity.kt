package com.example.henrik_sachdeva_myruns4

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.henrik_sachdeva_myruns4.database.*
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson

class TabMainActivity : AppCompatActivity() {

    private lateinit var viewPager2: ViewPager2
    private lateinit var tabLayout: TabLayout

    private lateinit var dao: ExerciseEntryDatabaseDao
    private lateinit var repository: ExerciseRepository
    private lateinit var viewModel: ExerciseViewModel

    private lateinit var gpsLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tab_main)

        initViewModel()
        setupGpsLauncher()
        setupTabs()
    }

    private fun initViewModel() {
        dao = ExerciseEntryDatabase.getInstance(this).exerciseEntryDatabaseDao()
        repository = ExerciseRepository(dao)
        val factory = ExerciseViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ExerciseViewModel::class.java]
    }

    private fun setupGpsLauncher() {
        gpsLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val data = result.data
            if (result.resultCode == RESULT_OK && data != null) {
                val json = data.getStringExtra("entry_json")
                if (!json.isNullOrEmpty()) {
                    val entry = Gson().fromJson(json, ExerciseEntry::class.java)
                    viewModel.insertEntry(entry)
                }
            }
        }
    }

    fun startExercise(inputType: String, activityTypeId: Int) {
        when (inputType.lowercase()) {

            "manual entry" -> {
                val intent = Intent(this, ManualEntryActivity::class.java)
                intent.putExtra("activity_type", activityTypeId)
                startActivity(intent)
            }

            "gps" -> {
                val intent = Intent(this, MapActivity::class.java).apply {
                    putExtra("mode", "live")
                    putExtra("inputType", ExerciseEntry.INPUT_TYPE_GPS)
                    putExtra("activityType", activityTypeId)
                }
                gpsLauncher.launch(intent)
            }

            "automatic" -> {
                val intent = Intent(this, MapActivity::class.java).apply {
                    putExtra("mode", "live")
                    putExtra("inputType", ExerciseEntry.INPUT_TYPE_AUTOMATIC)
                    putExtra("activityType", activityTypeId)
                }
                startActivity(intent)
            }
        }
    }

    private fun setupTabs() {
        viewPager2 = findViewById(R.id.viewpager)
        tabLayout = findViewById(R.id.tablayout)

        val fragments = listOf(
            StartFragment(),
            HistoryFragment(),
            SettingsFragment()
        )

        viewPager2.adapter = MyFragmentStateAdapter(this, fragments)

        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            tab.text = when (position) {
                0 -> "Start"
                1 -> "History"
                2 -> "Settings"
                else -> ""
            }
        }.attach()
    }
}
