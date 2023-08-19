package com.doctorsplaza.app.ui.patient.fragments.bookAppointment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentCheckOutBookingAppointmentBinding
import com.doctorsplaza.app.ui.patient.fragments.appointments.AppointmentViewModel
import com.doctorsplaza.app.ui.patient.fragments.bookAppointment.model.CouponData
import com.doctorsplaza.app.ui.patient.fragments.doctorDetails.model.DoctorDetailsData
import com.doctorsplaza.app.ui.patient.payment.PaymentActivity
import com.doctorsplaza.app.utils.*
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CheckOutBookingAppointmentFragment :
    Fragment(R.layout.fragment_check_out_booking_appointment), View.OnClickListener {

    private var doctorConsultationFee: Int = 0
    private var selectedBookType: String = "user"
    private var selectedGender: String = ""
    private var showDateParsed: Date? = null
    private var selectedPaymentType: String = "Cash"

    private var doctorId: String = ""
    private var clinicAddress: String = ""
    private var clinicName: String = ""
    private var clinicContact: String = ""
    private var clinicId: String = ""

    private var consultationDate: String = ""
    private var consultationDay: String = ""
    private var consultationTimeId: String = ""
    private var consultationStartTime: String = ""
    private var consultationEndTime: String = ""
    private var jsonObjectOnline: JsonObject = JsonObject()

    private lateinit var binding: FragmentCheckOutBookingAppointmentBinding

    private var dateFormat: SimpleDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    private val selectedDateFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

    private val selectConsultationDateFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader

    lateinit var doctorDetails: DoctorDetailsData

    private val appointmentViewModel: AppointmentViewModel by viewModels()

    private var appointmentType = ""

    private var bookType = MutableLiveData<String>()

    private var consultationFee = 0
    private var showReports = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView =
                inflater.inflate(R.layout.fragment_check_out_booking_appointment, container, false)
            binding = FragmentCheckOutBookingAppointmentBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())


        doctorId = arguments?.getString("doctorId").toString()
        clinicId = arguments?.getString("clinicId").toString()
        clinicName = arguments?.getString("clinicName").toString()
        clinicAddress = arguments?.getString("clinicAddress").toString()
        clinicContact = arguments?.getString("clinicContact").toString()
        consultationDate = arguments?.getString("consultationDate").toString()
        consultationDay = arguments?.getString("consultationDay").toString()
        consultationTimeId = arguments?.getString("consultationTimeId").toString()
        consultationStartTime = arguments?.getString("consultationStartTime").toString()
        consultationEndTime = arguments?.getString("consultationEndTime").toString()
        appointmentType = arguments?.getString("appointmentType").toString()


        if (appointmentType.lowercase() == "online") {
            binding.payAtClinicCheckBox.isVisible = false
            binding.payOnlineCheckBox.isChecked = true
            binding.payOnlineCheckBox.isEnabled = true
            selectedPaymentType = "Online"
        } else {
            binding.payAtClinicCheckBox.isChecked = true
            binding.payAtClinicCheckBox.isVisible = true
            binding.payOnlineCheckBox.isVisible = false
            selectedPaymentType = "Cash"
        }
        bookType.postValue("self")
        appointmentViewModel.getDoctor(doctorId)
    }

    private fun setPatientData(from: String) {
        with(binding) {
            if (from == "user") {
                patientName.setText(session.loginName)
                ageDetails.setText(session.loginAge)
                patientName.isEnabled = false
                ageDetails.isEnabled = false
            } else {
                patientName.setText("")
                ageDetails.setText("")
            }
            clinicDetails.text = clinicAddress
            contactDetails.text = clinicContact

            gender.text = session.loginGender
            selectedGender = session.loginGender.toString()
            showDateParsed = selectedDateFormat.parse(consultationDate)
            val showDate = dateFormat.format(showDateParsed)
            appointmentDateTime.text =
                "$consultationDay, $showDate ($consultationStartTime - $consultationEndTime)"

        }
    }

    private fun setObserver() {
        appointmentViewModel.doctor.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.status == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(), response.data.message)
                    } else {
                        if (response.data?.status == 200) {
                            binding.noData.isVisible = false
                            binding.loader.isVisible = false
                            doctorDetails = response.data.data[0]
                            setPatientData("user")
                            setDoctorData()
                        } else {
                            binding.noData.isVisible = true
                            binding.loader.isVisible = true
                        }
                    }
                }

                is Resource.Loading -> {
                    binding.loader.isVisible = true
                    appLoader.show()
                }

                is Resource.Error -> {
                    binding.loader.isVisible = true
                    binding.noData.isVisible = true
                    appLoader.dismiss()
                }
            }
        }

        appointmentViewModel.bookAppointment.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.status?.toInt() == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(), response.data.message)
                    } else {
                        if (response.data?.success!!) {
                            showToast(response.data.message)
                            showConfirmDialog()
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

        bookType.observe(viewLifecycleOwner) {
            when (it) {
                "user" -> {
                    binding.editPatientDetailsLbl.isVisible = true
                    binding.patientName.isEnabled = false
                    binding.ageDetails.isEnabled = false
                    binding.editGenderGroup.isVisible = false
                    binding.gender.isVisible = true
                    selectedBookType = it
                    setPatientData("user")
                }

                "guest" -> {
                    binding.editPatientDetailsLbl.isVisible = false
                    binding.patientName.isEnabled = true
                    binding.ageDetails.isEnabled = true
                    binding.editGenderGroup.isVisible = true
                    binding.gender.isVisible = false
                    selectedBookType = it

                    setPatientData("guest")
                }

                "edit" -> {
                    binding.editPatientDetailsLbl.isVisible = false
                    binding.patientName.isEnabled = true
                    binding.ageDetails.isEnabled = true
                    binding.editGenderGroup.isVisible = true
                    binding.gender.isVisible = false
                    binding.patientName.requestFocus()
                    selectedBookType = "user"
                }
            }
        }


        appointmentViewModel.applyCoupon.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    showToast(response.data?.message.toString())
                    if (response.data?.status == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(), response.data.message)
                    } else if (response.data?.code == 200) {
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
            removeCouponBtn.isVisible = true
            applyCouponBtn.visibility = View.INVISIBLE
            couponEdt.isEnabled = false
            couponAmt.isVisible = true
            couponAppliedLbl.isVisible = true

            consultationFees.text = "₹${data.original_price}"
            couponAmt.text = "-₹${data.discounted_price}"
            totalAmt.text = "₹${data.final_price}"
            consultationFee = data.final_price
        }
    }

    private fun removeAppliedCoupon() {
        with(binding) {
            applyCouponBtn.visibility = View.VISIBLE
            removeCouponBtn.visibility = View.GONE
            couponEdt.isEnabled = true
            couponAmt.isVisible = false
            couponAppliedLbl.isVisible = false
            couponEdt.isEnabled = true
            couponEdt.setText("")
            totalAmt.text = "₹${doctorConsultationFee}"
            consultationFee = doctorConsultationFee

        }

    }

    @SuppressLint("SetTextI18n")
    private fun setDoctorData() {

        with(binding) {
            Glide.with(requireContext()).load(doctorDetails.profile_picture).into(doctorImage)
            doctorName.text = doctorDetails.doctorName
            doctorSpecialistIn.text = doctorDetails.specialization
            doctorLocation.text = doctorDetails.address
            doctorDegree.text = doctorDetails.qualification
            verifiedIcon.isVisible = doctorDetails.is_approved
            consultationFees.text = "₹${doctorDetails.consultationfee}"
            totalAmt.text = "₹${doctorDetails.consultationfee}"
            consultationFee = doctorDetails.consultationfee.toInt()
            doctorConsultationFee = doctorDetails.consultationfee.toInt()
        }


    }

    private fun setOnClickListener() {
        with(binding) {
            backArrow.setOnClickListener(this@CheckOutBookingAppointmentFragment)
            payAndConfirm.setOnClickListener(this@CheckOutBookingAppointmentFragment)
            payAtClinicCheckBox.setOnClickListener(this@CheckOutBookingAppointmentFragment)
            payOnlineCheckBox.setOnClickListener(this@CheckOutBookingAppointmentFragment)
            editPatientDetailsLbl.setOnClickListener(this@CheckOutBookingAppointmentFragment)

            guestRadioBg.setOnClickListener(this@CheckOutBookingAppointmentFragment)
            guestRadioBtn.setOnClickListener(this@CheckOutBookingAppointmentFragment)

            selfRadioBg.setOnClickListener(this@CheckOutBookingAppointmentFragment)
            selfRadioBtn.setOnClickListener(this@CheckOutBookingAppointmentFragment)

            maleRadioBg.setOnClickListener(this@CheckOutBookingAppointmentFragment)
            maleRadioBtn.setOnClickListener(this@CheckOutBookingAppointmentFragment)

            femaleRadioBg.setOnClickListener(this@CheckOutBookingAppointmentFragment)
            femaleRadioBtn.setOnClickListener(this@CheckOutBookingAppointmentFragment)
            applyCouponBtn.setOnClickListener(this@CheckOutBookingAppointmentFragment)
            removeCouponBtn.setOnClickListener(this@CheckOutBookingAppointmentFragment)

        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backArrow -> {
                findNavController().popBackStack()
            }

            R.id.payAndConfirm -> {
                validateAndPay()
            }

            R.id.payAtClinicCheckBox -> {
                binding.payAtClinicCheckBox.isChecked = true
                binding.payOnlineCheckBox.isChecked = false
                selectedPaymentType = "Cash"
            }

            R.id.editPatientDetailsLbl -> {
                bookType.postValue("edit")
//                val bundle = Bundle().apply { putString("from", "appointment") }
//                findNavController().navigate(R.id.editProfileFragment, bundle)
            }

            R.id.guestRadioBg -> {
                binding.selfRadioBtn.isChecked = false
                binding.guestRadioBtn.isChecked = true
                bookType.postValue("guest")
            }

            R.id.guestRadioBtn -> {
                binding.selfRadioBtn.isChecked = false
                binding.guestRadioBtn.isChecked = true
                bookType.postValue("guest")
            }

            R.id.selfRadioBtn -> {
                binding.selfRadioBtn.isChecked = true
                binding.guestRadioBtn.isChecked = false
                bookType.postValue("user")
            }

            R.id.selfRadioBg -> {
                binding.selfRadioBtn.isChecked = true
                binding.guestRadioBtn.isChecked = false
                bookType.postValue("user")
            }

            R.id.femaleRadioBg -> {
                binding.maleRadioBtn.isChecked = false
                binding.femaleRadioBtn.isChecked = true
                selectedGender = "Female"
            }

            R.id.femaleRadioBtn -> {
                binding.maleRadioBtn.isChecked = false
                binding.femaleRadioBtn.isChecked = true
                selectedGender = "Female"

            }

            R.id.maleRadioBtn -> {
                binding.maleRadioBtn.isChecked = true
                binding.femaleRadioBtn.isChecked = false
                selectedGender = "Male"

            }

            R.id.maleRadioBg -> {
                binding.maleRadioBtn.isChecked = true
                binding.femaleRadioBtn.isChecked = false
                selectedGender = "Male"
            }

            R.id.payOnlineCheckBox -> {
                binding.payOnlineCheckBox.isChecked = true
                binding.payAtClinicCheckBox.isChecked = false
                selectedPaymentType = "Online"
            }

            R.id.applyCouponBtn -> {
                applyCoupon()
            }

            R.id.removeCouponBtn -> {
                removeAppliedCoupon()
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
            addProperty("doctorId", doctorId)
        }
        appointmentViewModel.applyCoupon(jsonObject)

    }

    private fun validateAndPay() {
        val patientName = binding.patientName.text.toString()
        val patientAge = binding.ageDetails.text.toString()
        val consultationDate = selectConsultationDateFormat.format(showDateParsed)
        showReports = binding.showReports.isChecked


        val jsonObject = JsonObject()
        jsonObject.addProperty("booked_as", selectedBookType)
        jsonObject.addProperty("user_id", session.patientId)
        jsonObject.addProperty("patient_id", session.patientId)
        jsonObject.addProperty("patientname", patientName)
        jsonObject.addProperty("age", patientAge)
        jsonObject.addProperty("gender", selectedGender)
        jsonObject.addProperty("clinicname", clinicName)
        jsonObject.addProperty("consultation_fee", consultationFee)
        jsonObject.addProperty("appointment_type", appointmentType)
        jsonObject.addProperty("date", consultationDate)
        jsonObject.addProperty("departmentname", doctorDetails.specialization)
        jsonObject.addProperty("doctor_id", doctorDetails._id)
        jsonObject.addProperty("doctorname", doctorDetails.doctorName)
        jsonObject.addProperty("mobile", session.loginPhone)
        jsonObject.addProperty("mode_of_payment", selectedPaymentType)
        jsonObject.addProperty("problem", "")
        jsonObject.addProperty("slotIdArray", consultationTimeId)
        jsonObject.addProperty("status", "pending")
        jsonObject.addProperty("is_report", showReports)


        if (selectedPaymentType == "Online") {
            jsonObjectOnline = jsonObject
            createOrderId()
        } else if (selectedPaymentType == "Cash") {
            jsonObject.addProperty("payment_status", false)
            jsonObject.addProperty("payment_id", session.patientId)
            appointmentViewModel.bookAppointment(jsonObject)
        }
    }


    private fun createOrderId() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("amount", consultationFee)
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
        jsonObject.addProperty("doctor_id", doctorId)
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
                        findNavController().navigate(R.id.appointmentFragment)
                        showConfirmDialog()
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


    private fun showConfirmDialog() {
        val confirmDialog = Dialog(requireActivity())
        confirmDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        confirmDialog.setCancelable(false)


        confirmDialog.setContentView(R.layout.payment_confirm_dialog)
        confirmDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val showDateParsed = selectedDateFormat.parse(consultationDate)
        val showDate = dateFormat.format(showDateParsed)

        confirmDialog.findViewById<TextView>(R.id.doctorName).text = doctorDetails.doctorName
        confirmDialog.findViewById<TextView>(R.id.time).text =
            "($consultationStartTime - $consultationEndTime)"
        confirmDialog.findViewById<TextView>(R.id.date).text = showDate
        confirmDialog.findViewById<TextView>(R.id.patientId).text = session.loginName

        confirmDialog.findViewById<View>(R.id.doneBtn).setOnClickListener {
            confirmDialog.dismiss()
            if (selectedPaymentType == "Online") {
                confirmDialog.dismiss()
            } else {
                findNavController().navigate(R.id.action_checkOutBookingAppointmentFragment_to_homeFragment)
            }
        }


        confirmDialog.show()
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val displayWidth = displayMetrics.widthPixels
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(confirmDialog.window!!.attributes)
        val dialogWindowWidth = (displayWidth * 0.9f).toInt()

        layoutParams.width = dialogWindowWidth
        confirmDialog.window!!.attributes = layoutParams
    }

    override fun onResume() {
        super.onResume()
        if (this::doctorDetails.isInitialized) {
//            setPatientData("user")
        }
    }
}