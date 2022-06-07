package com.doctorsplaza.app.ui.doctor.fragment.reports

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentRevenueReportsBinding
import com.doctorsplaza.app.ui.doctor.fragment.reports.adapter.RevenueFragmentAdapter
import com.google.android.material.tabs.TabLayout


class RevenueReportsFragment : Fragment(R.layout.fragment_revenue_reports), View.OnClickListener {
    private lateinit var binding: FragmentRevenueReportsBinding

    private lateinit var currentView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (!this::currentView.isInitialized) {
            currentView = inflater.inflate(R.layout.fragment_revenue_reports, container, false)
            binding = FragmentRevenueReportsBinding.bind(currentView)
            setOnClickListener()
            setTabLayoutAndViewPager()
        }
        return currentView
    }

    private fun setTabLayoutAndViewPager() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Appointment Count"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Financial Count"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Rent Status"))


        val fragmentManager = childFragmentManager
        val viewPagerAdapter = RevenueFragmentAdapter(fragmentManager, lifecycle)
        binding.reportsVp.adapter = viewPagerAdapter

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.reportsVp.currentItem = tab!!.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })


        binding.reportsVp.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })

    }

    private fun setOnClickListener() {
        with(binding) {

        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }

}