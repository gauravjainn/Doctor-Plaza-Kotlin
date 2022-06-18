package com.doctorsplaza.app.ui.doctor.fragment.prescription

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentAddBasicPrescriptionBinding
import com.doctorsplaza.app.utils.DoctorPlazaLoader
import com.doctorsplaza.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddBasicPrescriptionFragment : Fragment(R.layout.fragment_add_basic_prescription),
    View.OnClickListener {
    private lateinit var binding: FragmentAddBasicPrescriptionBinding

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader

    private val addPrescriptionViewModel: AddPrescriptionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView =
                inflater.inflate(R.layout.fragment_add_basic_prescription, container, false)
            binding = FragmentAddBasicPrescriptionBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())

    }

    private fun setObserver() {

    }


    private fun setOnClickListener() {
        with(binding) {
            saveBtn.setOnClickListener(this@AddBasicPrescriptionFragment)
            backArrow.setOnClickListener(this@AddBasicPrescriptionFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backArrow -> {
                findNavController().popBackStack()
            }
            R.id.saveBtn -> {
                findNavController().navigate(R.id.addPrescriptionWithMedicineFragment)
            }
        }
    }
}