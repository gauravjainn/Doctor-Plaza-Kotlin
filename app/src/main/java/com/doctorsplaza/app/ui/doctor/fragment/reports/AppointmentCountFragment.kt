package com.doctorsplaza.app.ui.doctor.fragment.reports

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentAppointmentCountBinding
import com.doctorsplaza.app.ui.doctor.fragment.reports.model.AppointmentsCountModel
import com.doctorsplaza.app.ui.doctor.fragment.reports.viewModel.RevenueViewModel
import com.doctorsplaza.app.utils.DoctorPlazaLoader
import com.doctorsplaza.app.utils.Resource
import com.doctorsplaza.app.utils.SessionManager
import com.doctorsplaza.app.utils.showToast
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class AppointmentCountFragment : Fragment(R.layout.fragment_appointment_count),
    View.OnClickListener {
    private lateinit var binding: FragmentAppointmentCountBinding

    private val revenueViewModel: RevenueViewModel by viewModels()

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_appointment_count, container, false)
            binding = FragmentAppointmentCountBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())
        binding.loader.isVisible = true
        val jsonObject = JsonObject()
        jsonObject.addProperty("id", session.loginId)
        revenueViewModel.getAppointmentsReports(jsonObject)
    }

    private fun setChartData(data: AppointmentsCountModel) {

        val color = ArrayList<Int>()
        color.add(ContextCompat.getColor(requireContext(), R.color.chartColorOne))
        color.add(ContextCompat.getColor(requireContext(), R.color.chartColorTwo))
        color.add(ContextCompat.getColor(requireContext(), R.color.chartColorThree))

        /**
         *  Attended Chart Data
         */
        val appointmentAttendedEntries = ArrayList<PieEntry>()
        if (
            data.weekdonecount == 0 &&
            data.monthdonecount == 0 &&
            data.yeardonecount == 0
        ) {
            appointmentAttendedEntries.add(PieEntry(1F, ""))
            appointmentAttendedEntries.add(PieEntry(1F, ""))
            appointmentAttendedEntries.add(PieEntry(1F, ""))
        } else {
            appointmentAttendedEntries.add(PieEntry(data.weekdonecount.toFloat(), ""))
            appointmentAttendedEntries.add(PieEntry(data.monthdonecount.toFloat(), ""))
            appointmentAttendedEntries.add(PieEntry(data.yeardonecount.toFloat(), ""))
        }


        val appointmentAttendedPieData = PieDataSet(appointmentAttendedEntries, "")
        appointmentAttendedPieData.colors = color
        appointmentAttendedPieData.setDrawValues(false)

        binding.revenuePieChart.animateXY(500, 500)
        binding.revenuePieChart.setDrawEntryLabels(false)
        binding.revenuePieChart.legend.isEnabled = false
        binding.revenuePieChart.setDrawMarkers(false)
        binding.revenuePieChart.description.isEnabled = false
        binding.revenuePieChart.holeRadius = 65f
        binding.revenuePieChart.transparentCircleRadius = 65f
        binding.revenuePieChart.invalidate()

        binding.revenuePieChart.data = PieData(appointmentAttendedPieData)

        binding.weeeklyAppointment.text = "Weekly   ${data.weekdonecount.toDouble().roundToInt()}"
        binding.monthlyAppointment.text = "Monthly  ${data.monthdonecount.toDouble().roundToInt()}"
        binding.yearlyAppointment.text = "Yearly   ${data.yeardonecount.toDouble().roundToInt()}"

        binding.appointmentsCount.text = "${data.monthdonecount.toDouble().roundToInt()}"

        /**
         *  Reschedule Chart Data
         */

        val rescheduledEntries = ArrayList<PieEntry>()
        if (
            data.weekrescount == 0 &&
            data.monthrescount == 0 &&
            data.monthrescount == 0
        ) {
            rescheduledEntries.add(PieEntry(1F, ""))
            rescheduledEntries.add(PieEntry(1F, ""))
            rescheduledEntries.add(PieEntry(1F, ""))
        } else {
            rescheduledEntries.add(PieEntry(data.weekrescount.toFloat(), ""))
            rescheduledEntries.add(PieEntry(data.monthrescount.toFloat(), ""))
            rescheduledEntries.add(PieEntry(data.monthrescount.toFloat(), ""))
        }


        val rescheduledPieData = PieDataSet(rescheduledEntries, "")
        rescheduledPieData.colors = color
        rescheduledPieData.setDrawValues(false)

        binding.reschedulePieChart.animateXY(500, 500)
        binding.reschedulePieChart.setDrawEntryLabels(false)
        binding.reschedulePieChart.legend.isEnabled = false
        binding.reschedulePieChart.setDrawMarkers(false)
        binding.reschedulePieChart.description.isEnabled = false
        binding.reschedulePieChart.holeRadius = 65f
        binding.reschedulePieChart.transparentCircleRadius = 65f
        binding.reschedulePieChart.invalidate()

        binding.reschedulePieChart.data = PieData(rescheduledPieData)

        binding.reWeeeklyAppointment.text = "Weekly   ${data.weekrescount.toDouble().roundToInt()}"
        binding.reMonthlyAppointment.text = "Monthly  ${data.monthrescount.toDouble().roundToInt()}"
        binding.reYearlyAppointment.text = "Yearly   ${data.yearrescount.toDouble().roundToInt()}"

        binding.rescheduleCount.text = "${data.monthrescount.toDouble().roundToInt()}"
        /**
         *  Reschedule Chart Data
         */

        val canceledEntries = ArrayList<PieEntry>()

        if (
            data.weekcancelcount == 0 &&
            data.monthcancelcount == 0 &&
            data.yearcancelcount == 0
        ) {
            canceledEntries.add(PieEntry(1F, ""))
            canceledEntries.add(PieEntry(1F, ""))
            canceledEntries.add(PieEntry(1F, ""))
        } else {
            canceledEntries.add(PieEntry(data.weekcancelcount.toFloat(), ""))
            canceledEntries.add(PieEntry(data.monthcancelcount.toFloat(), ""))
            canceledEntries.add(PieEntry(data.yearcancelcount.toFloat(), ""))
        }


        val canceledPieData = PieDataSet(canceledEntries, "")
        canceledPieData.colors = color
        canceledPieData.setDrawValues(false)

        binding.cancelPieChart.animateXY(500, 500)
        binding.cancelPieChart.setDrawEntryLabels(false)
        binding.cancelPieChart.legend.isEnabled = false
        binding.cancelPieChart.setDrawMarkers(false)
        binding.cancelPieChart.description.isEnabled = false
        binding.cancelPieChart.holeRadius = 65f
        binding.cancelPieChart.transparentCircleRadius = 65f
        binding.cancelPieChart.invalidate()

        binding.cancelPieChart.data = PieData(canceledPieData)

        binding.cancelWeeeklyAppointment.text =
            "Weekly   ${data.weekcancelcount.toDouble().roundToInt()}"
        binding.cancelMonthlyAppointment.text =
            "Monthly  ${data.monthcancelcount.toDouble().roundToInt()}"
        binding.cancelYearlyAppointment.text =
            "Yearly   ${data.yearcancelcount.toDouble().roundToInt()}"
        binding.cancelCount.text = "${data.monthcancelcount.toDouble().roundToInt()}"
    }

    private fun setObserver() {
        revenueViewModel.appointmentReports.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    binding.loader.isVisible = false
                    setChartData(response.data!!)
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

    private fun setOnClickListener() {
        with(binding) {

        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }
}