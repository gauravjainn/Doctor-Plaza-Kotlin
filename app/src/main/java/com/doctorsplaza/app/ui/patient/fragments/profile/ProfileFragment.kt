package com.doctorsplaza.app.ui.patient.fragments.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentUris
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.DisplayMetrics
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentProfileBinding
import com.doctorsplaza.app.ui.patient.fragments.clinicDoctors.adapter.ClinicDoctorsAdapter
import com.doctorsplaza.app.ui.patient.fragments.profile.adapter.PatientReportAdapter
import com.doctorsplaza.app.ui.patient.fragments.profile.model.PatientReportData
import com.doctorsplaza.app.ui.patient.fragments.profile.model.ProfileData
import com.doctorsplaza.app.utils.*
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.RequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile), View.OnClickListener {

    private var reportImage: File? = null

    private lateinit var binding: FragmentProfileBinding

    @Inject
    lateinit var session: SessionManager

    private val profileViewModel: ProfileViewModel by viewModels()

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader

    @Inject
    lateinit var clinicDoctorsAdapter: ClinicDoctorsAdapter

    @Inject
    lateinit var reportAdapter: PatientReportAdapter

    private lateinit var profileData: ProfileData
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_profile, container, false)
            binding = FragmentProfileBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())
        binding.loader.isVisible = true
        profileViewModel.getProfile()
        profileViewModel.getPatientReport()
    }


    private fun setObserver() {
        profileViewModel.profile.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    binding.loader.isVisible = false
                    binding.errorMessage.isVisible = false
                    if (response.data!!.status == 200) {
                        profileData = response.data.data
                        setUserData(profileData)
                    } else{
                        setUserData(profileData)
                    }
                }
                is Resource.Loading -> {
                    binding.loader.isVisible = true
                    appLoader.show()
                }
                is Resource.Error -> {
                    binding.loader.isVisible = true
                    binding.errorMessage.isVisible = true
                    appLoader.dismiss()
                }

            }
        }

        profileViewModel.getPatientReports.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    binding.loader.isVisible = false
                    binding.errorMessage.isVisible = false
                    if (response.data!!.code == 200) {
                        setReportRv(response.data.data)
                    }
                }
                is Resource.Loading -> {
                    binding.loader.isVisible = true
                    appLoader.show()
                }
                is Resource.Error -> {
                    binding.loader.isVisible = true
                    binding.errorMessage.isVisible = true
                    appLoader.dismiss()
                }

            }
        }

        profileViewModel.deleteReport.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data!!.code == 200) {
                        profileViewModel.getPatientReport()
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
    }

    private fun setReportRv(data: List<PatientReportData>) {
        reportAdapter.differ.submitList(data)
        binding.patientReportsRv.apply {
            setHasFixedSize(true)
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = reportAdapter
        }
    }

    private fun setUserData(data: ProfileData) {

        val dobParse = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val dobFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        var userDob = ""

        if (session.loginDOB.isNotEmpty()&&session.loginDOB!="null") {
            val stringDate: Date = dobParse.parse(session.loginDOB)
            userDob = dobFormat.format(stringDate)
        }

        with(binding) {
            if (data != null) {
                name.text = data.patient_name
                dateOfBirth.text = userDob
                gender.text = data.gender
                email.text = data.email
                phone.text = data.contact_number.toString()
                address.text = data.address
                if(data.age!=null){
                session.loginAge = data.age
                }
                if (data.profile_picture != null) {
                    session.loginImage = data.profile_picture
                } else {
                    session.loginImage = ""
                }

            } else {
                name.text = session.loginName
                dateOfBirth.text = userDob
                gender.text = session.loginGender
                email.text = session.loginEmail
                phone.text = session.loginPhone
                address.text = session.loginAddress
            }
        }
        Glide.with(requireContext()).applyDefaultRequestOptions(profileRequestOption())
            .load(session.loginImage).into(binding.userImage)
    }

    private fun setOnClickListener() {
        binding.profileEdit.setOnClickListener(this@ProfileFragment)
        binding.addReport.setOnClickListener(this@ProfileFragment)

        reportAdapter.setOnReportDeleteClickListener {
            profileViewModel.deleteReport(it)
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.profileEdit -> {
                val bundle = Bundle().apply {
                    putString("dob",profileData.dob)
                }
                findNavController().navigate(R.id.editProfileFragment,bundle)
            }
            R.id.addReport -> {
                showPickerDialog()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {

                val uri: Uri = data?.data!!

                reportImage = File(uri.path)
                addReport()
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

    private fun addReport() {
        val params: MutableMap<String, RequestBody> = HashMap()
        params["patient_id"] = getRequestBodyFromString(session.patientId) as RequestBody
        val imageFile = if (reportImage != null) {
            reportImage
        } else {
            File("")
        }

        imageFile?.let { profileViewModel.addReport(params, it) }

        profileViewModel.addReport.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data!!.code == 200) {
                        showToast(response.data.message)
                        profileViewModel.getPatientReport()
                    } else {
                        showToast(response.data.message)
                    }
                }
                is Resource.Loading -> {
                    appLoader.show()
                }
                is Resource.Error -> {
                    showToast(response.message.toString())
                    appLoader.dismiss()
                }
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
        val pdfPick = pickerDialog.findViewById<TextView>(R.id.pdfPick)

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
        pdfPick.setOnClickListener {
            val intent = Intent()
            intent.type = "application/pdf"
            intent.action = Intent.ACTION_OPEN_DOCUMENT
            launchPDFActivity.launch(intent)
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

    @SuppressLint("Range")
    private val launchPDFActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent = result.data!!

                val uri: Uri = data?.data!!
                val uriString: String = uri.toString()
                var pdfName: String? = null
                requireActivity().contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION)
                println("rsdasdadad ${RealPathUtil.getRealPath(requireContext(), uri)}")
                if (uriString.startsWith("content://")) {
                    var myCursor: Cursor? = null
                    try {
                        // Setting the PDF to the TextView
                        myCursor = requireContext().contentResolver.query(uri, null, null, null, null)
                        if (myCursor != null && myCursor.moveToFirst()) {
                            pdfName = myCursor.getString(myCursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                        }
                    } finally {
                        myCursor?.close()
                    }
                }
                reportImage = File(RealPathUtil.getRealPath(requireContext(), uri))
                addReport()
//
            }
         }
}