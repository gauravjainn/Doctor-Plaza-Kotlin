package com.doctorsplaza.app.ui.patient.fragments.doctorDetails

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentDoctorDetailsBinding
import com.doctorsplaza.app.ui.patient.fragments.doctorDetails.model.DoctorDetailsData
import com.doctorsplaza.app.utils.DoctorPlazaLoader
import com.doctorsplaza.app.utils.Resource
import com.doctorsplaza.app.utils.showToast
import com.doctorsplaza.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DoctorDetailsFragment : Fragment(R.layout.fragment_doctor_details), View.OnClickListener {

    private var clinicAddress: String = ""
    private var clinicName: String = ""

    private var clinicContact: String = ""

    private var clinicId: String = ""

    private var doctorId: String = ""

    private lateinit var binding: FragmentDoctorDetailsBinding

    private val doctorDetailsViewModel: DoctorDetailsViewModel by viewModels()

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader

    private var appointmentType = "Offline"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_doctor_details, container, false)
            binding = FragmentDoctorDetailsBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())
        doctorId = arguments?.getString("doctorId").toString()
        clinicId = arguments?.getString("clinicId").toString()
        clinicName = arguments?.getString("clinicName").toString()
        clinicAddress = arguments?.getString("clinicAddress").toString()
        clinicContact = arguments?.getString("clinicContact").toString()
        doctorDetailsViewModel.getDoctor(doctorId)
    }


    private fun setObserver() {
        doctorDetailsViewModel.doctor.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.status == 200) {
                        binding.noData.isVisible = false
                        binding.loader.isVisible = false
                        setDoctorData(response.data.data[0])
                    } else {
                        binding.noData.isVisible = true
                        binding.loader.isVisible = true

                    }
                }
                is Resource.Loading -> {
                    binding.loader.isVisible = true
                    appLoader.show()
                }
                is Resource.Error -> {
                    binding.loader.isVisible = true
                    binding.noData.isVisible = true
                    appLoader.dismiss()
                    showToast(response.message.toString())
                }
            }
        }
    }

    private fun setDoctorData(data: DoctorDetailsData) {
        with(binding) {
            Glide.with(requireContext()).load(data.profile_picture).into(doctorImage)
            doctorName.text = data.doctorName
            doctorSpecialistIn.text = data.specialization
            doctorLocation.text = data.city
            doctorDegree.text = data.qualification
            verifiedIcon.isVisible = data.is_approved
            verifiedViewGrp.isVisible = data.is_approved
            about.text = data.about
        }
    }

    private fun setOnClickListener() {
        with(binding) {
            backArrow.setOnClickListener(this@DoctorDetailsFragment)
            bookAppointment.setOnClickListener(this@DoctorDetailsFragment)
            inClinicRadioBtn.setOnClickListener(this@DoctorDetailsFragment)
            onlineRadioBtn.setOnClickListener(this@DoctorDetailsFragment)
            clinicConsultView.setOnClickListener(this@DoctorDetailsFragment)
            videoConsultView.setOnClickListener(this@DoctorDetailsFragment)
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backArrow -> {
                findNavController().popBackStack()
            }
            R.id.bookAppointment -> {
                val bundle = Bundle()
                bundle.putString("doctorId", doctorId)
                bundle.putString("clinicId", clinicId)
                bundle.putString("clinicName", clinicName)
                bundle.putString("clinicContact", clinicContact)
                bundle.putString("clinicAddress", clinicAddress)
                bundle.putString("appointmentType", appointmentType)
                findNavController().navigate(R.id.bookAppointmentFragment, bundle)
            }

            R.id.inClinicRadioBtn -> {
                binding.inClinicRadioBtn.isChecked = true
                binding.onlineRadioBtn.isChecked = false
                appointmentType = "Offline"
            }

            R.id.onlineRadioBtn -> {
                binding.inClinicRadioBtn.isChecked = false
                binding.onlineRadioBtn.isChecked = true
                appointmentType = "Online"
            }

            R.id.clinicConsultView -> {
                binding.inClinicRadioBtn.isChecked = true
                binding.onlineRadioBtn.isChecked = false
                appointmentType = "Offline"
            }

            R.id.videoConsultView -> {
                binding.inClinicRadioBtn.isChecked = false
                binding.onlineRadioBtn.isChecked = true
                appointmentType = "Online"
            }
        }
    }
}
