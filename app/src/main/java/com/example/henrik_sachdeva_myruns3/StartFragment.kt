package com.example.henrik_sachdeva_myruns3

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * The StartFragment represents the starting screen of the app.
 * It can be extended to include spinners or other input controls later.
 */
class StartFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate and return the layout for this fragment
        return inflater.inflate(R.layout.fragment_start, container, false)
    }
}
