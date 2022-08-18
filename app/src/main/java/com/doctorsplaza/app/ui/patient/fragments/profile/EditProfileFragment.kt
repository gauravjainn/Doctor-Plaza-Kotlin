package com.doctorsplaza.app.ui.patient.fragments.profile

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentEditProfileBinding
import com.doctorsplaza.app.ui.patient.fragments.clinicDoctors.adapter.ClinicDoctorsAdapter
import com.doctorsplaza.app.ui.patient.fragments.clinicDoctors.model.DoctorData
import com.doctorsplaza.app.utils.*
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.JsonObject
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class EditProfileFragment : Fragment(R.layout.fragment_edit_profile), View.OnClickListener {

    private var dob: String = ""


    private lateinit var binding: FragmentEditProfileBinding

    @Inject
    lateinit var session: SessionManager

    private val profileViewModel: ProfileViewModel by viewModels()

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader

    @Inject
    lateinit var clinicDoctorsAdapter: ClinicDoctorsAdapter

    private var doctorData: MutableList<DoctorData> = ArrayList()

    private var patientDOB = ""

    private var patientGender = ""

    private var from = ""

    val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_edit_profile, container, false)
            binding = FragmentEditProfileBinding.bind(currentView!!)
            init()
            setObserver()
            setUserData()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())
        from = arguments?.getString("from").toString()
        dob = arguments?.getString("dob") ?: ""
        if (!dob.isNullOrEmpty()) {
            patientDOB = isoFormat.parse(dob).toString()
        }
        Glide.with(requireContext()).applyDefaultRequestOptions(patientRequestOption())
            .load(session.loginImage).into(binding.userImage)
    }


    private fun setObserver() {
        profileViewModel.updateProfile.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.status == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(), response.data.message)
                    } else {
                        if (response.data!!.status == 200) {
                            requireContext().showToast(response.data.message)
                            if (from == "profile") {
                                findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
                            } else {
                                findNavController().popBackStack()
                            }
                            profileDetailsUpdated.postValue(response.data.data)

                            session.loginName = binding.name.text.toString()
                            session.loginDOB = binding.dateOfBirth.text.toString()
                            session.loginGender = patientGender
                            session.loginEmail = binding.email.text.toString()
                            session.loginAddress = binding.address.text.toString()

                        } else {
                            requireContext().showToast(response.data.message)
                        }
                    }
                }
                is Resource.Loading -> {
                    appLoader.show()
                }
                is Resource.Error -> {
                    requireContext().showToast(response.message.toString())
                    appLoader.dismiss()
                }
            }
        }

        profileViewModel.uploadPatientImage.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.status == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(), response.data.message)
                    } else
                        if (response.data?.code == 200) {
                            session.loginImage = response.data.profile_picture

                            profileImageUpdated.postValue(response.data.profile_picture)
                        } else {
                            binding.userImage.setImageResource(R.drawable.ic_user_image)
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
    }

    private fun setUserData() {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.gender_menu,
            R.layout.spinner_text
        )
        adapter.setDropDownViewResource(R.layout.spinner_text)
        binding.genderSpinner.adapter = adapter

        val dobParse = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val dobFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        var userDob = ""

        if (session.loginDOB.isNotEmpty() && session.loginDOB != "null") {
            val stringDate: Date = dobParse.parse(session.loginDOB)
            userDob = dobFormat.format(stringDate)
        }


        with(binding) {
            name.setText(session.loginName)
            dateOfBirth.text = userDob
            if (session.loginGender?.lowercase() == "male") {
                genderSpinner.setSelection(1)
            } else if (session.loginGender?.lowercase() == "female") {
                genderSpinner.setSelection(2)
            } else {
                genderSpinner.setSelection(0)
            }

            email.setText(session.loginEmail)
            email.isEnabled = email.text.isEmpty()
            phone.text = session.loginPhone
            address.setText(session.loginAddress)
        }
    }

    private fun setOnClickListener() {
        with(binding) {
            dateOfBirth.setOnClickListener(this@EditProfileFragment)
            backArrow.setOnClickListener(this@EditProfileFragment)
            saveBtn.setOnClickListener(this@EditProfileFragment)
            changeProfileImage.setOnClickListener(this@EditProfileFragment)
            genderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    patientGender = p0?.getItemAtPosition(p2).toString()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }

            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backArrow -> {
                findNavController().popBackStack()
            }
            R.id.dateOfBirth -> {
                selectDob()
            }

            R.id.saveBtn -> {
                updateProfile()
            }
            R.id.changeProfileImage -> {
                showPickerDialog()

            }
        }
    }

    private fun showPickerDialog() {
        val pickerDialog = Dialog(requireActivity())
        pickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        pickerDialog.setCancelable(true)
        pickerDialog.setContentView(R.layout.dialogue_image_pdf_picker)
        pickerDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val cameraPick = pickerDialog.findViewById<TextView>(R.id.cameraPick)
        val galleryPick = pickerDialog.findViewById<TextView>(R.id.galleryPick)

        cameraPick.setOnClickListener {
            ImagePicker.with(this)
                .cameraOnly()
                .crop()
                .compress(512)
                .maxResultSize(720, 720)
                .start()
            pickerDialog.dismiss()
        }
        galleryPick.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .compress(512)
                .maxResultSize(720, 720)
                .start()

            pickerDialog.dismiss()
        }


        pickerDialog.show()

        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val displayWidth = displayMetrics.widthPixels
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(pickerDialog.window!!.attributes)
        val dialogWindowWidth = (displayWidth * 0.8f).toInt()
        layoutParams.width = dialogWindowWidth
        pickerDialog.window!!.attributes = layoutParams
    }

    private fun updateProfile() {
        val userName = binding.name.text.toString()
        val userEmail = binding.email.text.toString()
        val userPhone = binding.phone.text.toString()
        val userAddress = binding.address.text.toString()

        if (userName.isEmpty()) {
            showToast("please enter a valid name.")
            return
        } else if (patientDOB.isEmpty()) {
            showToast("please enter a valid email.")
            return
        }else if (userEmail.isEmpty()) {
            showToast("please enter a valid email.")
            return
        } else if (userPhone.isEmpty()) {
            showToast("please enter a valid phone number.")
            return
        } else if (userAddress.isEmpty()) {
            showToast("please enter a valid address.")
            return
        } else if (patientGender.lowercase() != "male" && patientGender.lowercase() != "female") {
            showToast("please select your gender.")
            return
        }

        val json = JsonObject()
        json.addProperty("id", session.patientId)
        json.addProperty("patient_name", userName)
        json.addProperty("blood_group", "")
        json.addProperty("phoneNo", userPhone)
        json.addProperty("email", userEmail)
        json.addProperty("dob", patientDOB)
        json.addProperty("address", userAddress)
        json.addProperty("gender", patientGender)
        json.addProperty("age", "")

        profileViewModel.updateProfile(json)
    }

    private fun selectDob() {
        val constraintsBuilder = CalendarConstraints.Builder()
        val dateValidator: CalendarConstraints.DateValidator =
            DateValidatorPointBackward.now()
        constraintsBuilder.setValidator(dateValidator)

        val dobParse = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        var dobDate = Date()

        if (binding.dateOfBirth.text.isNotEmpty()) {
            dobDate = dobParse.parse(binding.dateOfBirth.text.toString())
        }

        val newDate = Date(dobDate.time + 86400000)

        val datePicker: MaterialDatePicker<Long> = MaterialDatePicker
            .Builder
            .datePicker()
            .setTheme(R.style.MaterialCalendarTheme)
            .setSelection(newDate.time)
            .setCalendarConstraints(constraintsBuilder.build())
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            .setTitleText("Select date of birth")
            .build()
        datePicker.show(childFragmentManager, "DATE_PICKER")

        datePicker.addOnPositiveButtonClickListener {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date = sdf.format(it)

            val stringDate: Date = sdf.parse(date)
            val consultationDate = isoFormat.format(stringDate)
            if (stringDate > Date()) {
                showToast("Date of Birth Cannot be Future Date.")
            } else {
                binding.dateOfBirth.text = date
                patientDOB = consultationDate.toString()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                val uri: Uri = data?.data!!
                println("got image $uri")
                binding.userImage.setImageURI(uri)
                val profileImage = File(uri.path)
                uploadProfileImage(profileImage)
            }
            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT)
                    .show()
            }
            else -> {
                Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadProfileImage(profileImage: File) {
        profileViewModel.uploadPatientImage(profileImage)
    }
}