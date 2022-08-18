package com.doctorsplaza.app.ui.doctor.fragment.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentDoctoHomeBinding
import com.doctorsplaza.app.ui.doctor.fragment.appointments.adapter.DoctorsUpcomingAppointmentsAdapter
import com.doctorsplaza.app.ui.doctor.fragment.home.model.AppointmentData
import com.doctorsplaza.app.ui.doctor.loginSignUp.model.DoctorDashBoard
import com.doctorsplaza.app.ui.patient.fragments.appointments.AppointmentViewModel
import com.doctorsplaza.app.ui.patient.fragments.bookAppointment.adapter.BookTimeAdapter
import com.doctorsplaza.app.ui.patient.fragments.home.adapter.BannerAdapter
import com.doctorsplaza.app.ui.patient.fragments.home.model.BannerData
import com.doctorsplaza.app.utils.*
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DoctorHomeFragment : Fragment(R.layout.fragment_docto_home), View.OnClickListener {
    private lateinit var binding: FragmentDoctoHomeBinding

    private val doctorHomeViewModel: DoctorHomeViewModel by viewModels()

    @Inject
    lateinit var bannerAdapter: BannerAdapter

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader

    @Inject
    lateinit var bookTimeAdapter: BookTimeAdapter

    @Inject
    lateinit var doctorUpcomingAppointmentAdapter: DoctorsUpcomingAppointmentsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_docto_home, container, false)
            binding = FragmentDoctoHomeBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        hideKeyboard(requireActivity())
        binding.loader.isVisible = true
        appLoader = DoctorPlazaLoader(requireContext())
        doctorHomeViewModel.getDoctorBanners()
        val jsonObject = JsonObject()
        jsonObject.addProperty("id", session.loginId)
        doctorHomeViewModel.getDoctorUpcomingAppointments(json = jsonObject)
        doctorHomeViewModel.getDoctorDashBoardData(json = jsonObject)
        setPullRefresh()
    }

    private fun setPullRefresh() {
        binding.pullToRefresh.setOnRefreshListener {
            val jsonObject = JsonObject()
            jsonObject.addProperty("id", session.loginId)
            doctorHomeViewModel.getDoctorUpcomingAppointments(json = jsonObject)
            binding.pullToRefresh.isRefreshing = false
        }
    }

    private fun setObserver() {
        doctorHomeViewModel.doctorBanner.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.status == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(), response.data.message)
                    } else {
                        if (response.data?.status == 200) {
                            setBanners(response.data.data)
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

        doctorHomeViewModel.appointments.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.status == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(), response.data.message)
                    } else {
                        if (response.data?.success!!) {
                            if (response.data.data.isEmpty()) {
                                binding.appointmentRv.isVisible = false
                                binding.noAppointments.isVisible = true
                                binding.appointmentViewAll.isVisible = false
                            } else {
                                binding.appointmentRv.isVisible = true
                                binding.noAppointments.isVisible = false
                                binding.appointmentViewAll.isVisible = true
                                setAppointmentsData(response.data.data)
                            }
                        }
                    }
                }

                is Resource.Loading -> {
                    appLoader.show()
                }
                is Resource.Error -> {
                    appLoader.dismiss()
                    showToast(response.message.toString())
                }
            }

        }

        doctorHomeViewModel.dashBoard.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.loader.isVisible = false
                    appLoader.dismiss()

                    if (response.data?.status == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(),response.data.message)
                    } else {
                    if (response.data?.success!!) {
                        setDashBoard(response.data)
                    }
                }}
                is Resource.Loading -> {
                    appLoader.show()
                }
                is Resource.Error -> {
                    appLoader.dismiss()
                }
            }

        }
    }

    private fun setAppointmentsData(data: List<AppointmentData>) {
        doctorUpcomingAppointmentAdapter.differ.submitList(data)
        binding.appointmentRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = doctorUpcomingAppointmentAdapter
        }
    }

    private fun setDashBoard(data: DoctorDashBoard) {
        with(binding) {
            totalAppointments.text = data.total_appointments.toString()
            totalClinics.text = data.clinic.toString()
            cancelledAppointments.text = data.cancel_appointments.toString()
            revenue.text = data.revenue.toString()
        }
    }

    private fun setBanners(data: List<BannerData>) {
        bannerAdapter.differ.submitList(data)
        binding.banner.adapter = bannerAdapter
        val tabLayoutMediator =
            TabLayoutMediator(binding.bannerDots, binding.banner, true) { _, _ -> }
        tabLayoutMediator.attach()
    }

    private fun setOnClickListener() {
        with(binding) {
            appointmentViewAll.setOnClickListener(this@DoctorHomeFragment)
        }


        doctorUpcomingAppointmentAdapter.setOnAppointmentClickListener {
            val bundle = Bundle().apply {
                putString("appointId", it._id)
            }
            findNavController().navigate(R.id.doctorAppointmentDetailsFragment, bundle)
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.appointmentViewAll -> {
                findNavController().navigate(R.id.doctorAppointmentFragment)
            }
        }
    }


    override fun onResume() {
        hideKeyboard(requireActivity())
        super.onResume()
        setObserver()
    }
}
