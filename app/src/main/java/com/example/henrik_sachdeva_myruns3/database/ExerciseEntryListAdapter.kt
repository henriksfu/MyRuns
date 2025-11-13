package com.example.henrik_sachdeva_myruns3.database

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.henrik_sachdeva_myruns3.R

interface OnEntryClickListener {
    fun onEntryClick(entry: ExerciseEntry)
}

class ExerciseEntryListAdapter(
    private val listener: OnEntryClickListener,
    context: Context
) : ListAdapter<ExerciseEntry, ExerciseEntryListAdapter.EntryViewHolder>(ExerciseEntryDiffCallback()) {

    private val sharedPrefs: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    private val unitPref: String =
        sharedPrefs.getString("unit_preference", "Miles") ?: "Miles"

    class EntryViewHolder(itemView: View, private val unitPref: String) :
        RecyclerView.ViewHolder(itemView) {

        private val entryText: TextView = itemView.findViewById(R.id.tv_entry)

        fun bind(entry: ExerciseEntry) {
            val date = entry.getFormattedDateTime()
            val entryType = entry.getEntryTypeString()
            val activity = entry.getActivityTypeString()
            val duration = entry.getFormattedDuration()

            // Convert miles → km only when user preference says Metric
            val distanceText = if (unitPref == "Imperial" || unitPref == "Miles") {
                String.format("%.2f Miles", entry.distance)
            } else {
                val km = entry.distance * 1.60934
                String.format("%.2f Kilometers", km)
            }

            entryText.text =
                "$entryType, $activity, $date\n$distanceText, $duration"
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_layout_adapter, parent, false)
        return EntryViewHolder(view, unitPref)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val entry = getItem(position)
        holder.bind(entry)
        holder.itemView.setOnClickListener { listener.onEntryClick(entry) }
    }
}

class ExerciseEntryDiffCallback : DiffUtil.ItemCallback<ExerciseEntry>() {
    override fun areItemsTheSame(old: ExerciseEntry, new: ExerciseEntry): Boolean {
        return old.id == new.id
    }

    override fun areContentsTheSame(old: ExerciseEntry, new: ExerciseEntry): Boolean {
        return old == new
    }
}
