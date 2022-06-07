package com.doctorsplaza.app.ui.doctor.fragment.reports.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.doctorsplaza.app.ui.doctor.fragment.reports.AppointmentCountFragment
import com.doctorsplaza.app.ui.doctor.fragment.reports.FinancialFragment
import com.doctorsplaza.app.ui.doctor.fragment.reports.RentStatusFragment

class RevenueFragmentAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AppointmentCountFragment()
            1 -> FinancialFragment()
            2 -> RentStatusFragment()
            else -> {
                AppointmentCountFragment()
            }
        }
    }

    override fun getItemCount(): Int {
        return 3
    }
}