package com.doctorsplaza.app.ui.doctor.loginSignUp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentDoctorLoginBinding
import com.doctorsplaza.app.ui.doctor.loginSignUp.DoctorLoginSignUpViewModel
import com.doctorsplaza.app.utils.DoctorPlazaLoader
import com.doctorsplaza.app.utils.Resource
import com.doctorsplaza.app.utils.SessionManager
import com.doctorsplaza.app.utils.showToast
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DoctorLoginFragment : Fragment(R.layout.fragment_doctor_login) , View.OnClickListener {

    private lateinit var binding: FragmentDoctorLoginBinding

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader

    private val doctorLoginSViewModel:DoctorLoginSignUpViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_doctor_login, container, false)
            binding = FragmentDoctorLoginBinding.bind(currentView!!)
            init()
            setObservers()
            setOnClickListener()
        }
        return currentView!!
    }



    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())
    }

    private fun setObservers() {
        doctorLoginSViewModel.login.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success->{
                    appLoader.dismiss()
                    if(response.data?.success!!){

                        val bundle = Bundle().apply {
                            putString("doctorId",response.data.data.doctor_id)
                            putString("type","doctor")
                            putString("phone",response.data.data.contactNumber.toString())
                            putString("from","login")
                        }
                            findNavController().navigate(R.id.doctorOTPFragment,bundle)
                    }else{
                        showToast(response.data.message)
                    }
                }
                is Resource.Loading->{
                    appLoader.show()
                }
                is Resource.Error->{
                    appLoader.dismiss()
                }
            }
        }
    }

    private fun setOnClickListener() {
        binding.backArrow.setOnClickListener(this@DoctorLoginFragment)
        binding.loginSubmit.setOnClickListener(this@DoctorLoginFragment)
        binding.haveAccountSignIn.setOnClickListener(this@DoctorLoginFragment)

    }

    private fun verifyAndLogin() {
        val phoneNumber = binding.phoneEdt.text.toString().trim()

        if(phoneNumber.length>10||phoneNumber.length<10){
            showToast("Please enter a valid phone number..")
        }else{
            val jsonObject = JsonObject()
            jsonObject.addProperty("mobilenumber", phoneNumber)
            jsonObject.addProperty("token", "req.body.token")
            doctorLoginSViewModel.setLogin(jsonObject = jsonObject)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backArrow -> {
                findNavController().popBackStack()
            }

            R.id.loginSubmit -> {
                verifyAndLogin()
            }

            R.id.haveAccountSignIn -> {
                findNavController().popBackStack()
            }
        }
    }
}