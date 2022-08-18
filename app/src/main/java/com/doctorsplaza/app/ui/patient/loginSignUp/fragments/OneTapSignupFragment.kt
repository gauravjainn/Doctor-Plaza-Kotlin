package com.doctorsplaza.app.ui.patient.loginSignUp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentOTPBinding
import com.doctorsplaza.app.databinding.FragmentOneTapSignupBinding
import com.doctorsplaza.app.ui.patient.loginSignUp.PatientLoginSignUpViewModel
import com.doctorsplaza.app.utils.DoctorPlazaLoader
import com.doctorsplaza.app.utils.Resource
import com.doctorsplaza.app.utils.showToast
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OneTapSignupFragment : Fragment(), View.OnClickListener {

    lateinit var binding: FragmentOneTapSignupBinding

    private var currentView: View? = null

    private lateinit var appProgress: DoctorPlazaLoader

    private val patientLoginSignUpViewModel: PatientLoginSignUpViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_one_tap_signup, container, false)
            binding = FragmentOneTapSignupBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        appProgress = DoctorPlazaLoader(requireContext())
    }

    private fun setObserver() {
        patientLoginSignUpViewModel.oneTapSignUpPatientOtp.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success->{
                    appProgress.dismiss()
                    if(response.data!!.success){
                        requireContext().showToast(response.data.message)
                        val bundle = Bundle()
                        bundle.putString("patientId", response.data.data.patient_id)
                        bundle.putString("type", "patient")
                        bundle.putString("phone", response.data.data.contact_number.toString())
                        bundle.putString("from", "signUp")
                        findNavController().navigate(
                            R.id.action_oneTapSignupFragment_to_OTPFragment,
                            bundle
                        )
                    } else {
                        requireContext().showToast(response.data.message)
                    }
                }
                is Resource.Loading->{
                    appProgress.show()
                }
                is Resource.Error->{
                    appProgress.dismiss()
                }
            }
        }
    }

    private fun setOnClickListener() {
        binding.backArrow.setOnClickListener(this@OneTapSignupFragment)
        binding.signUpSubmit.setOnClickListener(this@OneTapSignupFragment)
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.backArrow -> {
                findNavController().popBackStack()
            }

            R.id.signUpSubmit -> {
                oneTapSignUp()
            }
        }
    }

    private fun oneTapSignUp() {
        val phoneNo = binding.phoneNumberEdt.text.toString()
        if(phoneNo.isEmpty()||phoneNo.length>10||phoneNo.length<10){
            showToast("please enter a valid phone number")
            return
        }
        val json = JsonObject()
        json.addProperty("contactNumber", phoneNo)
        json.addProperty("device_type", "ANDROID")
        patientLoginSignUpViewModel.oneTapSignUpOtp(json)
    }
}