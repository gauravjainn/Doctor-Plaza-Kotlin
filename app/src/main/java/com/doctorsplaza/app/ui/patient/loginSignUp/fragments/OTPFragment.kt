package com.doctorsplaza.app.ui.patient.loginSignUp.fragments

import `in`.aabhasjindal.otptextview.OTPListener
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentOTPBinding
import com.doctorsplaza.app.ui.patient.PatientMainActivity
import com.doctorsplaza.app.ui.patient.loginSignUp.PatientLoginSignUpViewModel
import com.doctorsplaza.app.utils.DoctorPlazaLoader
import com.doctorsplaza.app.utils.Resource
import com.doctorsplaza.app.utils.showToast
import com.google.gson.JsonObject
import com.doctorsplaza.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class OTPFragment : Fragment(R.layout.fragment_o_t_p), View.OnClickListener {

    private lateinit var binding: FragmentOTPBinding

    @Inject
    lateinit var session: SessionManager

    private val patientLoginSignUpViewModel: PatientLoginSignUpViewModel by viewModels()

    private var currentView: View? = null

    private lateinit var appProgress: DoctorPlazaLoader

    private var patientId = ""

    private var type = ""

    private var from = ""

    private var phoneNumber = ""

    private var enteredOtp = ""

    private var enableResend = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_o_t_p, container, false)
            binding = FragmentOTPBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun setObserver() {
        patientLoginSignUpViewModel.verifyOTP.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appProgress.dismiss()
                    if (response.data!!.success) {
                        startActivity(Intent(requireActivity(), PatientMainActivity::class.java))
                        requireActivity().finish()
                        session.isLogin = true
                        session.apply {
                            patientId = response.data.data._id
                            loginName = response.data.data.patient_name
                            loginEmail = response.data.data.email
                            loginPhone = response.data.data.contact_number.toString()
                            loginAddress = response.data.data.address
                            loginGender = response.data.data.gender
                            loginDOB = response.data.data.dob.toString()
                            loginAge = response.data.data.age.toString()
                            loginType = "patient"
                            token = response.data.data.auth_token
                        }

                    } else {
                        requireContext().showToast(response.data.message)
                    }
                }

                is Resource.Loading -> {
                    appProgress.show()
                }

                is Resource.Error -> {
                    appProgress.dismiss()
                    requireContext().showToast(response.message.toString())
                }
            }
        }

        patientLoginSignUpViewModel.resendLoginSignUpOtp.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appProgress.dismiss()
                    if (response.data!!.success) {
                        showToast("OTP Sent Successfully")
                        val timer = object : CountDownTimer(30000, 1000) {
                            @SuppressLint("SetTextI18n")
                            override fun onTick(millisUntilFinished: Long) {
                                enableResend = false
                                binding.resendOtp.text = "" + String.format(
                                    "%d:%d",
                                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                            TimeUnit.MINUTES.toSeconds(
                                                TimeUnit.MILLISECONDS.toMinutes(
                                                    millisUntilFinished
                                                )
                                            )
                                );

                            }

                            override fun onFinish() {
                                enableResend = true
                                binding.resendOtp.text = "Resend"
                            }
                        }
                        timer.start()
                    } else {
                        requireContext().showToast(response.data.message)
                    }
                }

                is Resource.Loading -> {
                    appProgress.show()
                }

                is Resource.Error -> {
                    appProgress.dismiss()
                    requireContext().showToast(response.message.toString())
                }
            }
        }

    }


    private fun init() {
        appProgress = DoctorPlazaLoader(requireContext())
        patientId = arguments?.getString("patientId").toString()
        type = arguments?.getString("type").toString()
        phoneNumber = arguments?.getString("phone").toString()
        from = arguments?.getString("from").toString()
        binding.otpSentData.text = "We sent verification code to $phoneNumber"
    }


    private fun setOnClickListener() {

        binding.backArrow.setOnClickListener(this@OTPFragment)
        binding.resendOtp.setOnClickListener(this@OTPFragment)
        binding.verifyOTP.setOnClickListener(this@OTPFragment)
        binding.otpView.otpListener = object : OTPListener {
            override fun onInteractionListener() {
            }

            override fun onOTPComplete(otp: String) {
                enteredOtp = otp
                verifyOTP()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backArrow -> {
                findNavController().popBackStack()
            }
            R.id.verifyOTP -> {
                verifyOTP()
            }

            R.id.resendOtp -> {
                if (enableResend) {
                    resendOtp()
                }
            }
        }
    }

    private fun resendOtp() {
        val jsonObject = JsonObject().apply {
            addProperty("mobilenumber", arguments?.getString("phone").toString())
            addProperty("token", "abcdef")
        }
        patientLoginSignUpViewModel.resendLoginSignUpOtp(jsonObject)
    }

    private fun verifyOTP() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("patientid", patientId)
        jsonObject.addProperty("phone", phoneNumber)
        jsonObject.addProperty("type", type)
        jsonObject.addProperty("otp", enteredOtp)
        if (from == "signUp") {
            patientLoginSignUpViewModel.verifyOTP(jsonObject)
        } else if (from == "login") {
            patientLoginSignUpViewModel.verifyLoginOTP(jsonObject)
        }
    }
}