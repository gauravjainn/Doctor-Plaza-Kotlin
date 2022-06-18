package com.doctorsplaza.app.ui.patient.fragments.home

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
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
import com.doctorsplaza.app.databinding.FragmentHomeBinding
import com.doctorsplaza.app.data.commonModel.AppointmentData
import com.doctorsplaza.app.ui.patient.fragments.addAppointmentForm.model.RoomTimeSlotsData
import com.doctorsplaza.app.ui.patient.fragments.appointments.AppointmentViewModel
import com.doctorsplaza.app.ui.patient.fragments.bookAppointment.adapter.BookTimeAdapter
import com.doctorsplaza.app.ui.patient.fragments.clinicDoctors.model.DoctorData
import com.doctorsplaza.app.ui.patient.fragments.home.adapter.BannerAdapter
import com.doctorsplaza.app.ui.patient.fragments.home.adapter.OurDoctorsAdapter
import com.doctorsplaza.app.ui.patient.fragments.home.adapter.OurSpecialistAdapter
import com.doctorsplaza.app.ui.patient.fragments.home.adapter.UpcomingAppointmentsAdapter
import com.doctorsplaza.app.ui.patient.fragments.home.model.BannerData
import com.doctorsplaza.app.ui.patient.fragments.home.model.SpecialistData
import com.doctorsplaza.app.utils.*
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), View.OnClickListener {
    private lateinit var binding: FragmentHomeBinding

    private lateinit var rescheduleDialog: Dialog
    private var timeSlotsListPosition: Int = 0
    private lateinit var consultationDateView: TextView
    private lateinit var consultationTimeView: TextView
    private var selectedAppointmentType: String = "Offline"
    private var timeSlotSelected: Boolean = false
    private var consultationDate: String? = ""

    private var selectedConsultationDate: String? = ""


    private val homeViewModel: HomeViewModel by viewModels()

    private val appointmentViewModel: AppointmentViewModel by viewModels()

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var doctorPlazaLoader: DoctorPlazaLoader

    @Inject
    lateinit var bannerAdapter: BannerAdapter

    @Inject
    lateinit var specialistAdapter: OurSpecialistAdapter

    @Inject
    lateinit var ourDoctorsAdapter: OurDoctorsAdapter

    @Inject
    lateinit var upcomingAppointmentsAdapter: UpcomingAppointmentsAdapter

    private lateinit var specialistData: List<SpecialistData>

    private var timeSlotsList: MutableList<RoomTimeSlotsData> = ArrayList()

    @Inject
    lateinit var bookTimeAdapter: BookTimeAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_home, container, false)
            binding = FragmentHomeBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
            setAdapterClickListener()
        }
        return currentView!!
    }


    private fun init() {
        doctorPlazaLoader = DoctorPlazaLoader(requireContext())
        homeViewModel.getPatientBanners()
        homeViewModel.getOurSpecialists()
        homeViewModel.getOurDoctors()
        homeViewModel.getAppointments()
    }


    private fun setObserver() {
        homeViewModel.patientBanner.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.status == 200) {
                        setBannerView(response.data.data)
                    }
                }
                is Resource.Loading -> {
                    showLoading()
                }
                is Resource.Error -> {
                }
            }
        }

        homeViewModel.ourSpecialists.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.status == 200) {
                        specialistData = response.data.data
                        setSpecialists(specialistData)
                    }
                }
                is Resource.Loading -> {

                }
                is Resource.Error -> {

                }
            }
        }

        homeViewModel.ourDoctors.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.code == 200) {
                        if (response.data.data.isEmpty()) {
                            binding.noDoctorsLbl.isVisible = true
                            binding.ourDoctorsViewAll.isVisible = false
                        } else {
                            binding.noDoctorsLbl.isVisible = false
                            binding.ourDoctorsViewAll.isVisible = true
                            setOurDoctors(response.data.data)
                        }
                    }
                }
                is Resource.Loading -> {
                }
                is Resource.Error -> {
                }
            }
        }
        homeViewModel.appointments.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    hideLoading()
                    if (response.data?.status == 200) {
                        if (response.data.data.isEmpty()) {
                            binding.appointmentRv.isVisible = false
                            binding.noAppointments.isVisible = true
                            binding.appointmentViewAll.isVisible = false
                        } else {
                            binding.appointmentRv.isVisible = true
                            binding.noAppointments.isVisible = false
                            binding.appointmentViewAll.isVisible = true
                            setAppointmentRv(response.data.data)
                        }
                    } else {
                        showNoData()
                    }
                }
                is Resource.Loading -> {

                }
                is Resource.Error -> {
                    showNoData()
                    showToast(response.message.toString())
                }
            }
        }

        appointmentViewModel.drClinicTimeSlots.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    doctorPlazaLoader.dismiss()
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
                    doctorPlazaLoader.show()
                }
                is Resource.Error -> {
                    doctorPlazaLoader.dismiss()
                }
            }
        }


        appointmentViewModel.cancelAppointment.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    doctorPlazaLoader.dismiss()
                    if(response.data?.success!!){
                        showToast(response.data.message)
                        homeViewModel.getAppointments()
                    }else{
                        showToast(response.data.message)
                    }
                }
                is Resource.Loading -> {
                    doctorPlazaLoader.show()
                }
                is Resource.Error -> {
                    doctorPlazaLoader.dismiss()
                }
            }
        }

        appointmentViewModel.rescheduleAppointment.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    doctorPlazaLoader.dismiss()
                    if (response.data?.code==200) {
                        showToast(response.data.message)
                        rescheduleDialog.dismiss()
                        homeViewModel.getAppointments()
                    } else {
                        showToast(response.data?.message.toString())
                    }
                }

                is Resource.Loading -> {
                    doctorPlazaLoader.show()
                }

                is Resource.Error -> {
                    doctorPlazaLoader.dismiss()
                }
            }
        }
    }

    private fun setAppointmentRv(data: List<AppointmentData>) {
        upcomingAppointmentsAdapter.differ.submitList(data)
        binding.appointmentRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = upcomingAppointmentsAdapter
        }
    }


    private fun showLoading() {
        doctorPlazaLoader.show()
        binding.loading.isVisible = true
        binding.noData.isVisible = false
    }

    private fun hideLoading() {
        doctorPlazaLoader.dismiss()
        binding.loading.isVisible = false
        binding.noData.isVisible = false
    }

    private fun showNoData() {
        doctorPlazaLoader.dismiss()
        binding.loading.isVisible = true
        binding.noData.isVisible = true
    }

    private fun setBannerView(data: List<BannerData>) {
        bannerAdapter.differ.submitList(data)
        binding.patientsBanner.adapter = bannerAdapter
        val tabLayoutMediator =
            TabLayoutMediator(binding.bannerDots, binding.patientsBanner, true) { _, _ -> }
        tabLayoutMediator.attach()
    }


    private fun setSpecialists(data: List<SpecialistData>) {
        specialistAdapter.differ.submitList(data)
        binding.specialistsRv.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = specialistAdapter
        }
    }

    private fun setOurDoctors(data: List<DoctorData>) {
        ourDoctorsAdapter.differ.submitList(data)
        binding.ourDoctorsRv.apply {
            setHasFixedSize(true)
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = ourDoctorsAdapter
        }

    }

    private fun setOnClickListener() {
        with(binding) {
            specialistsViewAll.setOnClickListener(this@HomeFragment)
            ourDoctorsViewAll.setOnClickListener(this@HomeFragment)
            appointmentViewAll.setOnClickListener(this@HomeFragment)
            bookBloodCallNow.setOnClickListener(this@HomeFragment)
            orderMedicineCallNow.setOnClickListener(this@HomeFragment)
            physioCallNow.setOnClickListener(this@HomeFragment)
            searchBar.setOnClickListener(this@HomeFragment)

        }
    }


    private fun setAdapterClickListener() {
        ourDoctorsAdapter.setOnDoctorClickListener {
            val bundle = Bundle()
            bundle.putString("doctorId", it._id)
            findNavController().navigate(R.id.doctorDetailsFragment, bundle)
        }

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
            showCancelWarning(it._id)
        }

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

        specialistAdapter.setOnViewDoctors {
            val bundle = Bundle().apply {
                putString("searchKey",it.name)
            }
            findNavController().navigate(R.id.searchFragment,bundle)
        }

    }


    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.specialistsViewAll -> {
                val bundle = Bundle().apply {
                    putString("searchKey","")
                }
                findNavController().navigate(R.id.searchFragment,bundle)
            }

            R.id.searchBar -> {
                val bundle = Bundle().apply {
                    putString("searchKey","")
                }
                findNavController().navigate(R.id.searchFragment,bundle)
            }

            R.id.ourDoctorsViewAll -> {
                val bundle = Bundle()
                bundle.putString("from", "home")
                findNavController().navigate(R.id.ourDoctorsFragment, bundle)
            }

            R.id.appointmentViewAll -> {
                findNavController().navigate(R.id.appointmentFragment)
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



    override fun onResume() {
        super.onResume()
        setObserver()
        homeViewModel.getAppointments()
    }
}
