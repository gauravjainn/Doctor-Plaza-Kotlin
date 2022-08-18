package com.doctorsplaza.app.ui.doctor.fragment.appointments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentDoctorAppointmentBinding
import com.doctorsplaza.app.ui.doctor.fragment.appointments.adapter.DoctorPastAppointmentsAdapter
import com.doctorsplaza.app.ui.doctor.fragment.appointments.adapter.DoctorsUpcomingAppointmentsAdapter
import com.doctorsplaza.app.ui.doctor.fragment.home.DoctorHomeViewModel
import com.doctorsplaza.app.ui.doctor.fragment.home.model.AppointmentData
import com.doctorsplaza.app.utils.DoctorPlazaLoader
import com.doctorsplaza.app.utils.Resource
import com.doctorsplaza.app.utils.SessionManager
import com.doctorsplaza.app.utils.logOutUnAuthorized
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DoctorAppointmentFragment : Fragment(R.layout.fragment_doctor_appointment) {
    private lateinit var binding: FragmentDoctorAppointmentBinding

    private val doctorHomeViewModel: DoctorHomeViewModel by viewModels()

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader

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
            currentView = inflater.inflate(R.layout.fragment_doctor_appointment, container, false)
            binding = FragmentDoctorAppointmentBinding.bind(currentView!!)
            init()
            setObserver()
            setPullRefresh()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())
        val jsonObject = JsonObject()
        jsonObject.addProperty("id", session.loginId)
        doctorHomeViewModel.getDoctorUpcomingAppointments(jsonObject)
        doctorHomeViewModel.getDoctorPastAppointments(jsonObject)

    }

    private fun setPullRefresh() {
        binding.pullToRefresh.setOnRefreshListener {
            val jsonObject = JsonObject()
            jsonObject.addProperty("id", session.loginId)
            doctorHomeViewModel.getDoctorUpcomingAppointments(jsonObject)
            doctorHomeViewModel.getDoctorPastAppointments(jsonObject)
            binding.pullToRefresh.isRefreshing = false
        }
    }

    private fun setObserver() {
        doctorHomeViewModel.appointments.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.status == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(), response.data.message)
                    } else {
                        if (response.data?.success!!) {
                            if (response.data.data.isEmpty()) {
                                binding.appointmentRv.isVisible = false
                                binding.noAppointments.isVisible = true
                            } else {
                                binding.appointmentRv.isVisible = true
                                binding.noAppointments.isVisible = false
                                setUpcomingAppointmentsData(response.data.data)
                            }
                        }
                    }
                }
                is Resource.Loading -> {
                    appLoader.show()
                }
                is Resource.Error -> {
                    appLoader.dismiss()
                }
            }
        }

        doctorHomeViewModel.pastAppointments.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.status == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(), response.data.message)
                    } else {
                        if (response.data?.success!!) {
                            appLoader.dismiss()

                            if (response.data.data.isEmpty()) {
                                binding.noPastAppointmentsDataLbl.isVisible = true
                                binding.noPastAppointmentsDataLbl.text =
                                    "There is no appointments to show."
                            } else {
                                binding.servicesGroup.isVisible = false
                                setPastAppointmentsData(response.data.data)
                            }

                        }
                    }
                }
                is Resource.Loading -> {
                    appLoader.show()
                }
                is Resource.Error -> {
                    appLoader.dismiss()
                }
            }
        }

    }


    private fun setUpcomingAppointmentsData(data: List<AppointmentData>) {
        doctorUpcomingAppointmentsAdapter.differ.submitList(data)
        binding.appointmentRv.apply {
            setHasFixedSize(true)
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = doctorUpcomingAppointmentsAdapter
        }
    }

    private fun setPastAppointmentsData(data: List<AppointmentData>) {
        doctorPastAppointmentsAdapter.differ.submitList(data)
        binding.pastAppointmentsRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = doctorPastAppointmentsAdapter
        }
    }

    private fun setOnClickListener() {

        doctorUpcomingAppointmentsAdapter.setOnAppointmentClickListener {
            val bundle = Bundle().apply {
                putString("appointId", it._id)
            }
            findNavController().navigate(R.id.doctorAppointmentDetailsFragment, bundle)
        }
        doctorPastAppointmentsAdapter.setOnPastAppointmentClickListener {
            val bundle = Bundle().apply {
                putString("appointId", it._id)
            }
            findNavController().navigate(R.id.doctorAppointmentDetailsFragment, bundle)
        }
    }

    override fun onResume() {
        setObserver()
        super.onResume()
    }
}