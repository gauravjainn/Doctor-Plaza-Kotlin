package com.doctorsplaza.app.ui.doctor.fragment.prescription

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentAddPrescriptionWithMedicineBinding
import com.doctorsplaza.app.ui.doctor.fragment.appointments.DoctorAppointmentViewModel
import com.doctorsplaza.app.ui.doctor.fragment.appointments.model.PrescriptionData
import com.doctorsplaza.app.ui.doctor.fragment.prescription.adapter.MedicineAdapter
import com.doctorsplaza.app.ui.doctor.fragment.prescription.model.Medicine
import com.doctorsplaza.app.ui.doctor.fragment.prescription.model.MedicineModel
import com.doctorsplaza.app.ui.patient.fragments.appointments.model.AppointmentData
import com.doctorsplaza.app.utils.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.processor.internal.definecomponent.codegen._dagger_hilt_android_internal_builders_FragmentComponentBuilder
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class AddPrescriptionWithMedicineFragment :
    Fragment(R.layout.fragment_add_prescription_with_medicine), View.OnClickListener {

    private var from: String = ""
    private var editMedicinePosition: Int? = null
    private var appointmentId: String? = ""
    private var appointmentDateTime: String? = ""
    private var consultationFee: String? = ""

    private lateinit var binding: FragmentAddPrescriptionWithMedicineBinding

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader


    @Inject
    lateinit var medicineAdapter: MedicineAdapter

    private val addPrescriptionViewModel: AddPrescriptionViewModel by viewModels()
    private val doctorAppointmentViewModel: DoctorAppointmentViewModel by viewModels()

    private var prescriptionId = ""
    private var medicines: MutableList<Medicine> = ArrayList()
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView =
                inflater.inflate(R.layout.fragment_add_prescription_with_medicine, container, false)
            binding = FragmentAddPrescriptionWithMedicineBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())
        appointmentId = arguments?.getString("appointmentId")
        appointmentDateTime = arguments?.getString("appointmentTime")
        consultationFee = arguments?.getString("consultationFee")
        from = arguments?.getString("from").toString()


        with(binding) {
            patientName.text = arguments?.getString("patientName").toString()
            patientId.text = "Patient Id: ${arguments?.getString("patientId").toString()}"
            consultationAmt.text = "₹${consultationFee}"
            appointmentTime.text = appointmentDateTime
        }

        val prescriptionAdded = arguments?.getString("prescriptionAdded")
        if (prescriptionAdded == "yes") {
            binding.disease.isEnabled = false
            binding.mainComplaint.isEnabled = false
            binding.alimentDiagnosed.isEnabled = false
            binding.instructions.isEnabled = false

            binding.loader.isVisible = true
            doctorAppointmentViewModel.getAppointmentPrescriptionDetails(appointmentId.toString())
            binding.saveBtn.isVisible = false
            binding.downloadBtn.isVisible = true
        } else {
            binding.saveBtn.isVisible = true
            binding.downloadBtn.isVisible = false
        }
    }

    private fun setObserver() {
        addMedicine.observe(requireActivity()) {
            if (editMedicinePosition != null) {
                medicines.removeAt(editMedicinePosition!!)
            }
            medicines.addAll(listOf(it))
            setMedicineRv()
        }

        doctorAppointmentViewModel.doctorAppointmentPrescription.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.loader.isVisible = false
                    appLoader.dismiss()
                    if (response.data?.code == 200) {
                        setPrescriptionData(response.data.data)
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

        addPrescriptionViewModel.addPrescription.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.success!!) {
                        val bundle = Bundle().apply {
                            putString("appointId",appointmentId)
                        }
                        findNavController().navigate(R.id.action_addPrescriptionWithMedicineFragment_to_doctorAppointmentDetailsFragment,bundle)
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

        doctorAppointmentViewModel.getPrescriptionPdfUrl.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.code == 200) {
                        if (response.data.perscription.isNotEmpty()) {
                            val bundle = Bundle().apply {
                                putString("prescription", response.data.perscription)
                            }
                            if (from == "patient") {
                                findNavController().navigate(R.id.prescriptionFragment, bundle)
                            } else {
                                findNavController().navigate(R.id.prescriptionFragment2, bundle)

                            }
                        } else {
                            showToast("There is no pdf available")
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


    }


    private fun setMedicineRv() {
        medicineAdapter.differ.submitList(medicines)
        binding.medicineRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = medicineAdapter
        }

        medicineAdapter.notifyDataSetChanged()
    }

    private fun setPrescriptionData(data: List<PrescriptionData>) {
        with(binding) {
            data.forEach {
                if (it.type.lowercase() == "pdf") {
                    Glide.with(requireContext()).applyDefaultRequestOptions(patientRequestOption())
                        .load("")

                    prescriptionId = "Patient ID:${it._id}"
                    patientName.text = it.patientname
                    patientId.text = it.patientId
                    appointmentTime.text = appointmentDateTime
                    consultationAmt.text = "₹ $consultationFee"
                    disease.setText(it.disease)
                    mainComplaint.setText(it.complain)
                    alimentDiagnosed.setText(it.ailment_diagnosed)
                    instructions.setText(it.instructions)
                    prescriptionNote.setText(it.patient_note)
                    medicines = it.medicine as MutableList<Medicine>
                    setMedicineRv()
                    binding.addMedicineLbl.isVisible = false
                    medicineAdapter.hideEditView(false)
                }
            }
        }
    }

    private fun setOnClickListener() {
        with(binding) {
            addMedicineLbl.setOnClickListener(this@AddPrescriptionWithMedicineFragment)
            downloadBtn.setOnClickListener(this@AddPrescriptionWithMedicineFragment)
            backArrow.setOnClickListener(this@AddPrescriptionWithMedicineFragment)
            saveBtn.setOnClickListener(this@AddPrescriptionWithMedicineFragment)
        }

        medicineAdapter.setOnEditClickListener { data, index ->
            editMedicinePosition = index
            val bundle = Bundle().apply {
                putSerializable("data", data)
            }
            findNavController().navigate(R.id.addMedicineFragment, bundle)
        }
    }

    private fun validateAndAddPrescription() {
        val disease = binding.disease.text.toString()
        val mainComplaint = binding.mainComplaint.text.toString()
        val alimentDiagnosed = binding.alimentDiagnosed.text.toString()
        val instructions = binding.instructions.text.toString()
        val prescriptionNote = binding.prescriptionNote.text.toString()

        val currentDate = isoFormat.format(Date())
        val prescriptionModel = MedicineModel(
            alimentDiagnosed,
            appointmentId.toString(),
            mainComplaint,
            currentDate,
            disease,
            instructions,
            medicines,
            prescriptionNote,
            "",
            false
        )
        val gson = Gson()
        val request = gson.toJson(prescriptionModel)
        var prescriptionJson: JsonObject? = null
        try {
            prescriptionJson = JsonParser().parse(request) as JsonObject
            println()
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        when {
            disease.isEmpty() -> {
                showToast("please enter disease")
            }
            mainComplaint.isEmpty() -> {
                showToast("please enter main complaint")
            }
            alimentDiagnosed.isEmpty() -> {
                showToast("please enter aliment diagnosed")
            }
            instructions.isEmpty() -> {
                showToast("please enter instructions")
            }
            prescriptionNote.isEmpty() -> {
                showToast("please enter prescription note")
            }
            else -> {
                addPrescriptionViewModel.addPrescription(jsonObject = prescriptionJson!!)
            }
        }
    }

    private fun getPrescriptionPdfUrl() {

        doctorAppointmentViewModel.getPrescriptionPdfUrl(prescriptionId)


    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backArrow -> {
                findNavController().popBackStack()
            }
            R.id.saveBtn -> {
                validateAndAddPrescription()
            }

            R.id.downloadBtn -> {
                getPrescriptionPdfUrl()
            }

            R.id.addMedicineLbl -> {
                findNavController().navigate(R.id.addMedicineFragment)
            }
        }
    }


    override fun onResume() {
        super.onResume()
        setObserver()
    }
}