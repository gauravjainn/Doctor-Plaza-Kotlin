package com.doctorsplaza.app.ui.patient.loginSignUp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentPatientSignUpBinding
import com.doctorsplaza.app.ui.patient.loginSignUp.PatientLoginSignUpViewModel
import com.doctorsplaza.app.utils.*
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class PatientSignUpFragment : Fragment(R.layout.fragment_patient_sign_up), View.OnClickListener {

    private lateinit var binding: FragmentPatientSignUpBinding

    @Inject
    lateinit var session: SessionManager

    private val patientLoginSignUpViewModel: PatientLoginSignUpViewModel by viewModels()

    private var currentView: View? = null

    private lateinit var appProgress: DoctorPlazaLoader


    private var userNameGrp = true

    private var dobEmailGrp = false

    private var userPhoneGrp = false

    private var patientGender = ""

    private var patientDOB = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_patient_sign_up, container, false)
            binding = FragmentPatientSignUpBinding.bind(currentView!!)
            init()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        appProgress = DoctorPlazaLoader(requireContext())
        val materialDateBuilder = MaterialDatePicker.Builder.datePicker()
        materialDateBuilder.setTitleText("SELECT YOU DATE OF BIRTH")
    }


    private fun setOnClickListener() {
        with(binding) {
            backArrow.setOnClickListener(this@PatientSignUpFragment)
            dobEdt.setOnClickListener(this@PatientSignUpFragment)
            saveAndNext.setOnClickListener(this@PatientSignUpFragment)
            haveAccountSignIn.setOnClickListener(this@PatientSignUpFragment)
        }

        binding.genderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                patientGender = p0?.getItemAtPosition(p2).toString()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.saveAndNext -> {
                setViews()

            }

            R.id.backArrow -> {
                if (dobEmailGrp && userNameGrp && userPhoneGrp) {
                    dobEmailGrp = false
                    userPhoneGrp = true
                    binding.userNameGenderGrp.isVisible = false
                    binding.userPhoneGrp.isVisible = false
                    binding.dobEmailGrp.isVisible = true
                    binding.alreadyHaveAccount.visibility = View.INVISIBLE
                } else if (!dobEmailGrp && userPhoneGrp) {
                    dobEmailGrp = false
                    userPhoneGrp = false
                    userNameGrp = false
                    binding.userNameGenderGrp.isVisible = true
                    binding.dobEmailGrp.isVisible = false
                    binding.userPhoneGrp.isVisible = false
                    binding.alreadyHaveAccount.visibility = View.VISIBLE

                } else {
                    findNavController().popBackStack()
                }
            }

            R.id.haveAccountSignIn -> {
                findNavController().navigate(R.id.action_patientSignUpFragment_to_patientLoginFragment)
            }

            R.id.dobEdt -> {
                val datePicker: MaterialDatePicker<Long> = MaterialDatePicker
                    .Builder
                    .datePicker()

                    .setTheme(R.style.MaterialCalendarTheme)
                    .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                    .setTitleText("Select date of birth")
                    .build()

                datePicker.show(childFragmentManager, "DATE_PICKER")

                datePicker.addOnPositiveButtonClickListener {
                    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val date = sdf.format(it)
                    val stringDate: Date = sdf.parse(date)
                    val sdf1 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",Locale.getDefault())
                    var consultationDate = sdf1.format(stringDate)

                    if(stringDate>Date()){
                        showToast("Date of Birth Cannot be Future Date.")
                    }else{
                        binding.dobEdt.text = date
                        patientDOB = consultationDate.toString()
                    }
                }
            }
        }
    }

    private fun setViews() {
        val patientName = binding.userNameEdt.text.toString()
        val patientEmail = binding.emailEdt.text.toString()
        val patientNumber = binding.phoneNumberEdt.text.toString()

        userNameGrp = true
        if (dobEmailGrp && userNameGrp && userPhoneGrp) {
            binding.alreadyHaveAccount.visibility = View.INVISIBLE
            if (patientNumber.isEmpty() || patientNumber.length < 10) {
                requireContext().showToast("Please Enter Valid Mobile Number")
            } else {
                checkEmailAndSignup()

            }

        } else if (dobEmailGrp && userNameGrp) {
            binding.alreadyHaveAccount.visibility = View.INVISIBLE
            when {
                patientDOB.isEmpty() -> {
                    requireContext().showToast("Please Select Your Date of Birth")
                }

                patientEmail.isEmpty() || !checkEmail(patientEmail) -> {
                    requireContext().showToast("Please Enter a Valid Email Id")
                }

                else -> {
                    userPhoneGrp = true
                    binding.userNameGenderGrp.isVisible = false
                    binding.dobEmailGrp.isVisible = false
                    binding.userPhoneGrp.isVisible = true
                }
            }

        } else if (userNameGrp) {
            binding.alreadyHaveAccount.visibility = View.INVISIBLE
            when {
                patientName.isEmpty() -> {
                    requireContext().showToast("Please Enter Your Name")
                }

                patientGender == "Select Gender" -> {
                    requireContext().showToast("Please Select Your Gender")
                }

                else -> {
                    dobEmailGrp = true
                    binding.userNameGenderGrp.isVisible = false
                    binding.userPhoneGrp.isVisible = false
                    binding.dobEmailGrp.isVisible = true
                }
            }
        }
    }


    private fun checkEmailAndSignup() {
        val email = binding.emailEdt.toString()
        val jsonObject = JsonObject()
        jsonObject.addProperty("type", "patient")
        jsonObject.addProperty("email", email)
        patientLoginSignUpViewModel.checkEmail(jsonObject)

        patientLoginSignUpViewModel.checkEmail.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data!!.success) {
                        callSignUpApi()
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

    private fun callSignUpApi() {

        val patientName = binding.userNameEdt.text.toString()
        val patientEmail = binding.emailEdt.text.toString()
        val patientNumber = binding.phoneNumberEdt.text.toString()

        val jsonObject = JsonObject()
        jsonObject.addProperty(PatientName, patientName)
        jsonObject.addProperty(PatientEmail, patientEmail)
        jsonObject.addProperty(PatientBloodGroup, "")
        jsonObject.addProperty(PatientPassword, "doctor@123")
        jsonObject.addProperty(PatientGender, patientGender)
        jsonObject.addProperty(PatientAddress, "")
        jsonObject.addProperty(PatientAge, "")
        jsonObject.addProperty(PatientDOB, patientDOB)
        jsonObject.addProperty(PatientContactNumber, patientNumber)
        jsonObject.addProperty(PatientToken, "req.body.token")

        patientLoginSignUpViewModel.patientRegister(jsonObject)
        patientLoginSignUpViewModel.patientReg.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appProgress.dismiss()
                    if (response.data!!.success) {
                        requireContext().showToast(response.data.message)
                        val bundle = Bundle()
                        bundle.putString("patientId", response.data.data.patient_id)
                        bundle.putString("type", "patient")
                        bundle.putString("phone", response.data.data.contact_number.toString())
                        bundle.putString("from", "signUp")
                        findNavController().navigate(
                            R.id.action_patientSignUpFragment_to_OTPFragment,
                            bundle
                        )
                    } else {
                        requireContext().showToast(response.data.message)
                    }
                }
                is Resource.Loading -> {

                }
                is Resource.Error -> {
                    appProgress.dismiss()
                    requireContext().showToast(response.message.toString())
                }
            }
        }

    }
}