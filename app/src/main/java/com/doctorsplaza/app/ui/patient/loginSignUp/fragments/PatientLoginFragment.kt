package com.doctorsplaza.app.ui.patient.loginSignUp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentPatientLoginBinding
import com.doctorsplaza.app.ui.patient.loginSignUp.PatientLoginSignUpViewModel
import com.doctorsplaza.app.utils.DoctorPlazaLoader
import com.doctorsplaza.app.utils.PatientToken
import com.doctorsplaza.app.utils.Resource
import com.doctorsplaza.app.utils.showToast
import com.google.gson.JsonObject
import com.doctorsplaza.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PatientLoginFragment : Fragment(R.layout.fragment_patient_login), View.OnClickListener {

    private lateinit var binding: FragmentPatientLoginBinding

    @Inject
    lateinit var session: SessionManager

    private val patientLoginSignUpViewModel: PatientLoginSignUpViewModel by viewModels()

    private var currentView: View? = null

    private lateinit var appProgress: DoctorPlazaLoader

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_patient_login, container, false)
            binding = FragmentPatientLoginBinding.bind(currentView!!)
            init()
            setObserver()
            setRecyclerView()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        appProgress = DoctorPlazaLoader(requireContext())
    }


    private fun setObserver() {
    }

    private fun setOnClickListener() {
        with(binding) {
            backArrow.setOnClickListener(this@PatientLoginFragment)
            loginSubmit.setOnClickListener(this@PatientLoginFragment)
            notHaveAccountSignUp.setOnClickListener(this@PatientLoginFragment)

        }

    }
    private fun setRecyclerView() {}

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backArrow -> { findNavController().popBackStack() }
            R.id.loginSubmit -> { callLogin() }
            R.id.notHaveAccountSignUp -> {
                findNavController().navigate(R.id.action_patientLoginFragment_to_patientSignUpFragment)
            }

        }
    }

    private fun callLogin() {
        val phoneNumber = binding.phoneNumberEdt.text.toString()
        if(phoneNumber.isEmpty()||phoneNumber.length<10){
            requireContext().showToast("Please Enter a Valid Mobile Number")
            return
        }
        val jsonObject = JsonObject()
        jsonObject.addProperty("mobilenumber",phoneNumber)
        jsonObject.addProperty(PatientToken,"req.body.token")

        patientLoginSignUpViewModel.setLogin(jsonObject)

        patientLoginSignUpViewModel.login.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success->{
                    appProgress.dismiss()
                    if(response.data!!.success){
                        requireContext().showToast(response.data.message)
                        val bundle = Bundle()
                        bundle.putString("patientId", response.data.data.patient_id)
                        bundle.putString("type", "patient")
                        bundle.putString("phone", response.data.data.contact_number.toString())
                        bundle.putString("from", "login")
                        findNavController().navigate(R.id.OTPFragment,bundle)
                    }else{
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
}