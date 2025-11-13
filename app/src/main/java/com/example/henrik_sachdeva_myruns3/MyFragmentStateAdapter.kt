package com.example.henrik_sachdeva_myruns3

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Adapter for managing a dynamic list of fragments in a ViewPager2.
 */
class MyFragmentStateAdapter(
    activity: FragmentActivity,
    private val fragments: ArrayList<Fragment>
) : FragmentStateAdapter(activity) {

    override fun createFragment(position: Int): Fragment {
        // Return the fragment corresponding to the selected position
        return fragments[position]
    }

    override fun getItemCount(): Int {
        // Total number of fragments to display
        return fragments.size
    }
}
