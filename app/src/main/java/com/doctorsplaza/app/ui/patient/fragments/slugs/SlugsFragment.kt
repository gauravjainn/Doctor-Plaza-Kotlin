package com.doctorsplaza.app.ui.patient.fragments.slugs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentSlugsBinding
import com.doctorsplaza.app.ui.patient.commonModel.CommonViewModel
import com.doctorsplaza.app.ui.patient.fragments.slugs.model.SlugsData
import com.doctorsplaza.app.utils.DoctorPlazaLoader
import com.doctorsplaza.app.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SlugsFragment : Fragment(R.layout.fragment_slugs), View.OnClickListener {

    private lateinit var binding: FragmentSlugsBinding

    private var currentView: View? = null

    private val commonViewModel: CommonViewModel by viewModels()

    private lateinit var appProgress: DoctorPlazaLoader

    private var title = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_slugs, container, false)
            binding = FragmentSlugsBinding.bind(currentView!!)
            init()
            setOnClickListener()
            setObserver()
        }
        return currentView!!
    }

    private fun init() {
        appProgress = DoctorPlazaLoader(requireContext())
        title = arguments?.getString("title").toString()
        binding.appBarTitle.text = title
    }

    private fun setObserver() {
        commonViewModel.getSlugs()
        commonViewModel.slugs.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appProgress.dismiss()
                    if (response.data!!.success) {
                        binding.errorMessage.isVisible = false
                        setData(response.data.data)
                    } else {
                        binding.errorMessage.isVisible = true
                    }
                }
                is Resource.Loading -> {
                    appProgress.show()
                }
                is Resource.Error -> {
                    binding.errorMessage.isVisible = true
                    appProgress.dismiss()
                }
            }
        }

    }

    private fun setData(data: List<SlugsData>) {
        for (cms in data) {
            if (cms.title == title) {
                binding.pageDetails.text =
                    HtmlCompat.fromHtml(cms.content, HtmlCompat.FROM_HTML_MODE_LEGACY)
            }
        }
    }


    private fun setOnClickListener() {
        with(binding) {
            backArrow.setOnClickListener(this@SlugsFragment)
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