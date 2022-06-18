package com.doctorsplaza.app.ui.doctor.fragment.clinics

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentClinicDetailsBinding
import com.doctorsplaza.app.ui.doctor.fragment.clinics.model.ClinicData
import com.doctorsplaza.app.utils.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ClinicDetailsFragment : Fragment(R.layout.fragment_clinic_details), View.OnClickListener {
    private var clinicId: String = ""

    private lateinit var binding: FragmentClinicDetailsBinding

    private val clinicsViewModel: ClinicsViewModel by viewModels()

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_clinic_details, container, false)
            binding = FragmentClinicDetailsBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())
        clinicId = arguments?.getString("clinicId").toString()
        clinicsViewModel.getClinicsDetails(clinicId)
    }

    private fun setObserver() {
        clinicsViewModel.clinicsDetails.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.status == 200) {
                        setClinicsData(response.data.data)
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
    }

    private fun setClinicsData(data: ClinicData) {
        with(binding){
            clinicName.text = data.clinicName
            aboutClinic.text = ""
            clinicDetails.text = data.location
            contactDetails.text = data.clinicContactNumber.toString()
            visitingHoursDetails.text = "${data.start_time} to ${data.end_time}"
            clinicFloors.text = data.floorCount
            Glide.with(requireContext()).applyDefaultRequestOptions(clinicRequestOption()).load(data.image).into(clinicImage)
        }

    }

    private fun setOnClickListener() {
        with(binding) {
            backArrow.setOnClickListener(this@ClinicDetailsFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backArrow -> {
                findNavController().popBackStack()
            }
        }
    }
}
