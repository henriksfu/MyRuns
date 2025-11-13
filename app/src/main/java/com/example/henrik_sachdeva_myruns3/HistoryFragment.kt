package com.example.henrik_sachdeva_myruns3

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
import com.example.henrik_sachdeva_myruns3.database.*

class HistoryFragment : Fragment(), OnEntryClickListener {

    private lateinit var adapter: ExerciseEntryListAdapter
    private lateinit var viewModel: ExerciseViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_history, container, false)
        val recycler = view.findViewById<RecyclerView>(R.id.recycler_view)

        val db = ExerciseEntryDatabase.getInstance(requireContext()).exerciseEntryDatabaseDao
        val repo = ExerciseRepository(db)
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

        val intent = Intent(requireContext(), EntryActivity::class.java)
        intent.putExtra("ENTRY_ID", entry.id)
        startActivity(intent)
    }
}
