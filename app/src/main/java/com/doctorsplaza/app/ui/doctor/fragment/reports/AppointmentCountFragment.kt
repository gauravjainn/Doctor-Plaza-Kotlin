package com.doctorsplaza.app.ui.doctor.fragment.reports

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentAppointmentBinding
import com.doctorsplaza.app.databinding.FragmentAppointmentCountBinding
import com.doctorsplaza.app.databinding.FragmentDoctorAppointmentBinding
import com.doctorsplaza.app.ui.doctor.fragment.appointments.adapter.DoctorPastAppointmentsAdapter
import com.doctorsplaza.app.ui.doctor.fragment.appointments.adapter.DoctorsUpcomingAppointmentsAdapter
import com.doctorsplaza.app.ui.patient.fragments.appointments.AppointmentViewModel
import com.doctorsplaza.app.ui.patient.fragments.home.HomeViewModel
import com.doctorsplaza.app.utils.DoctorPlazaLoader
import com.doctorsplaza.app.utils.Resource
import com.doctorsplaza.app.utils.SessionManager
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AppointmentCountFragment : Fragment(R.layout.fragment_appointment_count),
    View.OnClickListener {
    private lateinit var binding: FragmentAppointmentCountBinding

    private val homeViewModel: HomeViewModel by viewModels()

    private val appointmentViewModel: AppointmentViewModel by viewModels()

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var doctorPlazaLoader: DoctorPlazaLoader

    @Inject
    lateinit var doctorPastAppointmentsAdapter: DoctorPastAppointmentsAdapter

    @Inject
    lateinit var doctorUpcomingAppointmentsAdapter: DoctorsUpcomingAppointmentsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_appointment_count, container, false)
            binding = FragmentAppointmentCountBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        doctorPlazaLoader = DoctorPlazaLoader(requireContext())
        setChartData()
    }

    private fun setChartData() {
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(0.2F, ""))
        entries.add(PieEntry(0.40F, ""))
        entries.add(PieEntry(0.10F, ""))

        val pieData = PieDataSet(entries, "")

        val color = ArrayList<Int>()
        color.add(ContextCompat.getColor(requireContext(), R.color.chartColorOne))
        color.add(ContextCompat.getColor(requireContext(), R.color.chartColorTwo))
        color.add(ContextCompat.getColor(requireContext(), R.color.chartColorThree))

        pieData.colors = color
        pieData.setDrawValues(false)

        binding.revenuePieChart.animateXY(500, 500)
        binding.revenuePieChart.setDrawEntryLabels(false)
        binding.revenuePieChart.legend.isEnabled = false
        binding.revenuePieChart.setDrawMarkers(false)
        binding.revenuePieChart.description.isEnabled = false
        binding.revenuePieChart.holeRadius = 65f
        binding.revenuePieChart.transparentCircleRadius = 65f
        binding.revenuePieChart.invalidate()

        binding.revenuePieChart.data = PieData(pieData)


        binding.reschedulePieChart.animateXY(500, 500)
        binding.reschedulePieChart.setDrawEntryLabels(false)
        binding.reschedulePieChart.legend.isEnabled = false
        binding.reschedulePieChart.setDrawMarkers(false)
        binding.reschedulePieChart.description.isEnabled = false
        binding.reschedulePieChart.holeRadius = 65f
        binding.reschedulePieChart.transparentCircleRadius = 65f
        binding.reschedulePieChart.invalidate()

        binding.reschedulePieChart.data = PieData(pieData)


    }

    private fun setObserver() {
        homeViewModel.patientBanner.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.status == 200) {
                    }
                }
                is Resource.Loading -> {
                }
                is Resource.Error -> {
                }
            }
        }
    }

    private fun setOnClickListener() {
        with(binding) {

        }

        doctorUpcomingAppointmentsAdapter.setOnAppointmentClickListener {

        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }
}