package com.doctorsplaza.app.ui.doctor.fragment.appointments

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentDoctorAppointmentDetailsBinding
import com.doctorsplaza.app.ui.doctor.fragment.appointments.adapter.AppointmentPatientReportAdapter
import com.doctorsplaza.app.ui.doctor.fragment.appointments.bottomSheet.OptionsBottomSheetFragment
import com.doctorsplaza.app.ui.doctor.fragment.appointments.model.PrescriptionData
import com.doctorsplaza.app.ui.doctor.fragment.prescription.model.Medicine
import com.doctorsplaza.app.ui.patient.fragments.appointments.model.AppointmentData
import com.doctorsplaza.app.ui.patient.fragments.appointments.model.PatientReports
import com.doctorsplaza.app.ui.videoCall.VideoActivity
import com.doctorsplaza.app.utils.*
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class DoctorAppointmentDetailsFragment : Fragment(R.layout.fragment_doctor_appointment_details),
    View.OnClickListener {

    private var prescriptionImage: String = ""
    private lateinit var medicine: List<Medicine>

    private var patientReportsAdapter: AppointmentPatientReportAdapter  = AppointmentPatientReportAdapter()

    private lateinit var prescriptionData: List<PrescriptionData>
    private lateinit var appointmentData: AppointmentData
    private var appointmentStatus: String = ""
    private var appointmentId: String = ""
    private lateinit var binding: FragmentDoctorAppointmentDetailsBinding

    private val doctorAppointmentViewModel: DoctorAppointmentViewModel by viewModels()

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

    private val showDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

    private val showDayFormat = SimpleDateFormat("EEEE", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (currentView == null) {
            currentView =
                inflater.inflate(R.layout.fragment_doctor_appointment_details, container, false)
            binding = FragmentDoctorAppointmentDetailsBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }

        return currentView!!
    }


    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())

        binding.loader.isVisible = true
        appointmentId = arguments?.getString("appointId").toString()
        val jsonObject = JsonObject()
        jsonObject.addProperty("id", appointmentId)
        doctorAppointmentViewModel.getAppointmentDetails(jsonObject)

//        doctorAppointmentViewModel.getAppointmentPrescriptionDetails(appointmentId)
    }

    private fun setObserver() {

        doctorAppointmentViewModel.doctorAppointmentDetails.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.status == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(), response.data.message)
                    } else {
                        if (response.data?.success!!) {
                            binding.loader.isVisible = false
                            appLoader.dismiss()
                            appointmentData = response.data.data
                            setAppointmentData(response.data.data)
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


        doctorAppointmentViewModel.updateAppointmentStatus.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.status?.toInt() == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(), response.data.message)
                    } else {
                        if (response.data?.code == "200") {
                            appLoader.dismiss()
                            binding.loader.isVisible = false
                            when (appointmentStatus) {
                                "done" -> {
                                    binding.cancelAppointment.isVisible = false
                                    binding.completeAppointment.isVisible = false
                                    binding.addPrescription.isVisible = true
                                }
                                "cancel" -> {
                                    findNavController().navigate(R.id.action_doctorAppointmentDetailsFragment_to_doctorAppointmentFragment)
                                }
                                else -> {
                                    showToast("Something went wrong!")
                                }
                            }
                        } else {
                            showToast(response.data?.message.toString())
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
        doctorAppointmentViewModel.doctorAppointmentPrescription.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.status == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(), response.data.message)
                    } else {
                        if (response.data?.code == 200) {
                            appLoader.dismiss()
                            binding.loader.isVisible = false
                            if (response.data.data.isNotEmpty()) {
                                prescriptionData = response.data.data
                                prescriptionData.forEach {
                                    if (it.type == "SCAN") {
                                        prescriptionImage = it.image
                                    } else {
                                        medicine = it.medicine
                                    }
                                }
                            }

                        } else {
                            showToast(response.data?.message.toString())
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

        doctorAppointmentViewModel.uploadPrescription.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    binding.loader.isVisible = false
                    if (response.data?.status?.toInt() == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(), response.data.message)
                    } else {
                        if (response.data?.code?.toInt() == 200) {
                            val jsonObject = JsonObject()
                            jsonObject.addProperty("id", appointmentId)
                            doctorAppointmentViewModel.getAppointmentDetails(jsonObject)
                        } else {
                            showToast(response.data?.message.toString())
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

        doctorAppointmentViewModel.generateVideoToken.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.status == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(),response.data.message)
                    } else {
                    if (response.data?.code != null && response.data.code == 200) {
                        response.data.message.let { showToast(it) }
                        appLoader.dismiss()
                        session.callStatus = ""
                        val jsonObject = JsonObject().apply {
                            addProperty("type", "doctor")
                            addProperty("name", appointmentData.patientname)
                            addProperty("status", "calling")
                            addProperty("appointmentId", appointmentId)
                        }
                        doctorAppointmentViewModel.callNotify(jsonObject)
                        startActivity(
                            Intent(
                                requireActivity(),
                                VideoActivity::class.java
                            ).putExtra("videoToken", response.data.data.doctor_token)
                                .putExtra("name", appointmentData.patientname)
                                .putExtra("appointId", appointmentData._id)
                        )

                    } else {
                        response.data?.message?.let { showToast(it) }
                    }
                }}
                is Resource.Loading -> {
                    appLoader.show()
                }
                is Resource.Error -> {
                    appLoader.dismiss()
                }
                else -> {}
            }
        }
        doctorAppointmentViewModel.callNotify.observe(viewLifecycleOwner) { }

    }

    private fun setAppointmentData(data: AppointmentData) {
        with(binding) {
            Glide.with(requireContext()).applyDefaultRequestOptions(patientRequestOption())
                .load(data.user_id.profile_picture).into(patientImage)
            patientName.text = data.patientname
            patientName2.text = data.patientname
            patientId.text = "Patient Id:\n${data.patient_id}"

            val appointmentParsedDate = isoFormat.parse(data.date)
            val appointmentFormattedDate = showDateFormat.format(appointmentParsedDate)
            val formattedDay = showDayFormat.format(appointmentParsedDate)
            appointmentDateTime.text = "$formattedDay  $appointmentFormattedDate (${data.room_time_slot_id.timeSlotData.start_time} - ${data.room_time_slot_id.timeSlotData.end_time})"

//            appointmentDateTime.text = "${data.room_time_slot_id.timeSlotData.day}  $formattedDate (${data.room_time_slot_id.timeSlotData.start_time} - ${data.room_time_slot_id.timeSlotData.end_time})"

            clinicDetails.text = data.clinic_id.location
            contactDetails.text = data.mobile
            ageDetails.text = data.age
            gender.text = data.gender


            consultationFees.text = "₹${data.doctor_id.consultationfee}"

            val discountAmount = data.doctor_id.consultationfee.toInt() - data.consultation_fee

            if(discountAmount>=1){
                binding.discountAmt.text = "-₹${discountAmount}"
            }else{
                binding.discountAmt.isVisible = false
                binding.discountLbl.isVisible = false
            }

//            binding.videoCallIcon.isVisible = data.appointment_type.lowercase() == "online"
            binding.joinVideoCall.isVisible = data.appointment_type.lowercase() == "online"

            if (data.problem?.isEmpty() == true) {
                problem.isVisible = false
                problemLbl.isVisible = false
            } else {
                problem.isVisible = true
                problemLbl.isVisible = true
                problem.text = data.problem
            }

            setPrescriptionStatusView(data)
            setPaymentStatusView(data)

            if(data.pat_reports.isNotEmpty()){
                reportsLbl.isVisible = true
                reportsRv.isVisible = true
                setPatientReports(data.pat_reports)
            } else {
                reportsLbl.isVisible = false
                reportsRv.isVisible = false
            }
        }
    }

    private fun setPatientReports(patReports: List<PatientReports>) {
        patientReportsAdapter.differ.submitList(patReports)
        binding.reportsRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
            adapter = patientReportsAdapter
        }

        patientReportsAdapter.setOnReportClickListener {
            val bundle = Bundle().apply {
                putString("from","appointmentDetails")
                putString("prescription",it.image)
                putString("prescriptionImage",it.image)
            }
            if(it.title.contains(".pdf")){
                findNavController().navigate(R.id.prescriptionFragment2,bundle)
            }else{
                findNavController().navigate(R.id.imagePrescriptionViewFragment,bundle)
            }
        }
    }

    private fun setPrescriptionStatusView(data: AppointmentData) {
        if (data.status == "done") {
            binding.cancelAppointment.isVisible = false
            binding.completeAppointment.isVisible = false
            binding.addPrescription.isVisible = true
            if (data.prescription != "false") {
                doctorAppointmentViewModel.getAppointmentPrescriptionDetails(appointmentId)
                binding.addPrescription.text = "View Prescription"
            } else {
                binding.addPrescription.text = "Add Prescription"
            }
        } else {
            binding.cancelAppointment.isVisible = true
            binding.completeAppointment.isVisible = true
            binding.addPrescription.isVisible = false
        }
    }

    private fun setPaymentStatusView(data: AppointmentData) {


        if (data.payment_id != null) {
            binding.totalAmt.text = "₹${data.payment_id.amount}"
            binding.paymentMode.text = data.mode_of_payment
        } else {
            binding.totalAmt.text = "₹${data.consultation_fee}"
            binding.paymentMode.text = "Cash"
        }
        binding.paymentStatus.text = if (data.payment_status) "Success" else "Pending"
    }

    private fun setOnClickListener() {
        with(binding) {
            backArrow.setOnClickListener(this@DoctorAppointmentDetailsFragment)
            addPrescription.setOnClickListener(this@DoctorAppointmentDetailsFragment)
            completeAppointment.setOnClickListener(this@DoctorAppointmentDetailsFragment)
            cancelAppointment.setOnClickListener(this@DoctorAppointmentDetailsFragment)
//            videoCallIcon.setOnClickListener(this@DoctorAppointmentDetailsFragment)
            joinVideoCall.setOnClickListener(this@DoctorAppointmentDetailsFragment)
        }
    }

    private fun updateAppointmentStatus(status: String) {
        appointmentStatus = status
        val jsonObject = JsonObject()
        jsonObject.addProperty("doctor_id", appointmentData.doctor_id._id)
        jsonObject.addProperty("appointment_id", appointmentId)
        jsonObject.addProperty("status", appointmentStatus)
        doctorAppointmentViewModel.updateAppointmentStatus(jsonObject)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backArrow -> {
                findNavController().popBackStack()
            }

            R.id.addPrescription -> {
                if (appointmentData.prescription == "false") {
                    showUploadPrescriptionOptions()
                } else {
                    showViewPrescriptionPopup()
                }
            }

            R.id.cancelAppointment -> {
                showPopUp("cancel")
            }

            R.id.completeAppointment -> {
                showPopUp("done")
            }

            R.id.joinVideoCall -> {

                val parsedStartTime = isoFormat.parse(appointmentData.room_time_slot_id.timeSlotData.startTimeDate)
                val parsedEndTime = isoFormat.parse(appointmentData.room_time_slot_id.timeSlotData.endTimeDate)

                val currentTime = Date().time
                val ctf = isoFormat.format(currentTime)

                if (isoFormat.parse(ctf).before(parsedStartTime)) {
                    showVideoCallNotStartAlertPopUp("Your booking Time is not started, Please try again at your booking time.")
                    return
                }

                if (isoFormat.parse(ctf).after(parsedEndTime)) {
                    showVideoCallAlertPopUp("Your booking Time has been completed, Please contact admin in case of any queries.")
                    return
                }

                val jsonObject = JsonObject().apply {
                    addProperty("id", appointmentId)
                    addProperty("type", "doctor")
                }
                doctorAppointmentViewModel.generateVideoToken(jsonObject)
                hideKeyboard(requireActivity(),binding.backArrow)
            }
        }
    }


    private fun showVideoCallAlertPopUp(message: String) {

        AlertDialog.Builder(context)
            .setMessage(message)
            .setPositiveButton(
                "yes"
            ) { _, _ ->
                updateAppointmentStatus(message)
            }
            .setNegativeButton("no", null)
            .show()
    }

    private fun showVideoCallNotStartAlertPopUp(msg: String) {
        AlertDialog.Builder(context)
            .setMessage(msg)
            .setPositiveButton("ok", null)
            .show()
    }

    private fun showViewPrescriptionPopup() {
        val pickerDialog = Dialog(requireActivity())
        pickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        pickerDialog.setCancelable(true)
        pickerDialog.setContentView(R.layout.dialogue_prescription)
        pickerDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val prescriptionImageIcon = pickerDialog.findViewById<ImageView>(R.id.cameraIcon)
        val prescriptionImageView = pickerDialog.findViewById<TextView>(R.id.prescriptionImage)
        val prescriptionPdfIcon = pickerDialog.findViewById<ImageView>(R.id.pdfIcon)
        val prescriptionPdf = pickerDialog.findViewById<TextView>(R.id.prescriptionPdf)

        prescriptionImageIcon.isVisible = prescriptionImage.isNotEmpty()
        prescriptionPdfIcon.isVisible = prescriptionImage.isEmpty()

        prescriptionImageView.setOnClickListener {
            val bundle = Bundle().apply {
                putString("prescriptionImage", prescriptionImage)
            }
            findNavController().navigate(R.id.imagePrescriptionViewFragment, bundle)
            pickerDialog.dismiss()
        }

        prescriptionPdf.setOnClickListener {
            val bundle = Bundle().apply {
                putString("prescriptionAdded", "yes")
                putString("appointmentId", appointmentId)
                putString("appointmentTime", binding.appointmentDateTime.text.toString())
                putString("consultationFee", appointmentData.consultation_fee.toString())
                putString("clinicName", appointmentData.clinic_id.clinicName)
                putString("clinicLocation", appointmentData.clinic_id.location)
            }

            findNavController().navigate(R.id.addPrescriptionWithMedicineFragment, bundle)
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

    private fun showUploadPrescriptionOptions() {
        val bottomSheet = OptionsBottomSheetFragment()
        bottomSheet.show(
            childFragmentManager,
            "ModalBottomSheet"
        )

        bottomSheet.setOnPrescriptionClickListener {
            val bundle = Bundle().apply {
                putString("appointmentId", appointmentId)
                putString("clinicName", appointmentData.clinic_id.clinicName)
                putString("clinicLocation", appointmentData.clinic_id.location)
                putString("consultationFee", appointmentData.consultation_fee.toString())
                putString("appointmentTime", binding.appointmentDateTime.text.toString())
                putString("patientName", appointmentData.patientname)
                putString("patientId", appointmentData.patient_id)
            }
            findNavController().navigate(R.id.addPrescriptionWithMedicineFragment, bundle)
        }

        bottomSheet.setOnScanClickListener {
            ImagePicker.with(this)
                .cameraOnly()
                .crop()
                .compress(512)

                .maxResultSize(720, 1080)
                .start()
        }

        bottomSheet.setOnImagePickerClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .compress(512)
                .maxResultSize(720, 1080)
                .start()
        }
    }

    private fun showPopUp(status: String) {
        val message = if (status == "done") {
            "Are you sure you want to Complete this Appointment?"
        } else {
            "Are you sure you want to Cancel Appointment?"
        }
        AlertDialog.Builder(context)
            .setMessage(message)
            .setPositiveButton(
                "yes"
            ) { _, _ ->
                updateAppointmentStatus(status)
            }
            .setNegativeButton("no", null)
            .show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                val uri: Uri = data?.data!!
                println("got image $uri")
                val prescriptionImage = File(uri.path)
                doctorAppointmentViewModel.uploadPrescription(prescriptionImage, appointmentId)
//                showToast("Prescription Image upload is in under construction..")

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