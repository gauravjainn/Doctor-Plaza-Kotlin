package com.doctorsplaza.app.ui.doctor.fragment.appointments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentClinicBinding
import com.doctorsplaza.app.databinding.FragmentDoctoHomeBinding
import com.doctorsplaza.app.databinding.FragmentDoctorAppointmentBinding
import com.doctorsplaza.app.ui.doctor.fragment.appointments.adapter.DoctorPastAppointmentsAdapter
import com.doctorsplaza.app.ui.doctor.fragment.appointments.adapter.DoctorsUpcomingAppointmentsAdapter
import com.doctorsplaza.app.ui.doctor.fragment.clinics.adapter.DoctorClinicsAdapter
import com.doctorsplaza.app.ui.patient.fragments.appointments.AppointmentViewModel
import com.doctorsplaza.app.ui.patient.fragments.appointments.adapter.PastAppointmentsAdapter
import com.doctorsplaza.app.ui.patient.fragments.bookAppointment.adapter.BookTimeAdapter
import com.doctorsplaza.app.ui.patient.fragments.home.HomeViewModel
import com.doctorsplaza.app.ui.patient.fragments.home.adapter.UpcomingAppointmentsAdapter
import com.doctorsplaza.app.utils.DoctorPlazaLoader
import com.doctorsplaza.app.utils.Resource
import com.doctorsplaza.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DoctorAppointmentFragment : Fragment(R.layout.fragment_doctor_appointment) , View.OnClickListener {
    private lateinit var binding: FragmentDoctorAppointmentBinding

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_doctor_appointment, container, false)
            binding = FragmentDoctorAppointmentBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        doctorPlazaLoader = DoctorPlazaLoader(requireContext())
        setAppointmentsData()

    }

    private fun setObserver() {
        homeViewModel.patientBanner.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.status == 200) {
                        setAppointmentsData()
                    }
                }
                is Resource.Loading -> {
                }
                is Resource.Error -> {
                }
            }
        }

    }

    private fun setAppointmentsData() {

        binding.appointmentRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
            adapter = doctorUpcomingAppointmentsAdapter
        }

        binding.pastAppointmentsRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = doctorPastAppointmentsAdapter
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