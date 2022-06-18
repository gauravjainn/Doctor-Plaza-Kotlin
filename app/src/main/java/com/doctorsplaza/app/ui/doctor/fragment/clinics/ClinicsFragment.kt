package com.doctorsplaza.app.ui.doctor.fragment.clinics

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
import com.doctorsplaza.app.databinding.FragmentClinicBinding
import com.doctorsplaza.app.ui.doctor.fragment.clinics.adapter.DoctorClinicsAdapter
import com.doctorsplaza.app.ui.doctor.fragment.clinics.model.ClinicData
import com.doctorsplaza.app.ui.patient.fragments.bookAppointment.adapter.BookTimeAdapter
import com.doctorsplaza.app.utils.DoctorPlazaLoader
import com.doctorsplaza.app.utils.Resource
import com.doctorsplaza.app.utils.SessionManager
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ClinicsFragment : Fragment(R.layout.fragment_clinic) {
    private lateinit var binding: FragmentClinicBinding

    private val clinicViewModel: ClinicsViewModel by viewModels()

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader

    @Inject
    lateinit var bookTimeAdapter: BookTimeAdapter

    @Inject
    lateinit var clinicDoctorsAdapter: DoctorClinicsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_clinic, container, false)
            binding = FragmentClinicBinding.bind(currentView!!)
            init()
            setObserver()
            setOnAdapterClickListener()
        }
        return currentView!!
    }


    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())
        binding.loading.isVisible = true
        val jsonObject = JsonObject()
        jsonObject.addProperty("id", session.loginId)
        clinicViewModel.getClinicsList(jsonObject)

    }

    private fun setObserver() {
        clinicViewModel.clinicsList.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    binding.errorMessage.isVisible = false
                    binding.loading.isVisible = false
                    if (response.data?.success!!) {
                        if (response.data.data.isEmpty()) {
                            binding.noData.isVisible = true
                        } else {
                            binding.noData.isVisible = false
                            setClinicsData(response.data.data)
                        }
                    }
                }
                is Resource.Loading -> {
                    appLoader.show()
                }
                is Resource.Error -> {
                    binding.errorMessage.isVisible = true
                    appLoader.dismiss()
                }
            }
        }

    }

    private fun setClinicsData(data: List<ClinicData>) {
        clinicDoctorsAdapter.differ.submitList(data)
        binding.clinicRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = clinicDoctorsAdapter
        }
    }

    private fun setOnAdapterClickListener() {
        clinicDoctorsAdapter.setOnViewAppointments {
            findNavController().navigate(R.id.doctorAppointmentFragment)
        }

        clinicDoctorsAdapter.setOnViewClinicDetails {
            val bundle = Bundle().apply {
                putString("clinicId", it?._id)
            }
            findNavController().navigate(R.id.clinicDetailsFragment, bundle)
        }
    }

}
