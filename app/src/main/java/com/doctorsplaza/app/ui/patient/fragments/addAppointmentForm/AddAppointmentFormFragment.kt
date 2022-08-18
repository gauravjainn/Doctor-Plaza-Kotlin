package com.doctorsplaza.app.ui.patient.fragments.addAppointmentForm

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentAddAppoinmnetFormBinding
import com.doctorsplaza.app.ui.patient.fragments.addAppointmentForm.model.ClinicsData
import com.doctorsplaza.app.ui.patient.fragments.addAppointmentForm.model.DoctorsData
import com.doctorsplaza.app.ui.patient.fragments.addAppointmentForm.model.RoomTimeSlotsData
import com.doctorsplaza.app.ui.patient.fragments.appointments.AppointmentViewModel
import com.doctorsplaza.app.ui.patient.fragments.appointments.adapter.PastAppointmentsAdapter
import com.doctorsplaza.app.ui.patient.fragments.bookAppointment.adapter.BookTimeAdapter
import com.doctorsplaza.app.ui.patient.fragments.bookAppointment.model.CouponData
import com.doctorsplaza.app.ui.patient.fragments.home.adapter.UpcomingAppointmentsAdapter
import com.doctorsplaza.app.ui.patient.payment.PaymentActivity
import com.doctorsplaza.app.utils.*
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CalendarConstraints.DateValidator
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


@AndroidEntryPoint
class AddAppointmentFormFragment : Fragment(R.layout.fragment_add_appoinmnet_form),
    View.OnClickListener {


    private var consultationDate: String? = ""

    private var selectedConsultationDate: String? = ""

    private var doctorData: DoctorsData? = null

    private var specializationData: String = ""

    private var clinicData: ClinicsData? = null

    private var selectedDoctorId: String = ""

    private var selectedClinicId: String = ""

    private lateinit var binding: FragmentAddAppoinmnetFormBinding

    @Inject
    lateinit var session: SessionManager

    private val appointmentViewModel: AppointmentViewModel by viewModels()

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader

    @Inject
    lateinit var upcomingAppointmentsAdapter: UpcomingAppointmentsAdapter

    @Inject
    lateinit var pastAppointmentsAdapter: PastAppointmentsAdapter

    @Inject
    lateinit var bookTimeAdapter: BookTimeAdapter

    private var selectedGender = ""

    private var selectedBookType = ""

    private var selectedAppointmentType = ""

    private var selectedPaymentType = ""

    private var timeSlotsList: MutableList<RoomTimeSlotsData> = ArrayList()

    private var timeSlotsListPosition = 0

    private var consultationFee = 0

    private var jsonObjectOnline: JsonObject = JsonObject()

    private var appointmentType: String = "Offline"

    private var timeSlotSelected = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_add_appoinmnet_form, container, false)
            binding = FragmentAddAppoinmnetFormBinding.bind(currentView!!)
            init()
            setSpinners()
            setObserver()
            setOnClickListener()
            setOnAdapterClickListener()
        }
        return currentView!!
    }

    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())
        setSelfGuestData("self")
        binding.loader.isVisible = true
        appointmentViewModel.getAppointmentClinics()
    }

    private fun setSpinners() {
        val appointmentTypeArray =
            requireContext().resources.getStringArray(R.array.appointment_type)
        val paymentTypeArray = requireContext().resources.getStringArray(R.array.payment_type)

        val appointmentAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.appointment_type,
            R.layout.spinner_text
        )
        appointmentAdapter.setDropDownViewResource(R.layout.spinner_text)

        binding.selectAppointmentSpinner.adapter = appointmentAdapter
        binding.selectAppointmentSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    selectedAppointmentType = appointmentTypeArray[position]
                    binding.consultationDate.text = ""
                    binding.consultationTime.text = ""
                    if (selectedAppointmentType == appointmentTypeArray[0]) {
                        appointmentType = "Offline"
                        selectedPaymentType = "Cash"
                        binding.paymentMode.text = "In Clinic"

                    } else if (selectedAppointmentType == appointmentTypeArray[1]) {
                        appointmentType = "Online"
                        selectedPaymentType = "Online"
                        binding.paymentMode.text = "Online"
                    }
                }
            }

        val paymentTypeAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.payment_type,
            R.layout.spinner_text
        )

        paymentTypeAdapter.setDropDownViewResource(R.layout.spinner_text)
        binding.selectPaymentModeSpinner.adapter = paymentTypeAdapter
        binding.selectPaymentModeSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    selectedPaymentType = paymentTypeArray[position]
                }
            }
    }

    private fun setObserver() {

        appointmentViewModel.appointmentClinics.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.errorMsg.isVisible = false
                    if (response.data?.status == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(), response.data.message)
                    } else {
                        if (response.data!!.success) {
                            if (response.data.data.isEmpty()) {
                                appLoader.dismiss()
                            } else {
                                setClinicSpinner(response.data.data)
                            }
                        }
                    }

                }
                is Resource.Loading -> {
                    appLoader.show()
                    binding.loader.isVisible = true
                }
                is Resource.Error -> {
                    binding.loader.isVisible = true
                    binding.errorMsg.isVisible = true
                    appLoader.dismiss()
                }
            }
        }

        appointmentViewModel.clinicSpecialization.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.errorMsg.isVisible = false
                    setSpecializationSpinner(response.data)
                }
                is Resource.Loading -> {}
                is Resource.Error -> {
                    binding.loader.isVisible = true
                    binding.errorMsg.isVisible = true
                    appLoader.dismiss()
                }
            }
        }

        appointmentViewModel.clinicDoctors.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    binding.loader.isVisible = false
                    binding.errorMsg.isVisible = false
                    if (response.data?.data?.isEmpty()!!) {
                        showToast("No Doctors Found")
                    } else {
                        setDoctorsSpinner(response.data.data)
                    }
                }
                is Resource.Loading -> {
                }
                is Resource.Error -> {
                    binding.loader.isVisible = true
                    binding.errorMsg.isVisible = true
                    appLoader.dismiss()
                }
            }
        }

        appointmentViewModel.drClinicTimeSlots.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.data?.isEmpty()!!) {
                        showToast("No Slots Available for Selected Date")
                        binding.consultationDate.text = ""
                    } else {
                        timeSlotsList.clear()
                        timeSlotsList.addAll(response.data.data)
                        showTimeSlotsDialog()
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

        appointmentViewModel.bookAppointment.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.success!!) {
                        showToast(response.data.message)
                        findNavController().navigate(R.id.action_addAppointmentFormFragment_to_appointmentFragment)
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

        appointmentViewModel.applyCoupon.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    showToast(response.data?.message.toString())
                    if (response.data?.code == 200) {
                        setCouponApplied(response.data.data)
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


    private fun setCouponApplied(data: CouponData) {
        with(binding) {
            applyCouponBtn.text = "Applied"
            applyCouponBtn.isEnabled = false
            couponEdt.isEnabled = false

            totalLbl.isVisible = true
            total.isVisible = true

            consultationFees.text = "₹${data.original_price}"
            total.text = "₹${data.final_price}"
            consultationFee = data.final_price
        }
    }

    private fun setClinicSpinner(data: List<ClinicsData>) {
        val clinicAdapter = ArrayAdapter(requireContext(), R.layout.spinner_text, data)
        clinicAdapter.setDropDownViewResource(R.layout.spinner_text)
        binding.clinicSpinner.adapter = clinicAdapter
        binding.clinicSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                clinicData = binding.clinicSpinner.selectedItem as ClinicsData
                selectedClinicId = clinicData!!._id
                val jsonObject = JsonObject()
                jsonObject.addProperty("clinicId", clinicData!!._id)
                appointmentViewModel.getSpecializationsByClinic(jsonObject)
            }
        }
    }

    private fun setSpecializationSpinner(data: JsonArray?) {
        val specializationList = ArrayList<String>()
        for (i in 0 until data!!.size()) {
            val specializationName = data[i].toString().substring(1, data[i].toString().length - 1)
            specializationList.add(specializationName)
        }

        val specializationAdapter =
            ArrayAdapter(requireContext(), R.layout.spinner_text, specializationList)
        specializationAdapter.setDropDownViewResource(R.layout.spinner_text)
        binding.specializationSpinner.adapter = specializationAdapter
        binding.specializationSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    specializationData = binding.specializationSpinner.selectedItem as String
                    val jsonObject = JsonObject()
                    jsonObject.addProperty("clinicId", selectedClinicId)
                    jsonObject.addProperty("dept", specializationData)
                    appointmentViewModel.getDoctorListByClinic(jsonObject)
                }
            }
    }

    private fun setDoctorsSpinner(data: List<DoctorsData>) {
        val clinicAdapter = ArrayAdapter(requireContext(), R.layout.spinner_text, data)
        clinicAdapter.setDropDownViewResource(R.layout.spinner_text)
        binding.doctorSpinner.adapter = clinicAdapter
        binding.doctorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                doctorData = binding.doctorSpinner.selectedItem as DoctorsData
                binding.consultationFees.text = "₹${doctorData!!.consultationfee}"
                selectedDoctorId = doctorData!!._id
                consultationFee = doctorData!!.consultationfee.toInt()

            }
        }
    }

    private fun getDate() {
        val constraintsBuilder = CalendarConstraints.Builder()
        val dateValidator: DateValidator = DateValidatorPointForward.now()
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
            val stringDate: Date = sdf.parse(consultationDate)
            val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
            val consultationDay = dayFormat.format(stringDate)
            binding.consultationDate.text = showDateSelectedFormat.format(stringDate)

            val jsonObject = JsonObject()
            jsonObject.addProperty("clinicId", selectedClinicId)
            jsonObject.addProperty("doctorId", selectedDoctorId)
            jsonObject.addProperty("date", selectedConsultationDate)
            jsonObject.addProperty("day", consultationDay)
            appointmentViewModel.getRoomSlotDetailsByDrAndClinicId(
                appointmentType = appointmentType,
                jsonObject
            )
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

        timeSlotsDialog.findViewById<View>(R.id.cancelBtn).setOnClickListener {
            binding.consultationTime.text = ""
            timeSlotsDialog.dismiss()
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

    private fun setSelfGuestData(userType: String) {
        with(binding) {

            if (userType == "self") {
                patientId.setText(session.loginName)
                phoneNumber.setText(session.loginPhone)
                age.setText(session.loginAge)
                if (session.loginGender == "Male") {
                    maleRadioBtn.isChecked = true
                    femaleRadioBtn.isChecked = false
                    selectedGender = "Male"
                } else {
                    maleRadioBtn.isChecked = false
                    femaleRadioBtn.isChecked = true
                    selectedGender = "Female"
                }
            } else {
                patientId.setText("")
                phoneNumber.setText("")
                age.setText("")

            }
        }
    }

    private fun setOnClickListener() {
        with(binding) {
            bookAppointmentBtn.setOnClickListener(this@AddAppointmentFormFragment)
            backArrow.setOnClickListener(this@AddAppointmentFormFragment)
            cancelBtn.setOnClickListener(this@AddAppointmentFormFragment)
            selfRadioBg.setOnClickListener(this@AddAppointmentFormFragment)
            guestRadioBg.setOnClickListener(this@AddAppointmentFormFragment)

            selfRadioBtn.setOnClickListener(this@AddAppointmentFormFragment)
            guestRadioBtn.setOnClickListener(this@AddAppointmentFormFragment)

            maleRadioBg.setOnClickListener(this@AddAppointmentFormFragment)
            femaleRadioBg.setOnClickListener(this@AddAppointmentFormFragment)

            maleRadioBtn.setOnClickListener(this@AddAppointmentFormFragment)
            femaleRadioBtn.setOnClickListener(this@AddAppointmentFormFragment)
            consultationDate.setOnClickListener(this@AddAppointmentFormFragment)

            bookAppointmentBtn.setOnClickListener(this@AddAppointmentFormFragment)
            applyCouponBtn.setOnClickListener(this@AddAppointmentFormFragment)

            consultationTime.setOnClickListener(this@AddAppointmentFormFragment)
        }
    }


    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun setOnAdapterClickListener() {
        bookTimeAdapter.setOnTimeSelectedListener { _, position ->
            timeSlotsList.forEach { it ->
                if (it.timeSlotData.isSelected) it.timeSlotData.isSelected = false
            }
            timeSlotsList[position].timeSlotData.isSelected = true
            timeSlotsListPosition = position
            binding.consultationTime.text =
                "${timeSlotsList[position].timeSlotData.start_time} - ${timeSlotsList[position].timeSlotData.end_time} Am"
            bookTimeAdapter.differ.submitList(timeSlotsList)
            timeSlotSelected = true
            bookTimeAdapter.notifyDataSetChanged()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backArrow -> {
                findNavController().popBackStack()
            }

            R.id.cancelBtn -> {
                findNavController().popBackStack()
            }
            R.id.selfRadioBg -> {
                binding.selfRadioBtn.isChecked = true
                binding.guestRadioBtn.isChecked = false
                setSelfGuestData("self")
                selectedBookType = "user"
            }
            R.id.guestRadioBg -> {
                binding.selfRadioBtn.isChecked = false
                binding.guestRadioBtn.isChecked = true
                setSelfGuestData("guest")
                selectedBookType = "guest"
            }
            R.id.selfRadioBtn -> {
                binding.selfRadioBtn.isChecked = true
                binding.guestRadioBtn.isChecked = false
                setSelfGuestData("self")
                selectedBookType = "user"
            }
            R.id.guestRadioBtn -> {
                binding.selfRadioBtn.isChecked = false
                binding.guestRadioBtn.isChecked = true
                setSelfGuestData("guest")
                selectedBookType = "guest"
            }
            R.id.maleRadioBg -> {
                binding.maleRadioBtn.isChecked = true
                binding.femaleRadioBtn.isChecked = false
                selectedGender = "Male"
            }
            R.id.femaleRadioBg -> {
                binding.maleRadioBtn.isChecked = false
                binding.femaleRadioBtn.isChecked = true
                selectedGender = "Female"
            }
            R.id.maleRadioBtn -> {
                binding.maleRadioBtn.isChecked = true
                binding.femaleRadioBtn.isChecked = false
                selectedGender = "Male"
            }
            R.id.femaleRadioBtn -> {
                binding.maleRadioBtn.isChecked = false
                binding.femaleRadioBtn.isChecked = true
                selectedGender = "Female"
            }

            R.id.consultationDate -> {
                getDate()
            }

            R.id.bookAppointmentBtn -> {
                validateAndBookAppointment()
            }

            R.id.applyCouponBtn -> {
                applyCoupon()
            }
            R.id.consultationTime -> {
                showTimeSlotsDialog()
            }
        }
    }

    private fun applyCoupon() {
        val couponCode = binding.couponEdt.text.toString()
        if (couponCode.isEmpty()) {
            showToast("please enter a valid coupon code")
            return
        }
        val jsonObject = JsonObject().apply {
            addProperty("code", couponCode)
            addProperty("patientId", session.patientId)
            addProperty("doctorId", selectedDoctorId)
        }
        appointmentViewModel.applyCoupon(jsonObject)
    }

    private fun validateAndBookAppointment() {
        val patientName = binding.patientId.text.toString().trim()
        val patientAge = binding.age.text.toString().trim()
        val patientPhone = binding.phoneNumber.text.toString().trim()
        val description = binding.describeYourProblem.text.toString().trim()
        val consultationDate = binding.consultationDate.text.toString().trim()
        val consultationTime = binding.consultationTime.text.toString().trim()
        val showReports = binding.showReports.isChecked
        if (patientName.isEmpty()) {
            showToast("Please Enter a Valid Patient Name")
        } else if (patientPhone.isEmpty() || patientPhone.length < 10) {
            showToast("Please Enter a Valid Phone Number")
        } else if (patientAge.isEmpty()) {
            showToast("Please Enter a Valid Age")
        } else if (description.isEmpty()) {
            showToast("Please Enter a Describe Your Problem")
        } else if (consultationDate.isEmpty()) {
            showToast("Please Select Consultation Date")
        } else if (consultationTime.isEmpty()) {
            showToast("Please Select Consultation Time")
        } else {
            val jsonObject = JsonObject()
            jsonObject.addProperty("age", patientAge)
            jsonObject.addProperty("appointment_type", selectedAppointmentType)
            jsonObject.addProperty("booked_as", selectedBookType)
            jsonObject.addProperty("clinicname", clinicData!!.clinicName)
            jsonObject.addProperty("consultation_fee", consultationFee)

            jsonObject.addProperty("date", selectedConsultationDate)
            jsonObject.addProperty("departmentname", specializationData)
            jsonObject.addProperty("doctor_id", doctorData?._id)
            jsonObject.addProperty("doctorname", doctorData?.doctorName)
            jsonObject.addProperty("gender", selectedGender)
            jsonObject.addProperty("mobile", patientPhone)
            jsonObject.addProperty("mode_of_payment", selectedPaymentType)
            jsonObject.addProperty("patient_id", session.patientId)
            jsonObject.addProperty("patientname", patientName)
            jsonObject.addProperty("problem", description)
            jsonObject.addProperty("slotIdArray", timeSlotsList[timeSlotsListPosition]._id)
            jsonObject.addProperty("status", "pending")
            jsonObject.addProperty("user_id", session.patientId)
            jsonObject.addProperty("user_id", session.patientId)
            jsonObject.addProperty("showReports", showReports)
            if (selectedPaymentType == "Online") {
                jsonObjectOnline = jsonObject
                createOrderId()
            } else if (selectedPaymentType == "Cash") {
                jsonObject.addProperty("payment_status", false)
                jsonObject.addProperty("payment_id", session.patientId)
                appointmentViewModel.bookAppointment(jsonObject)
            }
        }
    }

    private fun createOrderId() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("amount", "1")
        appointmentViewModel.createOrderId(jsonObject)
        appointmentViewModel.createOrderId.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> {
                    appLoader.show()
                }
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.order_id!!.isNotEmpty()) {
                        val intent = Intent(requireContext(), PaymentActivity::class.java)
                        intent.putExtra("consultationFee", consultationFee.toString())
                        intent.putExtra("orderId", response.data.order_id)
                        launchPaymentActivity.launch(intent)
                    } else {
                        showToast(response.data.message)
                    }
                }
                is Resource.Error -> {
                    appLoader.dismiss()
                }
            }
        }
    }

    private val launchPaymentActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent = result.data!!
                if (data.hasExtra("payment_id")) {
                    val orderId: String = data.getStringExtra("order_id").toString()
                    val razorpayPaymentId: String = data.getStringExtra("payment_id").toString()
                    jsonObjectOnline.addProperty("payment_status", "true")
                    verifyPayment(orderId, razorpayPaymentId)
                }
            }
        }

    private fun verifyPayment(order_id: String, razorpay_payment_id: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("order_id", order_id)
        jsonObject.addProperty("razorpay_payment_id", razorpay_payment_id)
        jsonObject.addProperty(
            "razorpay_signature",
            "b28b508247699b191588ce995628591174e4b5297b78eb3122c809d8d1f271e0"
        )
        appointmentViewModel.verifyPayment(jsonObject)
        appointmentViewModel.verifyPayment.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> {
                    appLoader.show()
                }
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.order_id!!.isNotEmpty()) {
                        savePaymentInfo(order_id, razorpay_payment_id)
                    } else {
                        showToast(response.data.message)
                    }
                }
                is Resource.Error -> {
                    appLoader.dismiss()

                }
            }
        }
    }

    private fun savePaymentInfo(order_id: String, razorpay_payment_id: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("amount", consultationFee)
        jsonObject.addProperty("doctor_id", doctorData?._id)
        jsonObject.addProperty("order_id", order_id)
        jsonObject.addProperty("payment_id", razorpay_payment_id)
        jsonObject.addProperty("payment_status", "success")
        jsonObject.addProperty("user_id", session.patientId)

        appointmentViewModel.savePayment(jsonObject)
        appointmentViewModel.savePayment.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> {
                    appLoader.show()
                }
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.success!!) {
                        jsonObjectOnline.addProperty("payment_id", response.data.data._id)
                        bookingAppointment(jsonObjectOnline)
                    } else {
                        showToast(response.data.message)
                    }
                }
                is Resource.Error -> {
                    appLoader.dismiss()

                }
            }
        }
    }


    private fun bookingAppointment(jsonObject: JsonObject) {
        appointmentViewModel.bookingAppointment(jsonObject)
        appointmentViewModel.bookingAppointment.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> {
                    appLoader.show()
                }
                is Resource.Success -> {
                    appLoader.dismiss()

                    if (response.data?.success!!) {
                        findNavController().navigate(R.id.action_addAppointmentFormFragment_to_appointmentFragment)
                    } else {
                        showToast(response.data.message)
                    }
                }
                is Resource.Error -> {
                    appLoader.dismiss()
                    showToast("something went wrong")

                }
            }
        }

    }
}