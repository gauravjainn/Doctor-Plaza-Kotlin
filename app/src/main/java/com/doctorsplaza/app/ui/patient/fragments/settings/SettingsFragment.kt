package com.doctorsplaza.app.ui.patient.fragments.settings

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentSettingsBinding
import com.doctorsplaza.app.data.commonModel.CommonViewModel
import com.doctorsplaza.app.ui.patient.loginSignUp.PatientLoginSignup
import com.doctorsplaza.app.utils.*
import com.google.android.material.button.MaterialButton
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings), View.OnClickListener {

    private lateinit var binding: FragmentSettingsBinding

    private var currentView: View? = null

    @Inject
    lateinit var session: SessionManager

    lateinit var appProgress: DoctorPlazaLoader

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
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        appProgress = DoctorPlazaLoader(requireContext())
    }

    private fun deleteAccount(reason: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("patientid", session.patientId)
        jsonObject.addProperty("mobilenumber", session.mobile)
        jsonObject.addProperty("description", reason)
        commonViewModel.deletePatientAccount(jsonObject)

        commonViewModel.deletePatientAccount.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appProgress.dismiss()
                    dialog.dismiss()
                    if (response.data?.status?.toInt() == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(),response.data.message)
                    } else {
                    if (response.data!!.success) {
                        requireContext().showToast(response.data.message)
                        session.isLogin = false
                        startActivity(Intent(requireActivity(), PatientLoginSignup::class.java))
                        requireActivity().finish()
                    } else {
                        requireContext().showToast(response.data.message)
                    }
                }}

                is Resource.Loading -> {
                    appProgress.show()
                }

                is Resource.Error -> {
                    dialog.dismiss()
                    appProgress.dismiss()
                }
            }
        }
    }


    private fun setOnClickListener() {
        binding.aboutUs.setOnClickListener(this@SettingsFragment)
        binding.privacyPolicy.setOnClickListener(this@SettingsFragment)
        binding.termsOfService.setOnClickListener(this@SettingsFragment)
        binding.deleteAccount.setOnClickListener(this@SettingsFragment)
        binding.backArrow.setOnClickListener(this@SettingsFragment)
    }


    private fun showWarning() {
        dialog = AlertDialog.Builder(context)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to Delete Your Account")
            .setPositiveButton(
                "yes"
            ) { _, _ ->
                deleteAccount("")
            }
            .setNegativeButton("no", null)
            .show()
    }

    private fun showDialog() {
        dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.delete_account_dialog)
        val reason = dialog.findViewById(R.id.reasonEdt) as EditText
        val submitBtn = dialog.findViewById<View>(R.id.submitBtn) as MaterialButton
        val cancelBtn = dialog.findViewById<View>(R.id.cancelBtn) as MaterialButton
        submitBtn.setOnClickListener {
            val reasonText = reason.text.toString()
            deleteAccount(reasonText)
        }
        cancelBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.aboutUs -> {
                val bundle = Bundle()
                bundle.putString("title", "About Us")
                findNavController().navigate(R.id.slugsFragment, bundle)
            }
            R.id.privacyPolicy -> {
                val bundle = Bundle()
                bundle.putString("title", "Privacy Policy")
                findNavController().navigate(R.id.slugsFragment, bundle)
            }
            R.id.termsOfService -> {
                val bundle = Bundle()
                bundle.putString("title", "Terms and Conditions")
                findNavController().navigate(R.id.slugsFragment, bundle)
            }
            R.id.deleteAccount -> {
                if (session.loginType == "patient") {
                    showWarning()
                } else {
                    showDialog()
                }
            }
            R.id.backArrow -> {
                findNavController().popBackStack()
            }
        }
    }
}