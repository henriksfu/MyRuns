package com.example.henrik_sachdeva_myruns4

import android.app.Dialog
import android.os.Bundle
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager

class UnitPreferenceDialogFragment : DialogFragment() {

    private lateinit var radioGroup: RadioGroup

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity()
            .layoutInflater
            .inflate(R.layout.fragment_unit_preference_dialog, null)

        radioGroup = view.findViewById(R.id.radioGroupUnit)

        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val current = prefs.getString("unit_preference", "Metric")

        if (current == "Imperial") {
            radioGroup.check(R.id.radioButtonImperial)
        } else {
            radioGroup.check(R.id.radioButtonMetric)
        }

        return AlertDialog.Builder(requireActivity())
            .setTitle("Unit Preference")
            .setView(view)
            .setPositiveButton("OK") { _, _ ->
                val selected =
                    if (radioGroup.checkedRadioButtonId == R.id.radioButtonMetric)
                        "Metric"
                    else
                        "Imperial"

                prefs.edit().putString("unit_preference", selected).apply()

                Toast.makeText(activity, "Unit preference saved: $selected", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
}
