package com.doctorsplaza.app.ui.doctor.fragment.reports

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentRentStatusBinding
import com.doctorsplaza.app.ui.doctor.fragment.reports.adapter.RentStatusAdapter
import com.doctorsplaza.app.ui.doctor.fragment.reports.model.RentData
import com.doctorsplaza.app.ui.doctor.fragment.reports.viewModel.RevenueViewModel
import com.doctorsplaza.app.utils.*
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RentStatusFragment : Fragment(R.layout.fragment_rent_status) {

    private lateinit var binding: FragmentRentStatusBinding

    private val revenueViewModel: RevenueViewModel by viewModels()

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader

    @Inject
    lateinit var rentStatusAdapter: RentStatusAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_rent_status, container, false)
            binding = FragmentRentStatusBinding.bind(currentView!!)
            init()
            setObserver()
        }
        return currentView!!
    }


    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())
        binding.loader.isVisible = true
        val jsonObject = JsonObject()
        jsonObject.addProperty("id", session.loginId)
        revenueViewModel.getRentStatus(jsonObject)
    }

    private fun setObserver() {
        revenueViewModel.rentStatus.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.loader.isVisible = false
                    binding.noDataMsg.isVisible = false
                    appLoader.dismiss()
                    if (response.data?.status == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(),response.data.message)
                    } else {
                    if (response.data?.success!!) {
                        if (response.data.data.isEmpty()) {
                            binding.noDataMsg.isVisible = true
                        } else {
                            binding.noDataMsg.isVisible = false
                            setRentData(response.data.data)
                        }
                    } else {
                        showToast(response.data.message)
                    }
                }}
                is Resource.Loading -> {
                    appLoader.show()
                }
                is Resource.Error -> {
                    appLoader.dismiss()
                    showToast(response.message.toString())
                }
            }
        }
    }

    private fun setRentData(data: List<RentData>) {
        rentStatusAdapter.differ.submitList(data)
        binding.rentStatusRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rentStatusAdapter
        }
    }
}