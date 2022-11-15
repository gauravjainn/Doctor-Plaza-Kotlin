package com.doctorsplaza.app.ui.patient.fragments.appointments

import android.app.AlertDialog
import android.app.Dialog
import android.app.DownloadManager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentAppointmentDetailsBinding
import com.doctorsplaza.app.ui.doctor.fragment.appointments.model.PrescriptionData
import com.doctorsplaza.app.ui.patient.fragments.addAppointmentForm.model.RoomTimeSlotsData
import com.doctorsplaza.app.ui.patient.fragments.appointments.model.AppointmentData
import com.doctorsplaza.app.ui.patient.fragments.bookAppointment.adapter.BookTimeAdapter
import com.doctorsplaza.app.ui.videoCall.VideoActivity
import com.doctorsplaza.app.utils.*

import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject




@AndroidEntryPoint
class AppointmentDetailsFragment : Fragment(R.layout.fragment_appointment_details),
    View.OnClickListener {

    private lateinit var  downloadDialog: Dialog
    private var downloadId: Long = 0L
    private lateinit var prescriptionData: List<PrescriptionData>
    private lateinit var rescheduleDialog: Dialog
    private var timeSlotsListPosition: Int = 0
    private lateinit var consultationDateView: TextView
    private lateinit var consultationTimeView: TextView
    private var selectedAppointmentType: String = "Offline"
    private var timeSlotSelected: Boolean = false
    private var consultationDate: String? = ""

    private var selectedConsultationDate: String? = ""

    private var appointmentId: String = ""

    private var from: String = ""

    private lateinit var binding: FragmentAppointmentDetailsBinding

    @Inject
    lateinit var session: SessionManager

    private val appointmentViewModel: AppointmentViewModel by viewModels()

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader

    private lateinit var appointmentData: AppointmentData

    private var timeSlotsList: MutableList<RoomTimeSlotsData> = ArrayList()

    private var rating: Float = 0F

    @Inject
    lateinit var bookTimeAdapter: BookTimeAdapter

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    private val showDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    private val showDayFormat = SimpleDateFormat("EEEE", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_appointment_details, container, false)
            binding = FragmentAppointmentDetailsBinding.bind(currentView!!)
            init()
            showProgressDialog()
            setObserver()
            setOnClickListener()
            setOnAdapterClickListener()
        }
        return currentView!!
    }


    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())
        binding.loader.isVisible = true
        from = arguments?.getString("from").toString()
        appointmentId = arguments?.getString("appointmentId").toString()
        if (from == "upcoming") {
            binding.ratingGroup.isVisible = false
            binding.saveBtn.isVisible = false
            binding.rescheduleCancelGroup.isVisible = true
        } else {
            binding.ratingGroup.isVisible = true
            binding.saveBtn.isVisible = true
            binding.rescheduleCancelGroup.isVisible = false
        }
        val jsonObject = JsonObject()
        jsonObject.addProperty("id", appointmentId)
        appointmentViewModel.getAppointmentDetails(jsonObject = jsonObject)
        binding.ratingExperienceBar.setOnRatingBarChangeListener { ratingBar, _, _ ->
            rating = ratingBar.rating
        }
    }


    private fun setObserver() {
        appointmentViewModel.appointmentDetails.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    binding.loader.isVisible = false
                    binding.errorMessage.isVisible = false
                    if (response.data?.status == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(), response.data.message)
                    } else {
                        if (response.data?.success!!) {
                            appointmentData = response.data.data
                            setAppointmentsDetails(response.data.data)
                        } else {
                            showToast(response.data.message)
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

        appointmentViewModel.cancelAppointment.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.status?.toInt() == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(), response.data.message)
                    } else {
                        if (response.data?.success!!) {
                            showToast(response.data.message)
                            findNavController().navigate(R.id.action_appointmentDetailsFragment_to_appointmentFragment)
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

        appointmentViewModel.drClinicTimeSlots.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.status == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(), response.data.message)
                    } else {
                        if (response.data?.data?.isEmpty()!!) {
                            showToast("No Slots Available for Selected Date")
                            consultationTimeView.text = ""
                        } else {
                            timeSlotsList.clear()
                            timeSlotsList.addAll(response.data.data)
                            showTimeSlotsDialog()
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

        appointmentViewModel.rescheduleAppointment.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    rescheduleDialog.dismiss()
                    if (response.data?.status == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(), response.data.message)
                    } else {
                        if (response.data?.code == 200) {
                            showToast(response.data.message)
                            findNavController().navigate(R.id.action_appointmentDetailsFragment_to_appointmentFragment)
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

        appointmentViewModel.reviewAppointment.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.status?.toInt() == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(), response.data.message)
                    } else {
                        if (response.data?.success!!) {
                            findNavController().navigate(R.id.action_appointmentDetailsFragment_to_homeFragment)
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
        appointmentViewModel.doctorAppointmentPrescription.observe(viewLifecycleOwner) { response ->
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
                                setPrescriptionData(prescriptionData)

                            } else {
                                showToast(response.data?.message.toString())
                            }
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


        appointmentViewModel.generateVideoToken.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.status == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(), response.data.message)
                    } else {
                        if (response.data?.code != null && response.data.code == 200) {
                            response.data.message.let { showToast(it) }
                            appLoader.dismiss()
                            session.callStatus = ""
                            val jsonObject = JsonObject().apply {
                                addProperty("type", "patient")
                                addProperty("name", appointmentData.doctorname)
                                addProperty("status", "calling")
                                addProperty("appointmentId", appointmentId)
                            }
                            appointmentViewModel.callNotify(jsonObject)

                            startActivity(
                                Intent(requireActivity(), VideoActivity::class.java)
                                    .putExtra("videoToken", response.data.data.patients_token)
                                    .putExtra("name", appointmentData.doctorname)
                                    .putExtra("appointId", appointmentData._id)
                            )
                        } else {
                            response.data?.message?.let { showToast(it) }
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
            appointmentViewModel.callNotify.observe(viewLifecycleOwner) { }
        }

        appointmentViewModel.downLoadAppointmentSlip.observe(viewLifecycleOwner){response->
            when(response){
                is Resource.Success->{
                    if(response.data?.code == 200){
                        downloadAppointmentSlip(response.data.url)
                    }else{
                        downloadDialog.dismiss()
                    }
                }
                is Resource.Loading->{}
                is Resource.Error->{}
            }
        }

    }

    private fun setPrescriptionData(prescriptionData: List<PrescriptionData>) {

        var scan = false
        var pdf = false

        prescriptionData.forEach {
            if (it.type == "SCAN") {
                val prescriptionImage = it.image
                val bundle = Bundle().apply {
                    putString("prescriptionImage", prescriptionImage)
                }
                if (!pdf) {
                    findNavController().navigate(
                        R.id.imagePrescriptionViewFragment2,
                        bundle
                    )
                    pdf = true

                }
            } else {
//                                    medicine = it.medicine
                val bundle = Bundle().apply {
                    putString("from", "patient")
                    putString("prescriptionAdded", "yes")
                    putString("appointmentId", appointmentId)
                    putString(
                        "appointmentTime",
                        binding.appointmentDateTime.text.toString()
                    )
                    putString(
                        "consultationFee",
                        appointmentData.consultation_fee.toString()
                    )
                    putString(
                        "clinicName",
                        appointmentData.clinic_id.clinicName
                    )
                    putString(
                        "clinicLocation",
                        appointmentData.clinic_id.location
                    )
                }
                if (!scan) {
                    findNavController().navigate(
                        R.id.addPrescriptionWithMedicineFragment2,
                        bundle
                    )
                    scan = true
                }
            }
        }
    }

    private fun showTimeSlotsDialog() {
        val timeSlotsDialog = Dialog(requireActivity())
        timeSlotsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        timeSlotsDialog.setCancelable(false)
        timeSlotsDialog.setContentView(R.layout.time_slots_dialogue)
        timeSlotsDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        timeSlotsDialog.findViewById<View>(R.id.submitBtn).setOnClickListener {
            if (timeSlotSelected) {
                timeSlotsDialog.dismiss()
            } else {
                showToast("Please Select Any One Time Slot")
            }
        }

        timeSlotsDialog.findViewById<View>(R.id.dialogCancelBtn).setOnClickListener {
            consultationTimeView.text = ""
        }

        val timeSlotsRv = timeSlotsDialog.findViewById<RecyclerView>(R.id.timeSlotsRv)
        bookTimeAdapter.differ.submitList(timeSlotsList)
        timeSlotsRv.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = bookTimeAdapter
        }
        timeSlotsDialog.show()

        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val displayWidth = displayMetrics.widthPixels
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(timeSlotsDialog.window!!.attributes)
        val dialogWindowWidth = (displayWidth * 0.9f).toInt()
        layoutParams.width = dialogWindowWidth
        timeSlotsDialog.window!!.attributes = layoutParams
    }


    private fun setAppointmentsDetails(data: AppointmentData) {
        with(binding) {
            doctorName.text = data.doctor_id.doctorName
            doctorSpecialistIn.text = data.doctor_id.specialization
            doctorDegree.text = data.doctor_id.qualification
            Glide.with(requireContext()).applyDefaultRequestOptions(doctorRequestOption())
                .load(data.doctor_id.profile_picture).into(doctorImage)

            patientName.text = data.patientname
            appointmentDateTime.text = data.patientname
            clinicDetails.text = data.clinic_id.location
            contactDetails.text = session.loginPhone
            ageDetails.text = data.age
            gender.text = data.gender

            consultationFees.text = "₹${data.doctor_id.consultationfee}"

            if (data.mode_of_payment == "Cash") {
                totalAmt.text = "₹${data.doctor_id.consultationfee}"
            } else {
                totalAmt.text = "₹${data.payment_id.amount}"
            }

            val parsedDate = isoFormat.parse(data.date)
            val formattedDate = showDateFormat.format(parsedDate)
            val formattedDay = showDayFormat.format(parsedDate)
            appointmentDateTime.text =
                "$formattedDay  $formattedDate (${data.room_time_slot_id.timeSlotData.start_time} - ${data.room_time_slot_id.timeSlotData.end_time})"

            val discountAmount = data.doctor_id.consultationfee.toInt() - data.consultation_fee

            if (discountAmount >= 1) {
                binding.discountAmt.text = "-₹${discountAmount}"
            } else {
                binding.discountAmt.isVisible = false
                binding.discountLbl.isVisible = false
            }
            totalAmt.text = "₹${data.consultation_fee}"
//            binding.videoCallIcon.isVisible = data.appointment_type.lowercase() == "online"
            binding.joinVideoCall.isVisible = data.appointment_type.lowercase() == "online"
            if (data.payment_status) {
                binding.paymentStatus.text = "Successful"
            } else {
                binding.paymentStatus.text = "Pending"
            }

            if (data.rating != null) {
                binding.ratingExperienceBar.rating = data.rating.rating.toString().toFloat()
                binding.experienceDesc.setText(data.rating.review)
                binding.ratingExperienceBar.isFocusable = false
                binding.experienceDesc.isEnabled = false
                binding.saveBtn.isVisible = false
            }
        }

        binding.viewPrescription.isVisible = data.prescription != "false"
    }

    private fun setOnAdapterClickListener() {
        bookTimeAdapter.setOnTimeSelectedListener { _, position ->
            timeSlotsList.forEach {
                if (it.timeSlotData.isSelected) it.timeSlotData.isSelected = false
            }
            timeSlotsList[position].timeSlotData.isSelected = true
            timeSlotsListPosition = position
            consultationTimeView.text =
                "${timeSlotsList[position].timeSlotData.start_time} - ${timeSlotsList[position].timeSlotData.end_time} Am"
            bookTimeAdapter.differ.submitList(timeSlotsList)
            timeSlotSelected = true
            bookTimeAdapter.notifyDataSetChanged()
        }
    }

    private fun setOnClickListener() {
        binding.backArrow.setOnClickListener(this@AppointmentDetailsFragment)
        binding.saveBtn.setOnClickListener(this@AppointmentDetailsFragment)
        binding.reschedule.setOnClickListener(this@AppointmentDetailsFragment)
        binding.cancelAppointment.setOnClickListener(this@AppointmentDetailsFragment)
        binding.viewPrescription.setOnClickListener(this@AppointmentDetailsFragment)
//        binding.videoCallIcon.setOnClickListener(this@AppointmentDetailsFragment)
        binding.joinVideoCall.setOnClickListener(this@AppointmentDetailsFragment)
        binding.downloadAppointmentSlip.setOnClickListener(this@AppointmentDetailsFragment)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.viewPrescription -> {
                appointmentViewModel.getAppointmentPrescriptionDetails(appointmentId)

            }
            R.id.backArrow -> {
                findNavController().popBackStack()
            }
            R.id.saveBtn -> {
                saveReviewRating()
            }
            R.id.reschedule -> {
                showRescheduleDialog()
            }
            R.id.cancelAppointment -> {
                showCancelWarning()
            }
            R.id.downloadAppointmentSlip -> {
                downloadDialog.show()
                appointmentViewModel.downloadAppointmentSlip(appointmentId)
                requireActivity().registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
            }


            R.id.joinVideoCall -> {
               joinVideoCall()
            }
        }
    }

    private fun joinVideoCall() {

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
            addProperty("type", "patient")
        }
        appointmentViewModel.generateVideoToken(jsonObject)
    }


    private fun downloadAppointmentSlip(url: String) {
        val fileName = url.substring(url.lastIndexOf('/') + 1)
        val downloadManager: DownloadManager? = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,fileName)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
        downloadId =  downloadManager!!.enqueue(request)
    }

    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadId == id) {
                downloadDialog.dismiss()
                Toast.makeText(requireContext(), "Appointment Slip Downloaded....", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun showVideoCallAlertPopUp(msg: String) {
        AlertDialog.Builder(context)
            .setMessage(msg)
            .setPositiveButton("yes", null)
            .setNegativeButton("no", null)
            .show()
    }

    private fun showVideoCallNotStartAlertPopUp(msg: String) {
        AlertDialog.Builder(context)
            .setMessage(msg)
            .setPositiveButton("ok", null)
            .show()
    }

    private fun saveReviewRating() {

        val review = binding.experienceDesc.text.toString()

        if (rating > 0) {
            val jsonObject = JsonObject()
            jsonObject.addProperty("doctorId", appointmentData.doctor_id._id)
            jsonObject.addProperty("clinicId", appointmentData.clinic_id._id)
            jsonObject.addProperty("appointmentId", appointmentId)
            jsonObject.addProperty("userId", session.patientId)
            jsonObject.addProperty("rating", rating)
            jsonObject.addProperty("review", review)
            appointmentViewModel.reviewAppointment(jsonObject)
        } else {
            showToast("please rate your experience to save")
        }
    }

    private fun showRescheduleDialog() {
        rescheduleDialog = Dialog(requireActivity())
        rescheduleDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        rescheduleDialog.setCancelable(false)
        rescheduleDialog.setContentView(R.layout.dialogue_reschedule_selection)
        rescheduleDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val offlineText = rescheduleDialog.findViewById<TextView>(R.id.offlineTxt)
        val onlineText = rescheduleDialog.findViewById<TextView>(R.id.onlineTxt)
        consultationDateView = rescheduleDialog.findViewById(R.id.consultationDate)
        consultationTimeView = rescheduleDialog.findViewById(R.id.consultationTime)

        onlineText.setOnClickListener {
            onlineText.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.radius_box_selected)
            onlineText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            offlineText.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.radius_box)
            offlineText.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimaryLight
                )
            )
            selectedAppointmentType = "Online"
        }

        offlineText.setOnClickListener {

            offlineText.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.radius_box_selected)
            offlineText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            onlineText.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.radius_box)
            onlineText.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimaryLight
                )
            )
            selectedAppointmentType = "Offline"
        }


        consultationDateView.setOnClickListener {
            getDate()
        }


        consultationTimeView.setOnClickListener {
            if (consultationDateView.text.toString().isNotEmpty()) {
                showTimeSlotsDialog()
            }
        }

        rescheduleDialog.findViewById<View>(R.id.submitBtn).setOnClickListener {
            when {
                consultationDateView.text.toString().isEmpty() -> {
                    showToast("please select Appointment Date")
                }

                consultationTimeView.text.toString().isEmpty() -> {
                    showToast("please select Appointment Time")
                }

                else -> {
                    val jsonObject = JsonObject()
                    jsonObject.addProperty("date",
                        getDateFormatted(consultationDate.toString(),
                            DATE_PATTERN,
                            DATE_FULL_PATTERN))
                    jsonObject.addProperty("by", "patient")
                    jsonObject.addProperty("time", timeSlotsList[timeSlotsListPosition]._id)
                    appointmentViewModel.rescheduleAppointment(appointmentId, jsonObject)
                }
            }
        }

        rescheduleDialog.findViewById<View>(R.id.cancelBtn).setOnClickListener {
            rescheduleDialog.dismiss()
        }


        rescheduleDialog.show()
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val displayWidth = displayMetrics.widthPixels
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(rescheduleDialog.window!!.attributes)
        val dialogWindowWidth = (displayWidth * 0.9f).toInt()
        layoutParams.width = dialogWindowWidth
        rescheduleDialog.window!!.attributes = layoutParams
    }

    private fun getDate() {
        val constraintsBuilder = CalendarConstraints.Builder()
        val dateValidator: CalendarConstraints.DateValidator = DateValidatorPointForward.now()
        constraintsBuilder.setValidator(dateValidator)

        val datePicker: MaterialDatePicker<Long> = MaterialDatePicker
            .Builder
            .datePicker()
            .setCalendarConstraints(constraintsBuilder.build())
            .setTheme(R.style.MaterialCalendarTheme)
            .setSelection(Date().time)
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            .setTitleText("Select date of birth")
            .build()
        datePicker.show(childFragmentManager, "DATE_PICKER")

        datePicker.addOnPositiveButtonClickListener {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val selectConsultationDateFormat =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val showDateSelectedFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
            consultationDate = sdf.format(it)
            selectedConsultationDate = selectConsultationDateFormat.format(it)
            val stringDate: Date = sdf.parse(this.consultationDate)
            val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
            val consultationDay = dayFormat.format(stringDate)


            val jsonObject = JsonObject()
            jsonObject.addProperty("clinicId", appointmentData.clinic_id._id)
            jsonObject.addProperty("doctorId", appointmentData.doctor_id._id)
            jsonObject.addProperty("date", selectedConsultationDate)
            jsonObject.addProperty("day", consultationDay)
            appointmentViewModel.getRoomSlotDetailsByDrAndClinicId(
                appointmentType = selectedAppointmentType,
                jsonObject
            )
            consultationDateView.text = showDateSelectedFormat.format(stringDate).toString()
        }
    }


    private fun showCancelWarning() {
        AlertDialog.Builder(context)
            .setTitle("Cancel Appointment")
            .setMessage("Are you sure you want to Cancel Appointment?")
            .setPositiveButton(
                "yes"
            ) { _, _ ->
                cancelAppointment()
            }
            .setNegativeButton("no", null)
            .show()
    }

    private fun cancelAppointment() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("by", "patient")
        appointmentViewModel.cancelAppointment(appointmentId = appointmentId, jsonObject)
    }
    private fun showProgressDialog() {
        downloadDialog = Dialog(requireActivity())
        downloadDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        downloadDialog.setCancelable(true)
        downloadDialog.setContentView(R.layout.dialogue_loading)
        downloadDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val displayWidth = displayMetrics.widthPixels
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(downloadDialog.window!!.attributes)
        val dialogWindowWidth = (displayWidth * 0.8f).toInt()
        layoutParams.width = dialogWindowWidth
        downloadDialog.window!!.attributes = layoutParams
    }

    override fun onResume() {
        super.onResume()
        setObserver()
    }
}