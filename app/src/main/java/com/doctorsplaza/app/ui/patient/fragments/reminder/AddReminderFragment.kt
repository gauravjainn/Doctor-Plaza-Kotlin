package com.doctorsplaza.app.ui.patient.fragments.reminder

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentAddReminderBinding
import com.doctorsplaza.app.utils.*
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.gson.JsonObject
import com.doctorsplaza.app.utils.SessionManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.sql.Time
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class AddReminderFragment : Fragment(R.layout.fragment_add_reminder), View.OnClickListener {
    private var reminderId: String = ""
    private var reminderTime: String = ""
    private var reminderDate: String = ""
    private lateinit var binding: FragmentAddReminderBinding

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader

    private val reminderViewModel: ReminderViewModel by viewModels()

    private var selectedHour: Int? = null
    private var selectedMinute: Int? = null

    private var selectedDate = ""
    private var selectedTime = ""
    private var selectedFrequency = ""
    private var from = ""

    val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    val jsonDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val jsonDateParse = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_add_reminder, container, false)
            binding = FragmentAddReminderBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())
        setSpinnerAdapter()

        if (arguments?.getString("from") == "update") {
            from = arguments?.getString("from").toString()
            reminderId = arguments?.getString("reminderId").toString()

            binding.medicine.setText(arguments?.getString("medicineName").toString())
            val formattedDate: Date = jsonDateFormat.parse(arguments?.getString("reminderDate"))
            selectedDate = jsonDateParse.format(formattedDate)
            reminderDate = jsonDateFormat.format(formattedDate)
            binding.date.text = jsonDateParse.format(formattedDate)
            binding.time.text = arguments?.getString("reminderTime").toString()
            selectedTime = arguments?.getString("reminderTime").toString()
            reminderTime = arguments?.getString("reminderTime").toString()
            val reminderType = arguments?.getString("reminderType").toString()
            val reminderTypeArray =
                requireContext().resources.getStringArray(R.array.medicine_frequency)
            reminderTypeArray.forEachIndexed { index, data ->
                if (reminderType == data) {
                    binding.frequencySpinner.setSelection(index)
                }
            }
        }

    }

    private fun setSpinnerAdapter() {
        val frequencyAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.medicine_frequency,
            R.layout.spinner_text
        )

        frequencyAdapter.setDropDownViewResource(R.layout.spinner_text)
        binding.frequencySpinner.adapter = frequencyAdapter

        binding.frequencySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    selectedFrequency = p0?.getItemAtPosition(p2).toString()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
    }


    private fun setObserver() {
        reminderViewModel.addReminder.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.status == 200) {
                        findNavController().navigate(R.id.action_addReminderFragment_to_appointmentFragment)
                    } else {
                        showToast(response.data!!.message)
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
        reminderViewModel.updateReminder.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.success!!) {
                        findNavController().navigate(R.id.action_addReminderFragment_to_appointmentFragment)
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

    private fun setOnClickListener() {
        with(binding) {
            backArrow.setOnClickListener(this@AddReminderFragment)
            time.setOnClickListener(this@AddReminderFragment)
            date.setOnClickListener(this@AddReminderFragment)
            submit.setOnClickListener(this@AddReminderFragment)
        }
    }

    private fun checkAndAddReminder() {

        val title = binding.medicine.text.toString()
        val reminderType = selectedFrequency


        when {
            title.isEmpty() -> {
                showToast("Please Add Medicine")
            }
            reminderType.isEmpty() -> {
                showToast("Please Select Frequency")
            }
            reminderDate.isEmpty() -> {
                showToast("Please Select Reminder Date")
            }
            reminderTime.isEmpty() -> {
                showToast("Please Select Reminder Time")
            }
            else -> {
                val jsonObject = JsonObject()
                jsonObject.addProperty("id", session.patientId)
                jsonObject.addProperty("title", title)
                jsonObject.addProperty(
                    "date",
                    getDateFormatted(
                        "$selectedDate $reminderTime",
                        REMINDER_DATE_MONTH_PATTERN2,
                        DATE_FULL_PATTERN
                    ) + "Z"
                )
                jsonObject.addProperty("type", reminderType)
                jsonObject.addProperty("dutime", reminderTime)

                val dudate = JsonObject()
                dudate.addProperty("isRange", false)
                dudate.addProperty("dateRange", "null")

                val singleDate = JsonObject()
                singleDate.addProperty("date", "[Object]")
                singleDate.addProperty(
                    "jsDate",
                    getDateFormatted(
                        "$selectedDate $reminderTime",
                        REMINDER_DATE_MONTH_PATTERN2,
                        DATE_FULL_PATTERN
                    ) + ".000Z"
                )
                singleDate.addProperty(
                    "formatted",
                    jsonDateFormat.format(jsonDateParse.parse(selectedDate))
                )
                singleDate.addProperty("epoc", "1620757800")
                dudate.add("singleDate", Gson().toJsonTree(singleDate))
                jsonObject.add("dudate", Gson().toJsonTree(dudate))

                if (from == "update") {
                    reminderViewModel.updateReminder(reminderId, jsonObject)
                } else {
                    reminderViewModel.addReminder(jsonObject)
                }
            }
        }


    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backArrow -> {
                findNavController().popBackStack()
            }

            R.id.submit -> {
                checkAndAddReminder()
            }
            R.id.time -> {
                val hour = selectedHour ?: LocalDateTime.now().hour
                val minute = selectedMinute ?: LocalDateTime.now().minute
                MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(hour)
                    .setMinute(minute)
                    .build()
                    .apply {
                        addOnPositiveButtonClickListener {

                            onTimeSelected(this.hour, this.minute)

                        }
                    }.show(childFragmentManager, MaterialTimePicker::class.java.canonicalName)

            }
            R.id.date -> {
                val constraintsBuilder = CalendarConstraints.Builder()
                val dateValidator: CalendarConstraints.DateValidator =
                    DateValidatorPointForward.now()
                constraintsBuilder.setValidator(dateValidator)

                val datePicker: MaterialDatePicker<Long> = MaterialDatePicker
                    .Builder
                    .datePicker()
                    .setCalendarConstraints(constraintsBuilder.build())
                    .setTheme(R.style.MaterialCalendarTheme)
                    .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                    .setTitleText("Select date of birth")
                    .build()
                datePicker.show(childFragmentManager, "DATE_PICKER")

                datePicker.addOnPositiveButtonClickListener {
                    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val onlyDateFormat = SimpleDateFormat("dd", Locale.getDefault())
                    val date = sdf.format(it)

                    val stringDate: Date = sdf.parse(date)
                    val consultationDate = isoFormat.format(stringDate)
                    reminderDate = consultationDate.toString()
                    binding.date.text = date
                    selectedDate = date
                }
            }

        }
    }


    private fun onTimeSelected(hour: Int, minute: Int) {
        selectedHour = hour
        selectedMinute = minute
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val time = Time(hour, minute, 0)
        val dateObj = sdf.parse(time.toString())
        binding.time.text = SimpleDateFormat("hh:mm aaa", Locale.getDefault()).format(dateObj)
        reminderTime = binding.time.text.toString()
    }

}