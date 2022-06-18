package com.doctorsplaza.app.ui.patient.loginSignUp.introduction

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentIntroductionTwoBinding
import com.doctorsplaza.app.ui.doctor.DoctorMainActivity
import com.doctorsplaza.app.ui.doctor.loginSignUp.DoctorLoginSignupActivity
import com.doctorsplaza.app.utils.DoctorPlazaLoader
import com.doctorsplaza.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class IntroductionTwoFragment : Fragment(R.layout.fragment_introduction_two), View.OnClickListener {

    private lateinit var binding: FragmentIntroductionTwoBinding

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var appProgress: DoctorPlazaLoader

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_introduction_two, container, false)
            binding = FragmentIntroductionTwoBinding.bind(currentView!!)
            init()
            setObserver()
            setRecyclerView()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        appProgress = DoctorPlazaLoader(requireContext())
    }


    private fun setObserver() {
    }


    private fun setOnClickListener() {
        with(binding) {
            nextBtn.setOnClickListener(this@IntroductionTwoFragment)
            loginAsDoctor.setOnClickListener(this@IntroductionTwoFragment)
        }
    }


    private fun setRecyclerView() {}

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.nextBtn -> {
                findNavController().navigate(R.id.patientSignUpFragment)
            }
            R.id.loginAsDoctor -> {
                startActivity(Intent(requireActivity(),DoctorLoginSignupActivity::class.java))

            }
        }
    }
}