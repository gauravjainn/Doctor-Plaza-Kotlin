package com.doctorsplaza.app.ui.doctor.loginSignUp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentDoctorSignUpBinding
import com.doctorsplaza.app.ui.doctor.loginSignUp.DoctorLoginSignUpViewModel
import com.doctorsplaza.app.utils.*
import com.google.gson.JsonObject
import com.gym.gymapp.utils.SingleLiveEvent
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class DoctorSignUpFragment : Fragment(R.layout.fragment_doctor_sign_up), View.OnClickListener {

    private var qualification: String = ""
    private var doctorName: String = ""
    private var selectedGender: String = ""
    private var selectedSpecialization: String = ""
    private var consultationFees: String = ""
    private var doctorExperience: String = ""
    private var address: String = ""
    private var email: String = ""
    private var phone: String = ""
    private var selectedState: String = ""
    private var selectedCity: String = ""
    private var pincode: String = ""

    private lateinit var binding: FragmentDoctorSignUpBinding

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader

    private val pageCount = SingleLiveEvent<Int>()

    private val doctorLoginSignUpViewModel: DoctorLoginSignUpViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_doctor_sign_up, container, false)
            binding = FragmentDoctorSignUpBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())
        pageCount.value = 1
        setGenderSpinner()
        doctorLoginSignUpViewModel.getSpecialization()
        doctorLoginSignUpViewModel.getState()
    }


    private fun setObserver() {
        pageCount.observe(viewLifecycleOwner) {
            when (it) {
                1 -> {
                    binding.userNameGenderGrp.isVisible = true
                    binding.specializationConsultationGrp.isVisible = false
                    binding.addressGrp.isVisible = false
                    binding.phoneEmailGrp.isVisible = false
                    binding.alreadyHaveAccount.isVisible = true
                    binding.loginSubmit.text = "Save & Next"
                }
                2 -> {
                    binding.userNameGenderGrp.isVisible = false
                    binding.specializationConsultationGrp.isVisible = true
                    binding.addressGrp.isVisible = false
                    binding.phoneEmailGrp.isVisible = false
                    binding.alreadyHaveAccount.visibility = View.INVISIBLE
                    binding.loginSubmit.text = "Save & Next"

                }
                3 -> {
                    binding.userNameGenderGrp.isVisible = false
                    binding.specializationConsultationGrp.isVisible = false
                    binding.addressGrp.isVisible = true
                    binding.phoneEmailGrp.isVisible = false
                    binding.alreadyHaveAccount.visibility = View.INVISIBLE
                    binding.loginSubmit.text = "Save & Next"
                }
                4 -> {
                    binding.userNameGenderGrp.isVisible = false
                    binding.specializationConsultationGrp.isVisible = false
                    binding.addressGrp.isVisible = false
                    binding.phoneEmailGrp.isVisible = true
                    binding.alreadyHaveAccount.visibility = View.INVISIBLE
                    binding.loginSubmit.text = "Submit"
                }
                5 -> {
                    val email = binding.emailEdt.text.toString().trim()
                    val jsonObject = JsonObject()
                    jsonObject.addProperty("email", email)
                    jsonObject.addProperty("type", "doctor")
                    doctorLoginSignUpViewModel.checkEmail(jsonObject = jsonObject)
                }
            }
        }

        doctorLoginSignUpViewModel.checkEmail.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.success!!) {
                        sendOtp()
                    } else {
                        pageCount.postValue(4)
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

        doctorLoginSignUpViewModel.specializations.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.success == true) {
                        setSpecialistSpinner(response.data.data)
                    } else {
                        showToast(response.data?.message.toString())
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


        doctorLoginSignUpViewModel.state.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.success == true) {
                        setState(response.data.data)
                    } else {
                        showToast(response.data?.message.toString())
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

        doctorLoginSignUpViewModel.city.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data!!.success) {
                        setCity(response.data.data)
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

        doctorLoginSignUpViewModel.doctorReg.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.success!!) {
                        val bundle = Bundle().apply {
                            putString("doctorId", response.data.data.doctor_id)
                            putString("type", "doctor")
                            putString("phone", response.data.data.contactNumber.toString())
                            putString("from", "signUp")
                        }
                        findNavController().navigate(
                            R.id.action_doctorSignUpFragment_to_doctorOTPFragment,
                            bundle
                        )
                    } else {
                        showToast(response.data.message)
                    }
                }
                is Resource.Loading -> {
                    appLoader.show()
                }

                is Resource.Error -> {
                    appLoader.dismiss()
                    showToast(response.data?.message.toString())
                }

            }
        }

    }

    private fun sendOtp() {
        val jsonObject = JsonObject()
        jsonObject.addProperty(DoctorName, doctorName)
        jsonObject.addProperty(PatientEmail, email)
        jsonObject.addProperty(PatientPassword, "doctor@123")
        jsonObject.addProperty(DepartmentName, selectedSpecialization)
        jsonObject.addProperty(PatientGender, selectedGender)
        jsonObject.addProperty(PatientAddress, address)
        jsonObject.addProperty(DoctorNo, phone)
        jsonObject.addProperty("qualification", qualification)
        jsonObject.addProperty(DoctorState, selectedState)
        jsonObject.addProperty(DoctorCity, selectedCity)
        jsonObject.addProperty(DoctorPincode, pincode)
        jsonObject.addProperty(DoctorFees, consultationFees)
        jsonObject.addProperty(DoctorExperience, doctorExperience)
        jsonObject.addProperty(PatientToken, "req.body.token")
        doctorLoginSignUpViewModel.doctorRegister(jsonObject)
    }


    private fun setSpecialistSpinner(data: List<String>) {

        val specialistAdapter = ArrayAdapter(requireContext(), R.layout.spinner_text, data)
        specialistAdapter.setDropDownViewResource(R.layout.spinner_text)
        binding.specializationSpinner.adapter = specialistAdapter
        binding.specializationSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    selectedSpecialization = binding.specializationSpinner.selectedItem.toString()
                }
            }
    }

    private fun setState(data: List<String>) {
        val stateAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_text,
            data
        )

        stateAdapter.setDropDownViewResource(R.layout.spinner_text)
        binding.stateSpinner.adapter = stateAdapter
        binding.stateSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    selectedState = binding.stateSpinner.selectedItem.toString().trim()
                    val jsonObject = JsonObject()
                    jsonObject.addProperty("state", selectedState)
                    doctorLoginSignUpViewModel.getCities(jsonObject)
                }
            }
    }

    private fun setCity(data: List<String>) {

        val stateAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_text,
            data
        )

        stateAdapter.setDropDownViewResource(R.layout.spinner_text)
        binding.citySpinner.adapter = stateAdapter
        binding.citySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    selectedCity = binding.citySpinner.selectedItem.toString().trim()
                }
            }
    }

    private fun setGenderSpinner() {

        val genderAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.gender_menu,
            R.layout.spinner_text
        )
        genderAdapter.setDropDownViewResource(R.layout.spinner_text)
        binding.genderSpinner.adapter = genderAdapter
        binding.genderSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    selectedGender = binding.genderSpinner.selectedItem.toString()
                }
            }
    }


    private fun setOnClickListener() {

        binding.backArrow.setOnClickListener(this@DoctorSignUpFragment)
        binding.haveAccountSignIn.setOnClickListener(this@DoctorSignUpFragment)
        binding.loginSubmit.setOnClickListener(this@DoctorSignUpFragment)

    }

    private fun setViewsAndSignUp() {
        when (pageCount.value) {
            /** UserName And Gender **/

            1 -> {
                doctorName = binding.userNameEdt.text.toString()

                when {
                    doctorName.isEmpty() -> {
                        showToast("please enter a valid user name")
                    }
                    selectedGender == "Select Gender" -> {
                        showToast("please select gender")
                    }
                    else -> {
                        pageCount.postValue(2)
                    }
                }
            }

            /** Specialization And Consultation fee **/

            2 -> {
                qualification = binding.qualificationEdt.text.toString()
                consultationFees = binding.consultationFeeEdt.text.toString()
                doctorExperience = binding.experienceEdt.text.toString()
                when {
                    selectedSpecialization.isEmpty() -> {
                        showToast("please select Specialization")
                    }

                    qualification.isEmpty() -> {
                        showToast("please enter your Qualification")
                    }

                    doctorExperience.isEmpty() -> {
                        showToast("please enter your experience")
                    }

                    consultationFees.isEmpty() -> {
                        showToast("please enter your consultation fee")
                    }
                    else -> {
                        pageCount.postValue(3)
                    }
                }
            }

            /** Address State City PinCode **/

            3 -> {
                address = binding.addressEdt.text.toString()
                pincode = binding.pinCodeEdt.text.toString()

                when {
                    address.isEmpty() -> {
                        showToast("please enter your address")
                    }
                    selectedState.isEmpty() -> {
                        showToast("please select your State")
                    }
                    selectedCity.isEmpty() -> {
                        showToast("please select your City")
                    }
                    pincode.isEmpty() || pincode.length < 6 -> {
                        showToast("please enter a valid Pin Code")
                    }
                    else -> {
                        pageCount.postValue(4)
                    }
                }
            }

            /** Email and Phone **/

            4 -> {
                email = binding.emailEdt.text.toString()
                phone = binding.phoneEdt.text.toString()

                when {
                    !email.isValidEmail() -> {
                        showToast("please enter a valid email")
                    }

                    phone.isEmpty() && phone.length<10 -> {
                        showToast("please enter a valid phone number")
                    }
                    else -> {
                        pageCount.postValue(5)
                    }
                }

            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backArrow -> {
                if (pageCount.value!! <= 1) {
                    requireActivity().onBackPressed()
                }
                pageCount.postValue(pageCount.value?.minus(1) ?: 0)
            }

            R.id.loginSubmit -> {
                setViewsAndSignUp()
            }
            R.id.haveAccountSignIn -> {
                findNavController().navigate(R.id.doctorLoginFragment)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setObserver()
    }
}