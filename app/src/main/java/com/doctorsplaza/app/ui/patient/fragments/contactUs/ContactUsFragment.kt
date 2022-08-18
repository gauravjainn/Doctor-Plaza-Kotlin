package com.doctorsplaza.app.ui.patient.fragments.contactUs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentContactUsBinding
import com.doctorsplaza.app.data.commonModel.CommonViewModel
import com.doctorsplaza.app.utils.*
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ContactUsFragment : Fragment(R.layout.fragment_contact_us), View.OnClickListener {
    private lateinit var binding: FragmentContactUsBinding

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
            currentView = inflater.inflate(R.layout.fragment_contact_us, container, false)
            binding = FragmentContactUsBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())

        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.subject_menu,
            R.layout.spinner_text
        )
        adapter.setDropDownViewResource(R.layout.spinner_text)
        binding.subjectSpinner.adapter = adapter

        binding.subjectSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    subject = p0?.getItemAtPosition(p2).toString()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }

        binding.fromEmailId.setText(session.loginEmail.toString())
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
            saveBtn.setOnClickListener(this@ContactUsFragment)
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backArrow -> {
                findNavController().popBackStack()
            }
            R.id.saveBtn -> {
                sendMessage()
            }
        }
    }

    private fun sendMessage() {
        val fromEmail = binding.fromEmailId.text.toString()
        val toEmail = binding.toEmail.text.toString()
        val content = binding.content.text.toString()

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


}
