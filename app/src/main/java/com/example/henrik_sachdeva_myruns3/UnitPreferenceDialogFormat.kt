package com.example.henrik_sachdeva_myruns3

import android.app.Dialog
import android.os.Bundle
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager

/**
 * Dialog fragment for selecting and saving the user’s preferred unit system.
 */
class UnitPreferenceDialogFragment : DialogFragment() {

    private lateinit var radioGroup: RadioGroup

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val view = requireActivity().layoutInflater.inflate(
            R.layout.fragment_unit_preference_dialog,
            null
        )

        radioGroup = view.findViewById(R.id.radioGroupUnit)

        // Load current preference
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val currentUnit = sharedPreferences.getString("unit_preference", "Metric")

        // Pre-select the current preference
        if (currentUnit == "Imperial") {
            radioGroup.check(R.id.radioButtonImperial)
        } else {
            radioGroup.check(R.id.radioButtonMetric)
        }

        builder.setView(view)
            .setTitle("Unit Preference")
            .setPositiveButton("OK") { _, _ ->
                // Save the selected unit
                val selectedUnit = if (radioGroup.checkedRadioButtonId == R.id.radioButtonMetric)
                    "Metric" else "Imperial"

                with(sharedPreferences.edit()) {
                    putString("unit_preference", selectedUnit)
                    apply()
                }

                Toast.makeText(
                    activity,
                    "Unit preference saved: $selectedUnit",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)

        return builder.create()
    }
}
