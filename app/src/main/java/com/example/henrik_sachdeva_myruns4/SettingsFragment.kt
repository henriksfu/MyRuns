package com.example.henrik_sachdeva_myruns4

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<Preference>("user_profile")?.setOnPreferenceClickListener {
            startActivity(Intent(requireContext(), MainActivity::class.java))
            true
        }

        findPreference<Preference>("unit_preference")?.setOnPreferenceClickListener {
            UnitPreferenceDialogFragment()
                .show(parentFragmentManager, "unit_preference_dialog")
            true
        }

        findPreference<Preference>("comments")?.setOnPreferenceClickListener {
            CommentsDialogFragment()
                .show(parentFragmentManager, "comments_dialog")
            true
        }

        findPreference<Preference>("webpage")?.setOnPreferenceClickListener {
            val url = "https://www.sfu.ca/computing.html"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            true
        }
    }
}
