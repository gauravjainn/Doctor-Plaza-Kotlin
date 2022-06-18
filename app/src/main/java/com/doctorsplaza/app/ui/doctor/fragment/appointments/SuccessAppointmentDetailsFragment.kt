package com.doctorsplaza.app.ui.doctor.fragment.appointments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentSuccessAppointmentDetaiksBinding
import com.doctorsplaza.app.ui.doctor.fragment.appointments.model.PrescriptionData
import com.doctorsplaza.app.ui.doctor.fragment.prescription.adapter.MedicineAdapter
import com.doctorsplaza.app.utils.DoctorPlazaLoader
import com.doctorsplaza.app.utils.Resource
import com.doctorsplaza.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SuccessAppointmentDetailsFragment : Fragment(R.layout.fragment_success_appointment_detaiks),
    View.OnClickListener {

    private lateinit var binding: FragmentSuccessAppointmentDetaiksBinding
    private lateinit var prescriptionData: PrescriptionData
    private var appointmentId: String = ""
    private var clinicName: String = ""
    private var clinicLocation: String = ""

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader

    @Inject
    lateinit var medicineAdapter: MedicineAdapter

    private val doctorAppointmentViewModel: DoctorAppointmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView =
                inflater.inflate(R.layout.fragment_success_appointment_detaiks, container, false)
            binding = FragmentSuccessAppointmentDetaiksBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())
        binding.loader.isVisible = true
        appointmentId = arguments?.getString("appointId").toString()
        clinicName = arguments?.getString("clinicName").toString()
        clinicLocation = arguments?.getString("clinicLocation").toString()
        doctorAppointmentViewModel.getAppointmentPrescriptionDetails(appointmentId)

    }

    private fun setObserver() {
        doctorAppointmentViewModel.doctorAppointmentPrescription.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()

                    if (response.data?.code == 200) {
                        binding.loader.isVisible = false
                        if (response.data.data.isNotEmpty()) {
                            prescriptionData = response.data.data[0]
                            setPrescriptionData()
                        } else {

                        }
                    } else {
                        binding.loader.isVisible = true
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

    private fun setPrescriptionData() {
        with(binding) {
            appointmentId.text = prescriptionData.appointmentId
            mainComplaint.text = prescriptionData.complain
            setMedicineAdapter()
        }

    }

    private fun setMedicineAdapter() {
        medicineAdapter.differ.submitList(prescriptionData.medicine)
        binding.medicineRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = medicineAdapter
        }

        medicineAdapter.hideEditView(false)
    }

    private fun setOnClickListener() {
        binding.backArrow.setOnClickListener(this@SuccessAppointmentDetailsFragment)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backArrow -> {
                findNavController().popBackStack()
            }
        }
    }
}