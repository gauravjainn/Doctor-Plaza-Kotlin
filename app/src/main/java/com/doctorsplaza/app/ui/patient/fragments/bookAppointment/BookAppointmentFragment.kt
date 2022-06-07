package com.doctorsplaza.app.ui.patient.fragments.bookAppointment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentBookAppointmentBinding
import com.doctorsplaza.app.ui.patient.fragments.addAppointmentForm.model.RoomTimeSlotsData
import com.doctorsplaza.app.ui.patient.fragments.appointments.AppointmentViewModel
import com.doctorsplaza.app.ui.patient.fragments.bookAppointment.adapter.BookDateAdapter
import com.doctorsplaza.app.ui.patient.fragments.bookAppointment.adapter.BookTimeAdapter
import com.doctorsplaza.app.ui.patient.fragments.doctorDetails.model.DoctorDetailsData
import com.doctorsplaza.app.ui.patient.fragments.reminder.model.DateModel
import com.doctorsplaza.app.utils.DoctorPlazaLoader
import com.doctorsplaza.app.utils.Resource
import com.doctorsplaza.app.utils.SessionManager
import com.doctorsplaza.app.utils.showToast
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class BookAppointmentFragment : Fragment(R.layout.fragment_book_appointment), View.OnClickListener {

    private var clinicAddress: String = ""

    private var timeSlotsListPosition: Int = 0

    private var clinicName: String = ""

    private var clinicContact: String = ""

    private var clinicId: String = ""

    private var doctorId: String = ""

    private var selectedBookDate: String = ""

    private lateinit var binding: FragmentBookAppointmentBinding

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader

    private var dateList: MutableList<DateModel> = ArrayList()

    private val appointmentViewModel: AppointmentViewModel by viewModels()

    @Inject
    lateinit var bookDateAdapter: BookDateAdapter

    @Inject
    lateinit var bookTimeAdapter: BookTimeAdapter

    lateinit var doctorDetails: DoctorDetailsData

    var dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val selectedDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

    private var timeSlotsData: MutableList<RoomTimeSlotsData> = ArrayList()

    private var appointmentType = "Offline"

    private var timeSlotSelected = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_book_appointment, container, false)
            binding = FragmentBookAppointmentBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
            setOnAdapterClickListener()
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
        appointmentType = arguments?.getString("appointmentType").toString()
        appointmentViewModel.getDoctor(doctorId)
        binding.bookDateRv.addOnScrollListener(dateScroller)
    }

    private fun setObserver() {
        appointmentViewModel.doctor.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {

                    appLoader.dismiss()
                    if (response.data?.status == 200) {
                        binding.noData.isVisible = false
                        binding.loader.isVisible = false
                        doctorDetails = response.data.data[0]
                        setDoctorData()
                        setDateList()
                    } else {
                        binding.noData.isVisible = true
                        binding.loader.isVisible = true
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

        appointmentViewModel.drClinicTimeSlots.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.code == 200) {
                        if (response.data.data.isEmpty()) {
                            binding.noTimeSlots.isVisible = true
                            binding.bookingTimeSlotRv.isVisible = false
                        } else {
                            binding.noTimeSlots.isVisible = false
                            binding.bookingTimeSlotRv.isVisible = true
                            timeSlotsData.clear()
                            timeSlotsData.addAll(response.data.data)
                            setTimeSlots()
                        }
                    } else {
                        binding.noTimeSlots.isVisible = true
                        binding.bookingTimeSlotRv.isVisible = false
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

    private fun setTimeSlots() {
        bookTimeAdapter.differ.submitList(timeSlotsData)
        binding.bookingTimeSlotRv.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = bookTimeAdapter
        }

    }

    private fun setDoctorData() {
        var doctorQualification = ""
        with(binding) {
            Glide.with(requireContext()).load(doctorDetails.profile_picture).into(doctorImage)
            doctorName.text = doctorDetails.doctorName
            doctorSpecialistIn.text = doctorDetails.specialization
            doctorLocation.text = doctorDetails.city
            doctorDetails.qualification.forEach {
                doctorQualification = if (doctorQualification.isEmpty()) {
                    it
                } else {
                    "$doctorQualification , $it"
                }
            }
            doctorDegree.text = doctorQualification
            verifiedIcon.isVisible = doctorDetails.is_approved
        }
    }

    private fun setDateList() {
        for (i in 0 until 90) {
            val calendar: Calendar = GregorianCalendar()
            calendar.add(Calendar.DATE, i)
            val dateModel = DateModel(i, dateFormat.format(calendar.time).toString(), i == 0)
            dateList.add(dateModel)
        }

        bookDateAdapter.differ.submitList(dateList)
        binding.bookDateRv.apply {
            setHasFixedSize(true)
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = bookDateAdapter
        }
        val selectedDate = dateFormat.parse(dateList[0].date)
        selectedBookDate = selectedDateFormat.format(selectedDate)

        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val consultationDay = dayFormat.format(selectedDate)
        val consultationDate = dateFormat.format(selectedDate)

        val jsonObject = JsonObject()
        jsonObject.addProperty("clinicId", clinicId)
        jsonObject.addProperty("doctorId", doctorDetails._id)
        jsonObject.addProperty("date", selectedBookDate)
        jsonObject.addProperty("day", consultationDay)
        appointmentViewModel.getRoomSlotDetailsByDrAndClinicId(appointmentType,jsonObject)

    }


    private val dateScroller = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val myLayoutManager: LinearLayoutManager =
                binding.bookDateRv.layoutManager as LinearLayoutManager
            val scrollPosition = myLayoutManager.findFirstVisibleItemPosition()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val monthYearFormat = SimpleDateFormat("MMM, yyyy", Locale.getDefault())
            val dateParsed = dateFormat.parse(dateList[scrollPosition + 1].date)
            val newMonthYear = monthYearFormat.format(dateParsed)
            binding.monthLbl.text = newMonthYear
        }
    }

    private fun setOnClickListener() {
        binding.backArrow.setOnClickListener(this@BookAppointmentFragment)
        binding.saveBtn.setOnClickListener(this@BookAppointmentFragment)
    }

    private fun setOnAdapterClickListener() {
        bookDateAdapter.setOnDateSelected { dateId, position ->
            dateList.forEach {
                if (it.selected) {
                    it.selected = false
                }
            }
            dateList[position].selected = true
            val selectedDate = dateFormat.parse(dateList[position].date)
            selectedBookDate = selectedDateFormat.format(selectedDate)
            bookDateAdapter.differ.submitList(dateList)
            bookDateAdapter.notifyDataSetChanged()

            val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val consultationDay = dayFormat.format(selectedDate)
            val consultationDate = dateFormat.format(selectedDate)

            val jsonObject = JsonObject()
            jsonObject.addProperty("clinicId", clinicId)
            jsonObject.addProperty("doctorId", doctorDetails._id)
            jsonObject.addProperty("date", selectedBookDate)
            jsonObject.addProperty("day", consultationDay)
            appointmentViewModel.getRoomSlotDetailsByDrAndClinicId(appointmentType,jsonObject)
        }

        bookTimeAdapter.setOnTimeSelectedListener { _, position ->
            timeSlotsData.forEach {
                if (it.timeSlotData.isSelected) it.timeSlotData.isSelected = false
            }
            timeSlotsData[position].timeSlotData.isSelected = true
            timeSlotsListPosition = position
            bookTimeAdapter.differ.submitList(timeSlotsData)
            timeSlotSelected = true
            bookTimeAdapter.notifyDataSetChanged()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backArrow -> {
                findNavController().popBackStack()
            }
            R.id.saveBtn -> {
                if(timeSlotSelected){
                    val bundle = Bundle()
                    bundle.putString("doctorId",doctorId)
                    bundle.putString("consultationDate",selectedBookDate)
                    bundle.putString("consultationDay",timeSlotsData[timeSlotsListPosition].day)
                    bundle.putString("consultationTimeId",timeSlotsData[timeSlotsListPosition]._id)
                    bundle.putString("consultationStartTime",timeSlotsData[timeSlotsListPosition].timeSlotData.start_time)
                    bundle.putString("consultationEndTime",timeSlotsData[timeSlotsListPosition].timeSlotData.end_time)
                    bundle.putString("consultationEndTime",timeSlotsData[timeSlotsListPosition].timeSlotData.end_time)
                    bundle.putString("appointmentType",appointmentType)
                    findNavController().navigate(R.id.checkOutBookingAppointmentFragment,bundle)
                }else{
                    showToast("please any one time slot")
                }

            }
        }
    }


}