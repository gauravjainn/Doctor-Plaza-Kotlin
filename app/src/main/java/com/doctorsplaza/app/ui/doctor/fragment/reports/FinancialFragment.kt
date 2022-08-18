package com.doctorsplaza.app.ui.doctor.fragment.reports

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentFinancialBinding
import com.doctorsplaza.app.ui.doctor.fragment.reports.model.FinancialModel
import com.doctorsplaza.app.ui.doctor.fragment.reports.viewModel.RevenueViewModel
import com.doctorsplaza.app.utils.*
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FinancialFragment : Fragment(R.layout.fragment_financial) {

    private lateinit var binding: FragmentFinancialBinding

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
            currentView = inflater.inflate(R.layout.fragment_financial, container, false)
            binding = FragmentFinancialBinding.bind(currentView!!)
            init()
            setObserver()
        }
        return currentView!!
    }


    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())
        binding.loader.isVisible = true
        val jsonObject = JsonObject()
        jsonObject.addProperty("id", session.loginId)
        revenueViewModel.getFinancialReports(jsonObject)
    }

    @SuppressLint("SetTextI18n")
    private fun setChartData(data: FinancialModel) {
        val entries = ArrayList<PieEntry>()
        if (
            data.onlinepayment == 0 &&
            data.totalamount == 0 &&
            data.cashpayment == 0
        ) {
            entries.add(PieEntry(1F, ""))
            entries.add(PieEntry(1F, ""))
            entries.add(PieEntry(1F, ""))
        } else {
            entries.add(PieEntry(data.weekcancelcount.toFloat(), ""))
            entries.add(PieEntry(data.monthcancelcount.toFloat(), ""))
            entries.add(PieEntry(data.yearcancelcount.toFloat(), ""))
        }


        val pieData = PieDataSet(entries, "")

        val color = ArrayList<Int>()
        color.add(ContextCompat.getColor(requireContext(), R.color.chartColorOne))
        color.add(ContextCompat.getColor(requireContext(), R.color.chartColorTwo))
        color.add(ContextCompat.getColor(requireContext(), R.color.chartColorThree))

        pieData.colors = color
        pieData.setDrawValues(false)

        binding.revenuePieChart.animateXY(500, 500)
        binding.revenuePieChart.setDrawEntryLabels(false)
        binding.revenuePieChart.legend.isEnabled = false
        binding.revenuePieChart.setDrawMarkers(false)
        binding.revenuePieChart.description.isEnabled = false
        binding.revenuePieChart.holeRadius = 65f
        binding.revenuePieChart.transparentCircleRadius = 65f
        binding.revenuePieChart.invalidate()

        binding.revenuePieChart.data = PieData(pieData)

        binding.totalAppointment.text = "Total   ${data.totalamount}"
        binding.cashAppointment.text = "Cash    ${data.cashpayment}"
        binding.onlineAppointment.text = "Online  ${data.onlinepayment}"
        binding.revenueCount.text = data.onlinepayment.toString()


    }

    private fun setObserver() {
        revenueViewModel.financialReports.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.loader.isVisible = false
                    appLoader.dismiss()
                    if (response.data?.status == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(),response.data.message.toString())
                    } else {
                        setChartData(response.data!!)
                    }

                }
                is Resource.Loading -> {
                    appLoader.show()
                }
                is Resource.Error -> {
                    appLoader.dismiss()
                    showToast(response.message.toString())
                }
            }
        }
    }
}