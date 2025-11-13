package com.example.henrik_sachdeva_myruns3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.Fragment

class StartFragment : Fragment() {

    private lateinit var inputTypeSpinner: Spinner
    private lateinit var activityTypeSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_start, container, false)

        inputTypeSpinner = view.findViewById(R.id.spinner_input_type)
        activityTypeSpinner = view.findViewById(R.id.spinner_activity_type)

        val startButton: Button = view.findViewById(R.id.start_button)

        startButton.setOnClickListener {

            val inputType = inputTypeSpinner.selectedItem.toString()
            val activityType = activityTypeSpinner.selectedItemPosition

            (activity as? TabMainActivity)
                ?.startExercise(inputType, activityType)
        }

        return view
    }
}

