package com.doctorsplaza.app.ui.doctor.fragment.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentDoctorProfileBinding
import com.doctorsplaza.app.databinding.FragmentEditDoctorProfileBinding
import com.doctorsplaza.app.ui.doctor.fragment.clinics.adapter.DoctorClinicsAdapter
import com.doctorsplaza.app.ui.patient.fragments.appointments.AppointmentViewModel
import com.doctorsplaza.app.ui.patient.fragments.bookAppointment.adapter.BookTimeAdapter
import com.doctorsplaza.app.ui.patient.fragments.home.HomeViewModel
import com.doctorsplaza.app.utils.DoctorPlazaLoader
import com.doctorsplaza.app.utils.Resource
import com.doctorsplaza.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EditDoctorProfileFragment : Fragment(R.layout.fragment_edit_doctor_profile), View.OnClickListener {
    private lateinit var binding: FragmentEditDoctorProfileBinding

    private val homeViewModel: HomeViewModel by viewModels()

    private val appointmentViewModel: AppointmentViewModel by viewModels()

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var doctorPlazaLoader: DoctorPlazaLoader

    @Inject
    lateinit var bookTimeAdapter: BookTimeAdapter

    @Inject
    lateinit var clinicDoctorsAdapter: DoctorClinicsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_edit_doctor_profile, container, false)
            binding = FragmentEditDoctorProfileBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        doctorPlazaLoader = DoctorPlazaLoader(requireContext())
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

        clinicDoctorsAdapter.setOnViewAppointments {

        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }
}
