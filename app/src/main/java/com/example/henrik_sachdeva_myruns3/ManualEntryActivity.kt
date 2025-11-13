package com.example.henrik_sachdeva_myruns3

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.henrik_sachdeva_myruns3.database.*
import java.util.Calendar

class ManualEntryActivity : AppCompatActivity() {

    private val infoItems = arrayOf(
        "Date", "Time", "Duration", "Distance", "Calories", "Heart Rate", "Comment"
    )

    private lateinit var myListView: ListView
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    private lateinit var viewModel: ExerciseViewModel

    private val selectedDateTime: Calendar = Calendar.getInstance()

    private var duration: Double? = null
    private var distance: Double? = null
    private var calories: Int? = null
    private var heartRate: Int? = null
    private var comment: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_entry)

        myListView = findViewById(R.id.myListView)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)

        // Initialize Room + ViewModel
        val db = ExerciseEntryDatabase.getInstance(this)
        val dao = db.exerciseEntryDatabaseDao()
        val repo = ExerciseRepository(dao)
        val factory = ExerciseViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory)[ExerciseViewModel::class.java]

        myListView.adapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, infoItems)

        cancelButton.setOnClickListener { finish() }
        saveButton.setOnClickListener { saveEntry() }

        myListView.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> showDatePickerDialog()
                1 -> showTimePickerDialog()
                2 -> showEditNumberDialog("Duration")
                3 -> showEditNumberDialog("Distance")
                4 -> showEditNumberDialog("Calories")
                5 -> showEditNumberDialog("Heart Rate")
                6 -> showEditTextDialog("Comment")
            }
        }
    }

    private fun saveEntry() {

        val selectedActivityTypeId =
            intent.getIntExtra("SELECTED_ACTIVITY_TYPE_ID", -1)

        val newEntry = ExerciseEntry(
            id = 0L,
            inputType = ExerciseEntry.INPUT_TYPE_MANUAL,
            activityType = selectedActivityTypeId,
            dateTime = selectedDateTime.time,

            duration = (duration ?: 0.0).toInt(),    // FIXED: now Int
            distance = distance ?: 0.0,              // stays Double

            calories = calories ?: 0,                // FIXED: Int
            heartRate = (heartRate ?: 0),              // FIXED: Int

            comment = comment ?: ""
        )

        try {
            viewModel.insertEntry(newEntry)
            Toast.makeText(this, "Entry saved", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("ManualEntryActivity", "Error saving entry: ${e.message}")
        }

        finish()
    }

    private fun showDatePickerDialog() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedDateTime.set(Calendar.YEAR, year)
                selectedDateTime.set(Calendar.MONTH, month)
                selectedDateTime.set(Calendar.DAY_OF_MONTH, day)
            },
            selectedDateTime.get(Calendar.YEAR),
            selectedDateTime.get(Calendar.MONTH),
            selectedDateTime.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePickerDialog() {
        TimePickerDialog(
            this,
            { _, hour, minute ->
                selectedDateTime.set(Calendar.HOUR_OF_DAY, hour)
                selectedDateTime.set(Calendar.MINUTE, minute)
            },
            selectedDateTime.get(Calendar.HOUR_OF_DAY),
            selectedDateTime.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun showEditNumberDialog(title: String) {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val value = input.text.toString()
                when (title) {
                    "Duration" -> duration = value.toDoubleOrNull()
                    "Distance" -> distance = value.toDoubleOrNull()
                    "Calories" -> calories = value.toIntOrNull()
                    "Heart Rate" -> heartRate = value.toIntOrNull()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showEditTextDialog(title: String) {
        val input = EditText(this)

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val value = input.text.toString()
                comment = value
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
