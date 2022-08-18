package com.doctorsplaza.app.ui.doctor.fragment.profile

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
import com.doctorsplaza.app.databinding.FragmentDoctorProfileBinding
import com.doctorsplaza.app.ui.doctor.fragment.profile.model.DoctorData
import com.doctorsplaza.app.utils.*
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DoctorProfileFragment : Fragment(R.layout.fragment_doctor_profile), View.OnClickListener {
    private lateinit var binding: FragmentDoctorProfileBinding

    private val doctorProfileViewModel: DoctorProfileViewModel by viewModels()

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
            currentView = inflater.inflate(R.layout.fragment_doctor_profile, container, false)
            binding = FragmentDoctorProfileBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())
        doctorProfileViewModel.getDoctorProfile()
        binding.loader.isVisible = true
    }

    private fun setObserver() {

        doctorProfileUpdated.observe(requireActivity()) {
            if (it) {
                doctorProfileViewModel.getDoctorProfile()
            }
        }

        doctorProfileViewModel.doctorProfile.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    binding.loader.isVisible = false
                    when (response.data?.status) {
                        401 -> {
                            session.isLogin = false
                            logOutUnAuthorized(requireActivity(),response.data.message)
                        }
                        200 -> {
                            setDoctorProfile(response.data.data[0])
                        }
                        else -> {
                            showToast("something went wrong")
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

        doctorProfileViewModel.doctorTurnDayOn.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.success!!) {
                        showToast(response.data.message)
                    } else {
                        showToast(response.data.message)
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

        doctorProfileViewModel.doctorTurnDayOff.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.success!!) {
                        showToast(response.data.message)
                    } else {
                        showToast(response.data.message)
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

        doctorProfileUpdated.observe(requireActivity()) {
            if (it) {
                doctorProfileViewModel.getDoctorProfile()
            }
        }

    }

    private fun setDoctorProfile(data: DoctorData) {
        with(binding) {
            session.loginImage = data.profile_picture
            Glide.with(requireContext()).applyDefaultRequestOptions(doctorRequestOption())
                .load(data.profile_picture).into(userImage)
            name.text = data.doctorName
            consultationFees.text = data.consultationfee
            department.text = data.specialization
            gender.text = data.gender
            email.text = data.email
            phone.text = data.contactNumber.toString()
            address.text = data.address
            experienceEdt.text = data.experience
            dayOffSwitch.isChecked = data.turndayoff
        }
    }


    private fun setOnClickListener() {
        with(binding) {
            profileEdit.setOnClickListener(this@DoctorProfileFragment)
            dayOffSwitch.setOnClickListener {
                val jsonObject = JsonObject()
                jsonObject.addProperty("id", session.loginId)
                if (dayOffSwitch.isChecked) {
                    doctorProfileViewModel.doctorTurnDayOff(jsonObject)
                } else {
                    doctorProfileViewModel.doctorTurnDayOn(jsonObject)
                }
            }
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.profileEdit -> {
                findNavController().navigate(R.id.editDoctorProfileFragment)
            }
        }
    }

    private fun setProfileImage() {
        Glide.with(requireContext()).applyDefaultRequestOptions(doctorRequestOption())
            .load(session.loginImage).into(binding.userImage)
    }

    override fun onResume() {
        super.onResume()
        setProfileImage()
    }


}
