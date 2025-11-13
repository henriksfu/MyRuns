package com.example.henrik_sachdeva_myruns3

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

/**
 * Fragment that manages user preferences and related actions.
 */
class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        // Open user profile activity
        findPreference<Preference>("user_profile")?.setOnPreferenceClickListener {
            startActivity(Intent(activity, MainActivity::class.java))
            true
        }

        // Open unit preference dialog
        findPreference<Preference>("unit_preference")?.setOnPreferenceClickListener {
            val dialog = UnitPreferenceDialogFragment()
            dialog.show(parentFragmentManager, "unit_preference_dialog")
            true
        }

        // Open comments dialog
        findPreference<Preference>("comments")?.setOnPreferenceClickListener {
            val dialog = CommentsDialogFragment()
            dialog.show(parentFragmentManager, "comments_dialog")
            true
        }

        // Open SFU Computing Science webpage
        findPreference<Preference>("webpage")?.setOnPreferenceClickListener {
            val url = "https://www.sfu.ca/computing.html"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
            true
        }
    }
}
