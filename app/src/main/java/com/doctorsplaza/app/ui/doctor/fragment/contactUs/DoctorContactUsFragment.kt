package com.doctorsplaza.app.ui.doctor.fragment.contactUs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.doctorsplaza.app.R
import com.doctorsplaza.app.data.commonModel.CommonViewModel
import com.doctorsplaza.app.databinding.FragmentContactUs2Binding
import com.doctorsplaza.app.databinding.FragmentDoctorProfileBinding
import com.doctorsplaza.app.ui.doctor.fragment.profile.DoctorProfileViewModel
import com.doctorsplaza.app.ui.doctor.fragment.profile.model.DoctorData
import com.doctorsplaza.app.utils.*
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ContactUsFragment : Fragment() , View.OnClickListener {
    private lateinit var binding: FragmentContactUs2Binding

    private val commonViewModel: CommonViewModel by viewModels()

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader
    private var subject = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_contact_us2, container, false)
            binding = FragmentContactUs2Binding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())
        binding.name.setText(session.loginName.toString())

    }

    private fun setObserver() {
        commonViewModel.contactUs.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.status?.toInt() == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(),response.data.message)
                    } else {
                    if (response.data!!.success) {
                        requireContext().showToast(response.data.message)
                        findNavController().popBackStack()
                    } else {
                        requireContext().showToast(response.data.message)
                    }
                }}

                is Resource.Loading -> {
                    appLoader.show()
                }

                is Resource.Error -> {
                    appLoader.dismiss()
                }

            }
        }
    }



    private fun setOnClickListener() {
        with(binding) {
            backArrow.setOnClickListener(this@ContactUsFragment)
            submit.setOnClickListener(this@ContactUsFragment)
        }
    }

    private fun sendMessage() {
        val fromEmail = binding.name.text.toString()
        val toEmail = binding.email.text.toString()
        val content = binding.message.text.toString()

        when {
            fromEmail.isEmpty() -> {
                showToast("please enter a your valid email")
            }
            toEmail.isEmpty() -> {
                showToast("please enter a admin valid email")
            }
            content.isEmpty() -> {
                showToast("please enter a valid message")
            }
            else -> {
                val jsonObject = JsonObject()
                jsonObject.addProperty("from", fromEmail)
                jsonObject.addProperty("message", content)
                jsonObject.addProperty("subject", subject)
                jsonObject.addProperty("to", toEmail)
                jsonObject.addProperty("type", session.loginType)
                commonViewModel.contactUs(jsonObject)
            }
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backArrow -> {
                findNavController().popBackStack()
            }
            R.id.submit -> {
                sendMessage()
            }
        }
    }
}
