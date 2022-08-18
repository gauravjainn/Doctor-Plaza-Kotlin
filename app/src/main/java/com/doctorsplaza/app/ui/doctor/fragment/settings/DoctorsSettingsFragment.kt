package com.doctorsplaza.app.ui.doctor.fragment.settings

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentSettingsBinding
import com.doctorsplaza.app.data.commonModel.CommonViewModel
import com.doctorsplaza.app.ui.patient.loginSignUp.PatientLoginSignup
import com.doctorsplaza.app.utils.*
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DoctorsSettingsFragment : Fragment(R.layout.fragment_settings), View.OnClickListener {

    private lateinit var binding: FragmentSettingsBinding

    private var currentView: View? = null

    @Inject
    lateinit var session: SessionManager

    private lateinit var appLoader: DoctorPlazaLoader

    private val commonViewModel: CommonViewModel by viewModels()

    lateinit var dialog: Dialog
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_settings, container, false)
            binding = FragmentSettingsBinding.bind(currentView!!)
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
        commonViewModel.deleteDoctorAccount.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    dialog.dismiss()
                    if (response.data?.status?.toInt() == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(),response.data.message)
                    } else {
                    if (response.data!!.code.toInt() == 200) {
                        requireContext().showToast(response.data.message)
                        session.isLogin = false
                        startActivity(Intent(requireActivity(), PatientLoginSignup::class.java))
                        requireActivity().finish()
                    } else {
                        requireContext().showToast(response.data.message)
                    }
                }}

                is Resource.Loading -> {
                    appLoader.show()
                }

                is Resource.Error -> {
                    dialog.dismiss()
                    appLoader.dismiss()
                    requireContext().showToast(response.message.toString())
                }
            }
        }
    }

    private fun deleteAccount() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("doctorid", session.doctorId)
        jsonObject.addProperty("mobilenumber", session.mobile)
        jsonObject.addProperty("description", "")
        commonViewModel.deleteDoctorAccount()
    }

    private fun setOnClickListener() {
        binding.aboutUs.setOnClickListener(this@DoctorsSettingsFragment)
        binding.privacyPolicy.setOnClickListener(this@DoctorsSettingsFragment)
        binding.termsOfService.setOnClickListener(this@DoctorsSettingsFragment)
        binding.deleteAccount.setOnClickListener(this@DoctorsSettingsFragment)
        binding.backArrow.setOnClickListener(this@DoctorsSettingsFragment)
    }

    private fun showWarning() {
        dialog = AlertDialog.Builder(context)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to Delete Your Account")
            .setPositiveButton(
                "yes"
            ) { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("no", null)
            .show()
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.aboutUs -> {
                val bundle = Bundle()
                bundle.putString("title", "About Us")
                findNavController().navigate(R.id.doctorSlugsFragment, bundle)
            }
            R.id.privacyPolicy -> {
                val bundle = Bundle()
                bundle.putString("title", "Privacy Policy")
                findNavController().navigate(R.id.doctorSlugsFragment, bundle)
            }
            R.id.termsOfService -> {
                val bundle = Bundle()
                bundle.putString("title", "Terms and Conditions")
                findNavController().navigate(R.id.doctorSlugsFragment, bundle)
            }
            R.id.deleteAccount -> {
                showWarning()
            }
            R.id.backArrow -> {
                findNavController().popBackStack()
            }
        }
    }
}