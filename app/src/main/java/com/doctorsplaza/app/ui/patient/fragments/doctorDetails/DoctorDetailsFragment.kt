package com.doctorsplaza.app.ui.patient.fragments.doctorDetails

import android.app.Dialog
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentDoctorDetailsBinding
import com.doctorsplaza.app.ui.patient.fragments.addAppointmentForm.model.ClinicsData
import com.doctorsplaza.app.ui.patient.fragments.doctorDetails.model.Clinics
import com.doctorsplaza.app.ui.patient.fragments.doctorDetails.model.DoctorDetailsData
import com.doctorsplaza.app.utils.*
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DoctorDetailsFragment : Fragment(R.layout.fragment_doctor_details), View.OnClickListener {

    private lateinit var clinicData: Clinics
    private var clinicAddress: String = ""
    private var clinicName: String = ""

    private var clinicContact: String = ""

    private var clinicId: String = ""

    private var doctorId: String = ""

    private lateinit var binding: FragmentDoctorDetailsBinding

    private val doctorDetailsViewModel: DoctorDetailsViewModel by viewModels()

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader

    private var appointmentType = "Offline"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_doctor_details, container, false)
            binding = FragmentDoctorDetailsBinding.bind(currentView!!)
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
        doctorDetailsViewModel.getDoctor(doctorId)

    }


    private fun setObserver() {
        doctorDetailsViewModel.doctor.observe(viewLifecycleOwner) { response ->
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
                            setDoctorData(response.data.data[0])

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
                    showToast(response.message.toString())
                }
            }
        }
    }

    private fun setDoctorData(data: DoctorDetailsData) {
        with(binding) {
            Glide.with(requireContext()).load(data.profile_picture).into(doctorImage)
            doctorName.text = data.doctorName
            doctorSpecialistIn.text = data.specialization
            doctorLocation.text = data.address
            doctorDegree.text = data.qualification
            verifiedIcon.isVisible = data.is_approved
            verifiedViewGrp.isVisible = data.is_approved
            about.text = data.about
            setClinicsSpinner(data.clinics)
        }
    }

    private fun setClinicsSpinner(data: List<Clinics>) {
        val clinicAdapter = ArrayAdapter(requireContext(), R.layout.spinner_text, data)
        clinicAdapter.setDropDownViewResource(R.layout.spinner_text)
        binding.clinicSpinner.adapter = clinicAdapter
        binding.clinicSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                clinicData = binding.clinicSpinner.selectedItem as Clinics
            }
        }
    }

    private fun setOnClickListener() {
        with(binding) {
            backArrow.setOnClickListener(this@DoctorDetailsFragment)
            bookAppointment.setOnClickListener(this@DoctorDetailsFragment)
            inClinicRadioBtn.setOnClickListener(this@DoctorDetailsFragment)
            onlineRadioBtn.setOnClickListener(this@DoctorDetailsFragment)
            clinicConsultView.setOnClickListener(this@DoctorDetailsFragment)
            videoConsultView.setOnClickListener(this@DoctorDetailsFragment)
        }
    }

    private fun showAddRequiredFieldsPopUp() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.update_profile_details)
        val updateBtn = dialog.findViewById<View>(R.id.updateBtn) as MaterialButton
        val cancelBtn = dialog.findViewById<View>(R.id.cancelBtn) as MaterialButton
        updateBtn.setOnClickListener {
            findNavController().navigate(R.id.action_doctorDetailsFragment_to_editProfileFragment)
            dialog.dismiss()
        }
        cancelBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()

        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val displayWidth = displayMetrics.widthPixels
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window!!.attributes)
        val dialogWindowWidth = (displayWidth * 0.9f).toInt()
        layoutParams.width = dialogWindowWidth
        dialog.window!!.attributes = layoutParams
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backArrow -> {
                findNavController().popBackStack()
            }
            R.id.bookAppointment -> {

                if (session.loginName.isEmpty() || session.loginDOB.isEmpty()) {
                    showAddRequiredFieldsPopUp()
                } else {
                    val bundle = Bundle()
                    bundle.putString("doctorId", doctorId)
                    bundle.putString("clinicId", clinicData._id)
                    bundle.putString("clinicName", clinicData.clinicName)
                    bundle.putString("clinicContact", clinicData.clinicContactNumber)
                    bundle.putString("clinicAddress", clinicData.location)
                    bundle.putString("appointmentType", appointmentType)
                    findNavController().navigate(R.id.bookAppointmentFragment, bundle)
                }
            }

            R.id.inClinicRadioBtn -> {
                binding.inClinicRadioBtn.isChecked = true
                binding.onlineRadioBtn.isChecked = false
                appointmentType = "Offline"
            }

            R.id.onlineRadioBtn -> {
                binding.inClinicRadioBtn.isChecked = false
                binding.onlineRadioBtn.isChecked = true
                appointmentType = "Online"
            }

            R.id.clinicConsultView -> {
                binding.inClinicRadioBtn.isChecked = true
                binding.onlineRadioBtn.isChecked = false
                appointmentType = "Offline"
            }

            R.id.videoConsultView -> {
                binding.inClinicRadioBtn.isChecked = false
                binding.onlineRadioBtn.isChecked = true
                appointmentType = "Online"
            }
        }
    }


}
