package com.doctorsplaza.app.ui.patient.fragments.clinics

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
import com.doctorsplaza.app.databinding.FragmentClinicBinding
import com.doctorsplaza.app.ui.patient.fragments.clinics.adapter.ClinicsAdapter
import com.doctorsplaza.app.ui.patient.fragments.clinics.model.ClinicData
import com.doctorsplaza.app.utils.DoctorPlazaLoader
import com.doctorsplaza.app.utils.Resource
import com.doctorsplaza.app.utils.showToast
import com.doctorsplaza.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ClinicFragment : Fragment(R.layout.fragment_clinic) , View.OnClickListener {

    private lateinit var binding: FragmentClinicBinding

    @Inject
    lateinit var session: SessionManager

    private val clinicViewModel: ClinicViewModel by viewModels()

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader

    @Inject
    lateinit var clinicsAdapter: ClinicsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_clinic, container, false)
            binding = FragmentClinicBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())
        clinicViewModel.getClinics()
    }


    private fun setObserver() {
        clinicViewModel.clinics.observe(viewLifecycleOwner){response->
            when(response){
            is Resource.Success->{
                appLoader.dismiss()
                binding.loading.isVisible = false
                if(response.data!!.status==200){
                    if(response.data.data.isNotEmpty()){
                        setClinicsData(response.data.data)
                    }else{
                        binding.noData.isVisible = true
                    }
                }
            }
            is Resource.Loading->{
                binding.loading.isVisible = true
                appLoader.show()

            }
            is Resource.Error->{
                appLoader.dismiss()
                binding.errorMessage.isVisible = true
                requireContext().showToast(response.message.toString())
            }

            }
        }

    }

    private fun setClinicsData(clinicData: List<ClinicData>) {
        clinicsAdapter.differ.submitList(clinicData)
        binding.clinicRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = clinicsAdapter
        }
    }


    private fun setOnClickListener() {

        clinicsAdapter.setOnViewDoctors {
            val bundle = Bundle().apply {
                putString("clinicId",it._id)
                putString("clinicName",it.clinicName)
                putString("clinicAddress",it.location)
                putString("clinicContact",it.clinicContactNumber.toString())
            }
            findNavController().navigate(R.id.ourDoctorsFragment,bundle)
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }
}