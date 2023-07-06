package com.doctorsplaza.app.ui.patient.fragments.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentProfileBinding
import com.doctorsplaza.app.ui.patient.fragments.clinicDoctors.adapter.ClinicDoctorsAdapter
import com.doctorsplaza.app.ui.patient.fragments.profile.adapter.PatientAdminReportAdapter
import com.doctorsplaza.app.ui.patient.fragments.profile.adapter.ReportAdapter
import com.doctorsplaza.app.ui.patient.fragments.profile.model.PatientReportData
import com.doctorsplaza.app.ui.patient.fragments.profile.model.ProfileData
import com.doctorsplaza.app.utils.*
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.ImagePicker as MultiImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


private const val LAST_OPENED_URI_KEY =
    "com.example.android.actionopendocument.pref.LAST_OPENED_URI_KEY"

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile), View.OnClickListener {

    private var reportFile: File? = null

    private lateinit var binding: FragmentProfileBinding

    @Inject
    lateinit var session: SessionManager

    private val profileViewModel: ProfileViewModel by viewModels()

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader

    @Inject
    lateinit var clinicDoctorsAdapter: ClinicDoctorsAdapter

//    @Inject
//    lateinit var reportAdapter: PatientReportAdapter

    @Inject
    lateinit var reportAdapter: ReportAdapter

    @Inject
    lateinit var bloodReportAdapter: ReportAdapter

    @Inject
    lateinit var reportAdminAdapter: PatientAdminReportAdapter

    private lateinit var profileData: ProfileData

    private lateinit var pdfPicker: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
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

        requireActivity().getSharedPreferences("Profile", Context.MODE_PRIVATE)
            .let { sharedPreferences ->
                if (sharedPreferences.contains(LAST_OPENED_URI_KEY)) {
                    val documentUri =
                        sharedPreferences.getString(LAST_OPENED_URI_KEY, null)?.toUri()
                            ?: return@let
                    openDocument(documentUri)
                }
            }
    }


    private fun setObserver() {
        profileViewModel.profile.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    binding.loader.isVisible = false
                    binding.errorMessage.isVisible = false
                    if (response.data?.status == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(), response.data.message)
                    } else {
                        if (response.data!!.status == 200) {
                            profileData = response.data.data
                            setUserData(profileData)
                        }
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
                    if (response.data?.status == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(), response.data.message)
                    } else {
                        if (response.data!!.code == 200) {
                            binding.patientReportsRv.isVisible = true
                            setReportRv(response.data.reports, response.data.admin_reports)
                        } else {
                            binding.patientReportsRv.isVisible = false
                        }
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
                    if (response.data?.status == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(), response.data.message)
                    } else {
                        if (response.data!!.code == 200) {

                            profileViewModel.getPatientReport()
                            showToast(response.data.message)
                        } else {
                            showToast(response.data.message)
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
    }

    private fun setReportRv(data: List<PatientReportData>, dataAdmin: List<PatientReportData>) {
        reportAdapter.differ.submitList(data)
        binding.patientReportsRv.apply {
            setHasFixedSize(true)
            itemAnimator?.addDuration=0
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = reportAdapter
        }

        reportAdapter.showDeleteButton(true)

        bloodReportAdapter.differ.submitList(dataAdmin)
        binding.patientAdminReportsRv.apply {
            setHasFixedSize(true)
            itemAnimator?.addDuration=0
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = bloodReportAdapter
        }

        bloodReportAdapter.showDeleteButton(false)
    }

    private fun setUserData(data: ProfileData) {

        val dobParse = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val dobFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        var userDob = ""

        session.loginDOB = data.dob.toString()

        if (session.loginDOB.isNotEmpty() && session.loginDOB != "null") {
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
                session.loginAddress = data.address
                if (data.age != null) {
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
        Glide.with(requireContext()).applyDefaultRequestOptions(patientRequestOption())
            .load(session.loginImage).into(binding.userImage)
    }

    private fun openDocument(documentUri: Uri) {
        requireActivity().getSharedPreferences("Profile", Context.MODE_PRIVATE).edit {
            putString(LAST_OPENED_URI_KEY, documentUri.toString())
        }
    }


    private fun setOnClickListener() {
        binding.profileEdit.setOnClickListener(this@ProfileFragment)
        binding.addReport.setOnClickListener(this@ProfileFragment)

        reportAdapter.setOnReportDeleteClickListener {
            profileViewModel.deleteReport(it)
        }

        reportAdapter.setOnPDFClickListener { data->
            val bundle = Bundle().apply {
                putString("prescription", data.image)
                putString("from", "Profile")
            }
            findNavController().navigate(R.id.prescriptionFragment, bundle)
        }

        reportAdapter.setOnImageClickListener { data, position ->
            openReportImagesFragment(data,position)

        }


        bloodReportAdapter.setOnPDFClickListener { data->
            val bundle = Bundle().apply {
                putString("prescription", data.image)
            }
            findNavController().navigate(R.id.prescriptionFragment2, bundle)
        }

        bloodReportAdapter.setOnImageClickListener { data, position ->
            openReportImagesFragment(data,position)

        }
    }

    private fun openReportImagesFragment(data: List<PatientReportData>, position: Int) {
        val imageList:MutableList<String> = ArrayList()

        data.forEach{
            val extension = it.file_name.substring(it.file_name.lastIndexOf("."))
            if(extension!=".pdf"){
                imageList.add(it.image)
            }
        }

        val bundle = Bundle()
        bundle.putString("images", Gson().toJson(imageList as List<String>))
        bundle.putInt("position", position)
        findNavController().navigate(R.id.reportsImagesFragment, bundle)

    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.profileEdit -> {
                val bundle = Bundle().apply {
                    putString("dob", profileData.dob)
                    putString("from", "profile")
                }
                findNavController().navigate(R.id.editProfileFragment, bundle)
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

                reportFile = File(uri.path)
                addReport()
            }
            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT)
                    .show()
            }
            else -> {
//                Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addReport() {
        val params: MutableMap<String, RequestBody> = HashMap()
        params["patient_id"] = getRequestBodyFromString(session.patientId) as RequestBody
        val imageFile = if (reportFile != null) {
            reportFile
        } else {
            File("")
        }

        imageFile?.let { profileViewModel.addReport(params, it) }

        profileViewModel.addReport.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.status == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(), response.data.message)
                    } else {
                        if (response.data!!.code == 200) {
                            showToast(response.data.message)
                            profileViewModel.getPatientReport()
                        } else {
                            showToast(response.data.message)
                        }
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
        val pdfIcon = pickerDialog.findViewById<ImageView>(R.id.pdfIcon)

        pdfIcon.isVisible = true
        pdfPick.isVisible = true

        cameraPick.setOnClickListener {
            ImagePicker.with(this).cameraOnly().crop().compress(512).maxResultSize(720, 720).start()
            pickerDialog.dismiss()
        }

        galleryPick.setOnClickListener {

           /* MultiImagePicker.with(requireActivity())
                .provider(ImageProvider.GALLERY) //Or bothCameraGallery()
                .setMultipleAllowed(true)
                .createIntentFromDialog { launcher.launch(it) }*/

              ImagePicker.with(this)
                  .galleryOnly()
                  .crop()

                  .compress(512)
                  .maxResultSize(720, 720)
                  .start()

            pickerDialog.dismiss()
        }

        pdfPick.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "application/pdf"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
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

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data!!
                // Use the uri to load the image
                // Only if you are not using crop feature:
                uri.let { _ ->
                    requireActivity().contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                //////////////
            }
        }

    @SuppressLint("Range")
    private val launchPDFActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent = result.data!!

                val uri: Uri = data.data!!
                requireActivity().contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val filePath = copyFileToInternalStorage(uri, "reports")
                reportFile = File(filePath)
                addReport()
            }
        }


    private fun copyFileToInternalStorage(uri: Uri, newDirName: String): String? {
        val returnCursor: Cursor? = requireActivity().contentResolver.query(
            uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null
        )

        val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = returnCursor?.getColumnIndex(OpenableColumns.SIZE)
        returnCursor?.moveToFirst()
        val name = nameIndex?.let { returnCursor.getString(it) }
        sizeIndex?.let { returnCursor.getLong(it).toString() }
        val output: File = if (newDirName != "") {
            val dir = File(requireContext().filesDir.toString() + "/" + newDirName)
            if (!dir.exists()) {
                dir.mkdir()
            }
            File(requireContext().filesDir.toString() + "/" + newDirName + "/" + name)
        } else {
            File(requireContext().filesDir.toString() + "/" + name)
        }
        try {
            val inputStream: InputStream? = requireActivity().contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(output)
            var read = 0
            val bufferSize = 1024
            val buffers = ByteArray(bufferSize)
            while (inputStream?.read(buffers).also {
                    if (it != null) {
                        read = it
                    }
                } != -1) {
                outputStream.write(buffers, 0, read)
            }
            inputStream?.close()
            outputStream.close()
        } catch (e: Exception) {
            Log.e("Exception", e.message!!)
        }
        return output.path
    }

    override fun onResume() {
        super.onResume()
        setObserver()
    }
}