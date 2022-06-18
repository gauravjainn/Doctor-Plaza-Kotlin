package com.doctorsplaza.app.ui.patient.fragments.appointments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.AbsListView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentAppointmentBinding
import com.doctorsplaza.app.data.commonModel.AppointmentData
import com.doctorsplaza.app.ui.patient.fragments.addAppointmentForm.model.RoomTimeSlotsData
import com.doctorsplaza.app.ui.patient.fragments.appointments.adapter.PastAppointmentsAdapter
import com.doctorsplaza.app.ui.patient.fragments.bookAppointment.adapter.BookTimeAdapter
import com.doctorsplaza.app.ui.patient.fragments.home.adapter.UpcomingAppointmentsAdapter
import com.doctorsplaza.app.utils.*
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class AppointmentFragment : Fragment(R.layout.fragment_appointment), View.OnClickListener {

    private lateinit var binding: FragmentAppointmentBinding

    private lateinit var rescheduleDialog: Dialog
    private var timeSlotsListPosition: Int = 0
    private lateinit var consultationDateView: TextView
    private lateinit var consultationTimeView: TextView
    private var selectedAppointmentType: String = "Offline"
    private var timeSlotSelected: Boolean = false
    private var consultationDate: String? = ""
    private var selectedConsultationDate: String? = ""
    private var timeSlotsList: MutableList<RoomTimeSlotsData> = ArrayList()

    @Inject
    lateinit var bookTimeAdapter: BookTimeAdapter

    @Inject
    lateinit var session: SessionManager

    private val appointmentViewModel: AppointmentViewModel by viewModels()

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader

    @Inject
    lateinit var upcomingAppointmentsAdapter: UpcomingAppointmentsAdapter

    @Inject
    lateinit var pastAppointmentsAdapter: PastAppointmentsAdapter

    private var appointmentDataList :List<AppointmentData> = ArrayList()

    private var pageNo = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_appointment, container, false)
            binding = FragmentAppointmentBinding.bind(currentView!!)
            init()
            setPullRefresh()
            setObserver()
            setOnAdapterClickListener()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())
//        appointmentViewModel.getAppointments(pageNo = pageNo.toString())
        appointmentViewModel.getAppointments(pageNo = pageNo.toString(), "new")
        appointmentViewModel.getPastAppointments(pageNo = pageNo.toString(), "old")
    }


    private fun setPullRefresh() {
        binding.pullToRefresh.setOnRefreshListener {
            val jsonObject = JsonObject()
            jsonObject.addProperty("id", session.loginId)
            appointmentViewModel.getAppointments(pageNo = pageNo.toString(), "new")
            appointmentViewModel.getPastAppointments(pageNo = pageNo.toString(), "old")
            binding.pullToRefresh.isRefreshing = false
        }
    }


    private fun setObserver() {
        appointmentViewModel.appointments.observe(viewLifecycleOwner) { response ->
            when (response) {

                is Resource.Success -> {
                    appLoader.dismiss()
                    binding.loader.isVisible = false
                    binding.errorMsg.isVisible = false
                    if (response.data?.status == 200) {
                        if (response.data.data.isEmpty()) {
                            binding.appointmentRv.isVisible = false
                            binding.noAppointments.isVisible = true
                        } else {
                            binding.appointmentRv.isVisible = true
                            binding.noAppointments.isVisible = false
                            appointmentDataList = response.data.data
                            setAppointmentRv(appointmentDataList)
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
                    showToast(response.message!!)
                }
            }
        }

        appointmentViewModel.pastAppointments.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.status == 200) {
                        if (response.data.data.isEmpty()) {
                            showServiceView()
                        } else {
                            binding.servicesGroup.isVisible = false
                            setPastAppointments(response.data.data)
                        }
                    }
                }

                is Resource.Loading -> {
                    appLoader.show()
                }

                is Resource.Error -> {
                    appLoader.dismiss()
                    showToast(response.message!!)
                }
            }
        }

        appointmentViewModel.cancelAppointment.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.success!!) {
                        showToast(response.data.message)
                        appointmentViewModel.getAppointments("1", "new")
                    } else {
                        showToast(response.data.message)
                    }
                }
                is Resource.Loading -> {
                    appLoader.show()
                }
                is Resource.Error -> {
                    appLoader.dismiss()
                    showToast(response.message!!)
                }
            }
        }

        appointmentViewModel.drClinicTimeSlots.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.data?.isEmpty()!!) {
                        showToast("No Slots Available for Selected Date")
                        consultationTimeView.text = ""
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

        appointmentViewModel.rescheduleAppointment.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.code==200) {
                        showToast(response.data.message)
                        rescheduleDialog.dismiss()
                        appointmentViewModel.getAppointments(pageNo = pageNo.toString(),"new")
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

    }

    private fun showServiceView() {
        if(appointmentDataList.isEmpty()){
            binding.noPastAppointmentsDataLbl.text = "There is no appointments but we provide following service."
        }else{
            binding.noPastAppointmentsDataLbl.text = "There is no past appointments but we provide following service."
        }

        binding.servicesGroup.isVisible = true
    }

    private fun setOnAdapterClickListener() {

        upcomingAppointmentsAdapter.setOnAppointmentClickListener {
            val bundle = Bundle().apply {
                putString("from","upcoming")
                putString("appointmentId",it._id)
            }

            findNavController().navigate(R.id.appointmentDetailsFragment,bundle)
        }

        upcomingAppointmentsAdapter.setOnRescheduleClickListener {
            showRescheduleDialog(it._id,it)

        }

        upcomingAppointmentsAdapter.setOnCancelClickListener {
           showCancelWarning(appointmentId = it._id)

        }

        bookTimeAdapter.setOnTimeSelectedListener { _, position ->
            timeSlotsList.forEach { it ->
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


    private fun showRescheduleDialog(appointmentId: String, appointmentData: AppointmentData) {
        rescheduleDialog = Dialog(requireActivity())
        rescheduleDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        rescheduleDialog.setCancelable(false)
        rescheduleDialog.setContentView(R.layout.dialogue_reschedule_selection)
        rescheduleDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val offlineText = rescheduleDialog.findViewById<TextView>(R.id.offlineTxt)
        val onlineText = rescheduleDialog.findViewById<TextView>(R.id.onlineTxt)
        val cancelBtn = rescheduleDialog.findViewById<TextView>(R.id.cancelBtn)
        consultationDateView = rescheduleDialog.findViewById(R.id.consultationDate)
        consultationTimeView = rescheduleDialog.findViewById(R.id.consultationTime)

        cancelBtn.setOnClickListener {
            rescheduleDialog.dismiss()
        }
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
            getDate(appointmentData)
        }


        consultationTimeView.setOnClickListener {
            if(consultationDateView.text.toString().isNotEmpty()){
                if(timeSlotsList.isEmpty()){
                    showToast("there are no slots available for selected date")
                }else{
                    showTimeSlotsDialog()

                }
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
                    jsonObject.addProperty(
                        "date",
                        getDateFormatted(
                            consultationDate.toString(),
                            DATE_PATTERN,
                            DATE_FULL_PATTERN
                        )
                    )
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

    private fun getDate(appointmentData: AppointmentData) {
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

    private fun showTimeSlotsDialog() {
        val timeSlotsDialog = Dialog(requireActivity())
        timeSlotsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        timeSlotsDialog.setCancelable(false)
        timeSlotsDialog.setContentView(R.layout.time_slots_dialogue)
        timeSlotsDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        timeSlotsDialog.findViewById<View>(R.id.cancelBtn).setOnClickListener {
            timeSlotsDialog.dismiss()
        }

        timeSlotsDialog.findViewById<View>(R.id.submitBtn).setOnClickListener {
            if (timeSlotSelected) {
                timeSlotsDialog.dismiss()
            } else {
                showToast("Please Select Any One Time Slot")
            }
        }

        timeSlotsDialog.findViewById<View>(R.id.cancelBtn).setOnClickListener {
            consultationTimeView.text = ""
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

    private fun showCancelWarning(appointmentId: String) {
        AlertDialog.Builder(context)
            .setTitle("Cancel Appointment")
            .setMessage("Are you sure you want to Cancel Appointment?")
            .setPositiveButton(
                "yes"
            ) { _, _ ->
                cancelAppointment(appointmentId)
            }
            .setNegativeButton("no", null)
            .show()
    }

    private fun cancelAppointment(appointmentId: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("by", "patient")
        appointmentViewModel.cancelAppointment(appointmentId = appointmentId, jsonObject)
    }


    private fun setAppointmentRv(data: List<AppointmentData>) {
        upcomingAppointmentsAdapter.differ.submitList(data)
        binding.appointmentRv.apply {
            setHasFixedSize(true)
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = upcomingAppointmentsAdapter
        }.addOnScrollListener(scrollListener)
    }

    var isLoading = false
    var isScrolling = false


    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            if (dy > 0) {
                val recycleLayoutManager =
                    binding.appointmentRv.layoutManager as LinearLayoutManager
                if (!isLoading) {
                    if (recycleLayoutManager != null && recycleLayoutManager.findLastCompletelyVisibleItemPosition() == upcomingAppointmentsAdapter.itemCount - 2) {
                        pageNo++
                        appointmentViewModel.getAppointments(pageNo = pageNo.toString(), "new")
                        isLoading = true
                    }
                }
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }

    private fun setPastAppointments(data: List<AppointmentData>) {
        pastAppointmentsAdapter.differ.submitList(data)
        binding.pastAppointmentsRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = pastAppointmentsAdapter
        }

    }

    private fun setOnClickListener() {
        binding.addAppointment.setOnClickListener(this@AppointmentFragment)
        binding.bookBloodCallNow.setOnClickListener(this@AppointmentFragment)
        binding.orderMedicineCallNow.setOnClickListener(this@AppointmentFragment)
        binding.physioCallNow.setOnClickListener(this@AppointmentFragment)
        pastAppointmentsAdapter.setOnPastAppointmentClickListener {
            val bundle = Bundle().apply { putString("appointmentId", it) }
            findNavController().navigate(R.id.appointmentDetailsFragment, bundle)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.addAppointment -> {
                findNavController().navigate(R.id.addAppointmentFormFragment)
            }

            R.id.bookBloodCallNow -> {
                val number = Uri.parse("tel:9876543214")
                val callIntent = Intent(Intent.ACTION_DIAL, number)
                requireActivity().startActivity(callIntent)
            }
            R.id.physioCallNow -> {
                val number = Uri.parse("tel:9876543214")
                val callIntent = Intent(Intent.ACTION_DIAL, number)
                requireActivity().startActivity(callIntent)
            }
            R.id.orderMedicineCallNow -> {
                val number = Uri.parse("tel:9876543214")
                val callIntent = Intent(Intent.ACTION_DIAL, number)
                requireActivity().startActivity(callIntent)
            }
        }
    }
}