package com.doctorsplaza.app.ui.doctor.fragment.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentDoctoHomeBinding
import com.doctorsplaza.app.databinding.FragmentHomeBinding
import com.doctorsplaza.app.ui.patient.fragments.appointments.AppointmentViewModel
import com.doctorsplaza.app.ui.patient.fragments.bookAppointment.adapter.BookTimeAdapter
import com.doctorsplaza.app.ui.patient.fragments.home.HomeViewModel
import com.doctorsplaza.app.utils.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DoctorHomeFragment : Fragment(R.layout.fragment_docto_home), View.OnClickListener {
    private lateinit var binding: FragmentDoctoHomeBinding

    private val homeViewModel: HomeViewModel by viewModels()

    private val appointmentViewModel: AppointmentViewModel by viewModels()

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var doctorPlazaLoader: DoctorPlazaLoader


    @Inject
    lateinit var bookTimeAdapter: BookTimeAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_docto_home, container, false)
            binding = FragmentDoctoHomeBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
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
                    }
                }
                is Resource.Loading -> {
                }
                is Resource.Error -> {
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

            R.id.specialistsViewAll -> {
                findNavController().navigate(R.id.searchFragment)
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


    override fun onResume() {
        super.onResume()
        setObserver()
        homeViewModel.getAppointments()
    }
}
