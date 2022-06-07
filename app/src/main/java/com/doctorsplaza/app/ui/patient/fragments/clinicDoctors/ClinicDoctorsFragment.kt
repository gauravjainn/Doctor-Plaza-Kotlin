package com.doctorsplaza.app.ui.patient.fragments.clinicDoctors

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentOurDoctorsBinding
import com.doctorsplaza.app.ui.patient.fragments.clinicDoctors.adapter.ClinicDoctorsAdapter
import com.doctorsplaza.app.ui.patient.fragments.clinicDoctors.model.DoctorData
import com.doctorsplaza.app.utils.DoctorPlazaLoader
import com.doctorsplaza.app.utils.Resource
import com.doctorsplaza.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ClinicDoctorsFragment : Fragment(R.layout.fragment_our_doctors), View.OnClickListener {

    private var clinicContact: String = ""

    private var clinicAddress: String = ""

    private var clinicName: String = ""

    private var clinicId: String =""

    private lateinit var binding: FragmentOurDoctorsBinding

    @Inject
    lateinit var session: SessionManager

    private val ourDoctorsViewModel: ClinicDoctorsViewModel by viewModels()

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader


    @Inject
    lateinit var clinicDoctorsAdapter: ClinicDoctorsAdapter

    private var doctorData: MutableList<DoctorData> = ArrayList()

    var pageNo = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_our_doctors, container, false)
            binding = FragmentOurDoctorsBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())

         clinicId = arguments?.getString("clinicId").toString()
         clinicName = arguments?.getString("clinicName").toString()
         clinicAddress = arguments?.getString("clinicAddress").toString()
         clinicContact = arguments?.getString("clinicContact").toString()

        if (arguments?.getString("from").toString() == "home") {
            ourDoctorsViewModel.getAllDoctors(pageNo.toString())
        } else {
            ourDoctorsViewModel.getDoctorsByClinic(clinicId)
        }
    }


    private fun setObserver() {
        ourDoctorsViewModel.doctors.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data!!.success) {
                        if (response.data.data.isEmpty()) {
                            binding.loading.isVisible = true
                            binding.noData.isVisible = true
                        } else {
                            response.data.data.forEach {
                                doctorData.addAll(it.doctorData)
                            }
                            binding.loading.isVisible = false
                            setDoctors(doctorData)
                        }
                    } else {
                        binding.loading.isVisible = true
                        binding.errorMessage.isVisible = true
                    }
                }
                is Resource.Loading -> {
                    binding.loading.isVisible = true
                    appLoader.show()
                }
                is Resource.Error -> {
                    binding.loading.isVisible = true
                    binding.errorMessage.isVisible = true
                    appLoader.dismiss()
                }
            }
        }

        ourDoctorsViewModel.allDoctorsDoctors.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data!!.code == 200) {
                        binding.moreLoader.isVisible = false
                        if (pageNo<2&&response.data.data.isEmpty()) {
                            binding.loading.isVisible = true
                            binding.noData.isVisible = true
                        } else {
                            if (doctorData.size == response.data.total) {
                                isLoading = true
                            } else {
                                isLoading = false
                                binding.loading.isVisible = false
                                doctorData.addAll(response.data.data)
                                if (pageNo > 1) {
                                    clinicDoctorsAdapter.differ.submitList(doctorData)
                                    clinicDoctorsAdapter.notifyDataSetChanged()

                                } else {
                                    setDoctors(doctorData)
                                }
                            }
                        }
                    } else {
                        binding.loading.isVisible = true
                        binding.errorMessage.isVisible = true
                    }
                }
                is Resource.Loading -> {
                    if(pageNo<2){
                        binding.loading.isVisible = true
                        appLoader.show()
                    }else{
                        binding.moreLoader.isVisible = true
                    }
                }
                is Resource.Error -> {
                    if(pageNo<2){
                        binding.loading.isVisible = true
                        binding.errorMessage.isVisible = true
                        appLoader.dismiss()
                    }else{
                        binding.moreLoader.isVisible = false
                    }

                }
            }
        }
    }

    private fun setDoctors(data: List<DoctorData>) {
        clinicDoctorsAdapter.differ.submitList(data)
        binding.doctorsRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = clinicDoctorsAdapter
        }.addOnScrollListener(scrollListener)
    }

    var isLoading = false
    var isScrolling = false


    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            if (dy > 0) {

                val recycleLayoutManager = binding.doctorsRv.layoutManager as LinearLayoutManager
                if (!isLoading) {
                    if (recycleLayoutManager != null && recycleLayoutManager.findLastCompletelyVisibleItemPosition() == clinicDoctorsAdapter.itemCount - 2) {
                        pageNo++
                        if (arguments?.getString("from").toString() == "home") {
                            ourDoctorsViewModel.getAllDoctors(pageNo.toString())
                        } else {
//                            ourDoctorsViewModel.getDoctorsByClinic(clinicId)
                        }
                        isLoading = true
                    }
                }
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }

    private fun setOnClickListener() {
        binding.backArrow.setOnClickListener(this)
        clinicDoctorsAdapter.setOnViewDoctors {
            val bundle = Bundle()
            bundle.putString("doctorId", it._id)
            bundle.putString("clinicId", clinicId)
            bundle.putString("clinicName", clinicName)
            bundle.putString("clinicAddress",clinicAddress)
            bundle.putString("clinicContact",clinicContact)
            findNavController().navigate(R.id.doctorDetailsFragment, bundle)
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backArrow -> {
                findNavController().popBackStack()
            }
        }
    }
}