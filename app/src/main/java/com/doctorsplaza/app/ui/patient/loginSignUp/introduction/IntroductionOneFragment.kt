package com.doctorsplaza.app.ui.patient.loginSignUp.introduction

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentIntroductionOneBinding
import com.doctorsplaza.app.utils.DoctorPlazaLoader
import com.doctorsplaza.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class IntroductionOneFragment : Fragment(R.layout.fragment_introduction_one), View.OnClickListener {


    private lateinit var binding: FragmentIntroductionOneBinding

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
            currentView = inflater.inflate(R.layout.fragment_introduction_one, container, false)
            binding = FragmentIntroductionOneBinding.bind(currentView!!)
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
        with(binding){
            nextBtn.setOnClickListener(this@IntroductionOneFragment)
        }
    }


    private fun setRecyclerView() {}

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.nextBtn->{
                findNavController().navigate(R.id.introductionTwoFragment)
            }
        }
    }
}