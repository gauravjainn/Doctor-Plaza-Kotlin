package com.doctorsplaza.app.ui.patient.fragments.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentSearchBinding
import com.doctorsplaza.app.ui.patient.fragments.home.HomeViewModel
import com.doctorsplaza.app.ui.patient.fragments.home.adapter.OurSpecialistAdapter
import com.doctorsplaza.app.ui.patient.fragments.home.model.SpecialistData
import com.doctorsplaza.app.ui.patient.fragments.search.adapter.SearchDoctorsAdapter
import com.doctorsplaza.app.ui.patient.fragments.search.model.SearchData
import com.doctorsplaza.app.utils.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search), View.OnClickListener {
    private var searchKey: String = ""
    private lateinit var binding: FragmentSearchBinding

    @Inject
    lateinit var session: SessionManager

    private val homeViewModel: HomeViewModel by viewModels()

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader

    @Inject
    lateinit var specialistAdapter: OurSpecialistAdapter

    @Inject
    lateinit var searchDoctorsAdapter: SearchDoctorsAdapter

    lateinit var specialistData: List<SpecialistData>

    private var specialistShowed = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_search, container, false)
            binding = FragmentSearchBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
            setOnAdapterClickListener()
        }
        return currentView!!
    }


    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())

        searchKey = arguments?.getString("searchKey").toString()
        if (searchKey.isNotEmpty()) {
            binding.searchSpecialists.setText(searchKey)
            homeViewModel.search(searchKey)
        }
        homeViewModel.getOurSpecialists()

        binding.searchSpecialists.imeOptions = EditorInfo.IME_ACTION_DONE


    }


    private fun setObserver() {

        homeViewModel.ourSpecialists.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.status == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(), response.data.message)
                    } else {
                        if (response.data?.status == 200) {
                            specialistData = response.data.data
                            if (searchKey.isEmpty()) {
                                setSpecialists()
                            }
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

        homeViewModel.search.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.status == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(), response.data.message)
                    } else {
                        if (response.data?.data!!.isNotEmpty()) {
                            binding.loaderBg.isVisible = false
                            binding.noDataMsg.isVisible = false
                            setSearchRv(response.data.data)

                        } else {
                            binding.loaderBg.isVisible = true
                            binding.noDataMsg.isVisible = true
                        }
                    }
                }
                is Resource.Loading -> {
//                    appLoader.show()
                }

                is Resource.Error -> {
                    appLoader.dismiss()
                }
            }
        }
    }


    private fun setSpecialists() {
        binding.loaderBg.isVisible = false
        binding.noDataMsg.isVisible = false
        specialistAdapter.setAdapterSize("search")
        specialistAdapter.differ.submitList(specialistData)
        binding.specialistRv.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = specialistAdapter
        }

        if (!specialistShowed) {
            specialistShowed = true
            runAnimation()
        }

    }

    private fun setSearchRv(data: List<SearchData>) {
        searchDoctorsAdapter.differ.submitList(data)
        binding.specialistRv.apply {
            itemAnimator?.changeDuration = 0
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchDoctorsAdapter
        }
        searchDoctorsAdapter.notifyDataSetChanged()
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun setOnClickListener() {
        binding.backArrow.setOnClickListener(this)
        binding.closeIcon.setOnClickListener(this)
        binding.searchSpecialists.doOnTextChanged { text, _, _, _ ->
            binding.searchSpecialists.requestFocus()
            if (text!!.isEmpty() || text.length < 2) {

                if (this::specialistData.isInitialized) {
                    setSpecialists()
                }


            } else {
                if (text.length > 2) {
                    homeViewModel.search(text.toString().trim())
                }
            }
        }
    }

    private fun setOnAdapterClickListener() {
        searchDoctorsAdapter.setOnViewDoctors {
            findNavController().popBackStack()
            val bundle = Bundle()
            bundle.putString("doctorId", it._id)
            if (it.clinicData != null) {
                bundle.putString("clinicId", it.clinicData._id)
                bundle.putString("clinicName", it.clinicData.clinicName)
                bundle.putString("clinicAddress", it.clinicData.location)
                bundle.putString("clinicContact", it.clinicData.clinicContactNumber.toString())
            }
            findNavController().navigate(R.id.doctorDetailsFragment, bundle)
        }

        specialistAdapter.setOnViewDoctors {
            binding.searchSpecialists.setText(it.name)
            homeViewModel.search(it.name.trim())
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backArrow -> {

                findNavController().popBackStack()
            }
            R.id.closeIcon -> {
                binding.searchSpecialists.setText("")
                binding.searchSpecialists.clearFocus()
                hideKeyboard(requireActivity(), binding.searchSpecialists)
            }
        }
    }

    private fun runAnimation() {
        val context = binding.specialistRv.context
        val layoutAnimationController: LayoutAnimationController? =
            AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation)
        binding.specialistRv.layoutAnimation = layoutAnimationController
        binding.specialistRv.scheduleLayoutAnimation()
    }
}


