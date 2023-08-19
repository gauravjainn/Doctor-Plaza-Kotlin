package com.doctorsplaza.app.ui.doctor.fragment.profile

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentEditDoctorProfileBinding
import com.doctorsplaza.app.ui.doctor.fragment.profile.model.DoctorData
import com.doctorsplaza.app.ui.doctor.loginSignUp.model.SpecialistsModel
import com.doctorsplaza.app.utils.*
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.JsonObject
import com.gym.gymapp.utils.SingleLiveEvent
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class EditDoctorProfileFragment : Fragment(R.layout.fragment_edit_doctor_profile),
    View.OnClickListener {

    private var selectedGender: String = ""
    private lateinit var profileData: DoctorData

    private var selectedDepartment: String = ""

    private lateinit var specialists: List<String>

    private lateinit var binding: FragmentEditDoctorProfileBinding

    private val doctorProfileViewModel: DoctorProfileViewModel by viewModels()

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader

    private var profileLoaded = false

    private var specializationLoaded = false

    private var allLoaded = SingleLiveEvent<Boolean>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_edit_doctor_profile, container, false)
            binding = FragmentEditDoctorProfileBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())
        binding.loader.isVisible = true
        doctorProfileViewModel.getSpecialization()
        doctorProfileViewModel.getDoctorProfile()
    }

    private fun setObserver() {
        doctorProfileViewModel.doctorProfile.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.status == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(), response.data.message)
                    } else if (response.data?.status == 200) {
                        profileData = response.data.data[0]
                        profileLoaded = true
                        allLoaded.postValue(profileLoaded && specializationLoaded)
                    }
                }

                is Resource.Loading -> {
                    appLoader.show()
                }

                is Resource.Error -> {
                    appLoader.dismiss()
                }
            }

            doctorProfileViewModel.specializations.observe(viewLifecycleOwner) { response ->
                when (response) {
                    is Resource.Success -> {

                        appLoader.dismiss()
                        if (response.data?.status == 401) {
                            session.isLogin = false
                            logOutUnAuthorized(requireActivity(), response.data.message)
                        } else {
                            specialists = response.data?.data!!
                            specializationLoaded = true
                            allLoaded.postValue(profileLoaded && specializationLoaded)
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

        doctorProfileViewModel.editDoctorProfile.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.status?.toInt() == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(), response.data.message)
                    } else
                        if (response.data?.status == "200") {
                            doctorProfileUpdated.postValue(true)
                            findNavController().navigate(R.id.action_editDoctorProfileFragment_to_doctorProfileFragment)

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

        doctorProfileViewModel.uploadDoctorImage.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    when (response.data?.status) {
                        401 -> {
                            session.isLogin = false
                            logOutUnAuthorized(requireActivity(), response.data.message)
                        }

                        200 -> {
                            session.loginImage = response.data.profile_picture
                            Glide.with(requireContext())
                                .applyDefaultRequestOptions(doctorRequestOption())
                                .load(response.data.profile_picture).into(binding.userImage)

                            doctorProfileUpdated.postValue(true)
                        }

                        else -> {
                            binding.userImage.setImageResource(R.drawable.doctor_placeholder)
                        }
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

        allLoaded.observe(viewLifecycleOwner)
        {
            if (it) {
                binding.loader.isVisible = false
                setProfileData(profileData)
                setSpecialistSpinner()
            }
        }
    }

    private fun setSpecialistSpinner() {
        val departmentSpinnerAdapter =
            ArrayAdapter(requireContext(), R.layout.spinner_text, specialists)
        departmentSpinnerAdapter.setDropDownViewResource(R.layout.spinner_text)
        binding.departmentSpinner.adapter = departmentSpinnerAdapter
        binding.departmentSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    selectedDepartment = binding.departmentSpinner.selectedItem.toString()
                }
            }

        specialists.forEachIndexed { index, data ->
            if (data == profileData.specialization) {
                binding.departmentSpinner.setSelection(index)
            }
        }

        val genderSpinnerAdapter =
            ArrayAdapter.createFromResource(
                requireContext(),
                R.array.gender_menu,
                R.layout.spinner_text
            )
        genderSpinnerAdapter.setDropDownViewResource(R.layout.spinner_text)
        binding.genderSpinner.adapter = genderSpinnerAdapter
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

        specialists.forEachIndexed { index, data ->
            if (data == profileData.specialization) {
                binding.departmentSpinner.setSelection(index)
            }
        }

        if (profileData.gender != null) {
            if (profileData.gender.lowercase() == "male") {
                binding.genderSpinner.setSelection(1)
            } else if (profileData.gender.lowercase() == "female") {
                binding.genderSpinner.setSelection(2)
            }
        }

    }

    private fun setProfileData(data: DoctorData) {
        with(binding) {
            Glide.with(requireContext()).applyDefaultRequestOptions(doctorRequestOption())
                .load(data.profile_picture).into(userImage)
            name.setText(data.doctorName)
            consultationFees.setText(data.consultationfee)
            email.text = data.email
            phone.text = data.contactNumber.toString()
            address.setText(data.address)
            experienceEdt.setText(data.experience)
        }
    }

    private fun setOnClickListener() {
        with(binding) {
            backArrow.setOnClickListener(this@EditDoctorProfileFragment)
            saveBtn.setOnClickListener(this@EditDoctorProfileFragment)
            changeProfileImage.setOnClickListener(this@EditDoctorProfileFragment)
        }
    }


    private fun validateAndUpdateProfile() {
        val name = binding.name.text.toString()
        val consultationFee = binding.consultationFees.text.toString()
        val email = binding.email.text.toString()
        val phone = binding.phone.text.toString()
        val address = binding.address.text.toString()
        val experience = binding.experienceEdt.text.toString()

        if (phone.length < 10) {
            showToast("Please enter a valid phone number")
            return
        } else if (!email.isValidEmail()) {
            showToast("Please enter a valid email")
            return
        } else if (consultationFee.isEmpty()) {
            showToast("Please enter a valid consultation fee")
            return
        } else if (address.isEmpty()) {
            showToast("Please enter a valid address")
            return
        } else if (experience.isEmpty()) {
            showToast("Please enter your experience")
            return
        }

        val jsonObject = JsonObject()
        jsonObject.addProperty("id", profileData._id)
        jsonObject.addProperty("contactNumber", phone)
        jsonObject.addProperty("specialization", selectedDepartment)
        jsonObject.addProperty("consultationfee", consultationFee)
        jsonObject.addProperty("doctorName", name)
        jsonObject.addProperty("email", email)
        jsonObject.addProperty("address", address)
        jsonObject.addProperty("gender", selectedGender)
        jsonObject.addProperty("experience", experience)
        doctorProfileViewModel.editDoctorProfile(jsonObject)
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


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backArrow -> {
                findNavController().popBackStack()
            }

            R.id.changeProfileImage -> {
                showPickerDialog()
            }

            R.id.saveBtn -> {
                validateAndUpdateProfile()
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
//                try {
//                    val file_uri = data.data
//                    val bitmap = MediaStore.Images.Media.getBitmap(
//                        requireActivity().contentResolver,
//                        file_uri
//                    )
//                    binding.userImage.setImageBitmap(bitmap)
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
                val profileImage = File(uri.path)
                println("got uri.path $uri.path")
                doctorProfileViewModel.uploadDoctorImage(profileImage)
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
}
