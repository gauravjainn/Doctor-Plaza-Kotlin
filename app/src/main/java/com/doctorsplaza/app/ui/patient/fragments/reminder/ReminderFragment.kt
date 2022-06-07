package com.doctorsplaza.app.ui.patient.fragments.reminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentReminderBinding
import com.doctorsplaza.app.ui.patient.fragments.reminder.adapter.MedicineReminderAdapter
import com.doctorsplaza.app.ui.patient.fragments.reminder.adapter.ReminderDateAdapter
import com.doctorsplaza.app.ui.patient.fragments.reminder.model.DateModel
import com.doctorsplaza.app.ui.patient.fragments.reminder.model.ReminderData
import com.doctorsplaza.app.utils.DoctorPlazaLoader
import com.doctorsplaza.app.utils.Resource
import com.doctorsplaza.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.internal.notify
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


@AndroidEntryPoint
class ReminderFragment : Fragment(R.layout.fragment_reminder), View.OnClickListener {

    private var reminderSelectedDate: String = ""
    private lateinit var binding: FragmentReminderBinding

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader

    @Inject
    lateinit var medicineReminderAdapter: MedicineReminderAdapter

    @Inject
    lateinit var reminderDateAdapter: ReminderDateAdapter

    private val reminderViewModel: ReminderViewModel by viewModels()

    var dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val selectedDateFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    var recyclerViewSetupDone = false

    private var dateList: MutableList<DateModel> = ArrayList()

    var reminderData: MutableList<ReminderData> = ArrayList()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_reminder, container, false)
            binding = FragmentReminderBinding.bind(currentView!!)
            init()
            setDateList()
            setObserver()
            setRecyclerView()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())

    }

    private fun setDateList() {
        for (i in 0 until 90) {
            val calendar: Calendar = GregorianCalendar()
            calendar.add(Calendar.DATE, i)
            val dateModel = DateModel(i, dateFormat.format(calendar.time).toString(), i == 0)
            dateList.add(dateModel)
        }
        reminderDateAdapter.differ.submitList(dateList)
        val selectedDate = dateFormat.parse(dateList[0].date)
        reminderSelectedDate = selectedDateFormat.format(selectedDate)
        reminderViewModel.getReminders(reminderSelectedDate)
    }


    private fun setObserver() {
        reminderViewModel.getReminders.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    binding.errorMessage.isVisible = false
                    if (response.data?.status == 200) {
                        if (response.data.data.isEmpty()) {
                            binding.noData.isVisible = true
                            binding.remindersRv.isVisible = false
                        } else {
                            binding.remindersRv.isVisible = true
                            binding.noData.isVisible = false
                            reminderData.clear()
                            reminderData.addAll(response.data.data)
                            setReminderRv()

                        }
                    } else {
                        binding.errorMessage.isVisible = true
                    }
                }
                is Resource.Loading -> {
                    appLoader.show()
                }
                is Resource.Error -> {
                    binding.errorMessage.isVisible = true
                    appLoader.dismiss()
                }
            }
        }
    }


    private fun setReminderRv() {
        medicineReminderAdapter.differ.submitList(reminderData)
        binding.remindersRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = medicineReminderAdapter
        }

    }

    private fun setRecyclerView() {
        binding.datesRv.apply {
            setHasFixedSize(true)
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = reminderDateAdapter
        }
        binding.datesRv.addOnScrollListener(dateScroller)
    }

    private val dateScroller = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val myLayoutManager: LinearLayoutManager =
                binding.datesRv.layoutManager as LinearLayoutManager
            val scrollPosition = myLayoutManager.findFirstVisibleItemPosition()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val monthYearFormat = SimpleDateFormat("MMM, yyyy", Locale.getDefault())
            val dateParsed = dateFormat.parse(dateList[scrollPosition + 1].date)
            val newMonthYear = monthYearFormat.format(dateParsed)
            binding.monthLbl.text = newMonthYear
        }
    }

    private fun setOnClickListener() {
        with(binding) {
            addMedicineReminder.setOnClickListener(this@ReminderFragment)
            monthLbl.setOnClickListener(this@ReminderFragment)
        }

        medicineReminderAdapter.setOnUpdateReminderClickListener {
            val bundle = Bundle()
            bundle.putString("from", "update")
            bundle.putString("reminderId", it._id)
            bundle.putString("medicineName", it.title)
            bundle.putString("reminderDate", it.dudate.singleDate.formatted)
            bundle.putString("reminderTime", "${it.dutime}")
            bundle.putString("reminderType", it.type)
            findNavController().navigate(R.id.addReminderFragment, bundle)
        }


        reminderDateAdapter.setOnDateSelected { dateId, position ->
            dateList.forEach {
                if (it.selected) {
                    it.selected = false
                }
            }

            dateList[position].selected = true

            val selectedDate = dateFormat.parse(dateList[position].date)
            reminderSelectedDate = selectedDateFormat.format(selectedDate)
            reminderDateAdapter.differ.submitList(dateList)
            reminderDateAdapter.notifyDataSetChanged()
            reminderViewModel.getReminders(reminderSelectedDate)
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.addMedicineReminder -> {
                findNavController().navigate(R.id.addReminderFragment)
            }


        }
    }

}