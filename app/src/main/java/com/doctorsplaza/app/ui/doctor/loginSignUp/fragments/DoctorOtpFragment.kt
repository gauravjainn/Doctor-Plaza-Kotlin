package com.doctorsplaza.app.ui.doctor.loginSignUp.fragments

import `in`.aabhasjindal.otptextview.OTPListener
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.util.DisplayMetrics
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentOTPBinding
import com.doctorsplaza.app.ui.doctor.DoctorMainActivity
import com.doctorsplaza.app.ui.doctor.loginSignUp.DoctorLoginSignUpViewModel
import com.doctorsplaza.app.ui.patient.loginSignUp.PatientLoginSignup
import com.doctorsplaza.app.utils.DoctorPlazaLoader
import com.doctorsplaza.app.utils.Resource
import com.doctorsplaza.app.utils.SessionManager
import com.doctorsplaza.app.utils.showToast
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class DoctorOtpFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentOTPBinding

    @Inject
    lateinit var session: SessionManager

    private val doctorLoginSignUpViewModel: DoctorLoginSignUpViewModel by viewModels()

    private var currentView: View? = null

    private lateinit var appProgress: DoctorPlazaLoader

    private var doctorId = ""

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
        doctorLoginSignUpViewModel.resendSignUpOtp.observe(viewLifecycleOwner) { response ->
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
                                            ))

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

        doctorLoginSignUpViewModel.resendLoginOtp.observe(viewLifecycleOwner) { response ->
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
                                )

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
        doctorId = arguments?.getString("doctorId").toString()
        type = arguments?.getString("type").toString()
        phoneNumber = arguments?.getString("phone").toString()
        from = arguments?.getString("from").toString()
        binding.otpSentData.text = "We sent verification code to $phoneNumber"
    }


    private fun setOnClickListener() {

        binding.backArrow.setOnClickListener(this@DoctorOtpFragment)
        binding.verifyOTP.setOnClickListener(this@DoctorOtpFragment)
        binding.resendOtp.setOnClickListener(this@DoctorOtpFragment)
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
             requireActivity().onBackPressed()
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
            addProperty("token", "")
        }

        if (from == "signUp") {
            doctorLoginSignUpViewModel.resendSignUpOtp(jsonObject)
        } else if (from == "login") {
            doctorLoginSignUpViewModel.resendLoginOtp(jsonObject)
        }

    }

    private fun verifyOTP() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("doctorid", doctorId)
        jsonObject.addProperty("phone", phoneNumber)
        jsonObject.addProperty("type", type)
        jsonObject.addProperty("otp", enteredOtp)
        if (from == "signUp") {
            doctorLoginSignUpViewModel.verifyOTP(jsonObject)
        } else if (from == "login") {
            doctorLoginSignUpViewModel.verifyLoginOTP(jsonObject)
        }
        doctorLoginSignUpViewModel.verifyOTP.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appProgress.dismiss()
                    if (response.data!!.success) {
                        if (from == "signUp") {
                            showSuccessDialog()
                        } else {
                            session.isLogin = true
                            session.loginType = "doctor"
                            if (from == "login") {
                                session.loginId = response.data.data._id
                                session.doctorId = response.data.data.doctor_id
                                session.loginName = response.data.data.doctorName
                                session.mobile = response.data.data.contactNumber.toString()
                                session.token = response.data.data.auth_token
                            }
                            startActivity(Intent(requireActivity(), DoctorMainActivity::class.java))
                            PatientLoginSignup().finish()
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
    }

    private fun showSuccessDialog() {
        val successDialog = Dialog(requireActivity())
        successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        successDialog.setCancelable(false)


        successDialog.setContentView(R.layout.dialog_doctor_registered_successfully)
        successDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        successDialog.findViewById<View>(R.id.doneBtn).setOnClickListener {
            successDialog.dismiss()
            requireActivity().finish()
        }


        successDialog.show()
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val displayWidth = displayMetrics.widthPixels
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(successDialog.window!!.attributes)
        val dialogWindowWidth = (displayWidth * 0.9f).toInt()

        layoutParams.width = dialogWindowWidth
        successDialog.window!!.attributes = layoutParams
    }
}