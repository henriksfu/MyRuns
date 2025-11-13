package com.example.henrik_sachdeva_myruns4

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.henrik_sachdeva_myruns4.database.*

class HistoryFragment : Fragment(), OnEntryClickListener {

    private lateinit var adapter: ExerciseEntryListAdapter
    private lateinit var viewModel: ExerciseViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_history, container, false)
        val recycler = view.findViewById<RecyclerView>(R.id.recycler_view)

        val dao = ExerciseEntryDatabase
            .getInstance(requireContext())
            .exerciseEntryDatabaseDao()

        val repo = ExerciseRepository(dao)
        val factory = ExerciseViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory)[ExerciseViewModel::class.java]

        adapter = ExerciseEntryListAdapter(this, requireContext())
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(requireContext())

        viewModel.allEntries.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        return view
    }

    override fun onEntryClick(entry: ExerciseEntry) {
        Log.d("HistoryFragment", "Clicked entry: ${entry.id}")

        when (entry.inputType) {
            ExerciseEntry.INPUT_TYPE_MANUAL -> {
                startActivity(
                    Intent(requireContext(), EntryActivity::class.java).apply {
                        putExtra("ENTRY_ID", entry.id)
                    }
                )
            }

            ExerciseEntry.INPUT_TYPE_GPS,
            ExerciseEntry.INPUT_TYPE_AUTOMATIC -> {
                startActivity(
                    Intent(requireContext(), MapActivity::class.java).apply {
                        putExtra("mode", "history")
                        putExtra("entry_id", entry.id)
                    }
                )
            }
        }
    }
}
