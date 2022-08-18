package com.doctorsplaza.app.ui.patient.prescription

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentPrescriptionBinding
import com.doctorsplaza.app.utils.DoctorPlazaLoader
import com.doctorsplaza.app.utils.SessionManager
import javax.inject.Inject

class PrescriptionFragment : Fragment(R.layout.fragment_prescription) , View.OnClickListener {
    private lateinit var binding: FragmentPrescriptionBinding

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader
    private val removePdfTopIcon =
        "javascript:(function() {" + "document.querySelector('[role=\"toolbar\"]').remove();})()"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_prescription, container, false)
            binding = FragmentPrescriptionBinding.bind(currentView!!)
            init()
            setOnClickListener()
        }
        return currentView!!
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())

        val prescriptionUrl = arguments?.getString("prescription").toString()
        val from = arguments?.getString("from").toString()
        if(from == "appointmentDetails"){
            binding.appBarTitle.text = "Reports"
        }
        showPdfFile(prescriptionUrl)
    }


    private fun showPdfFile(imageString: String) {
        appLoader.show()
        binding.pdfWebView.invalidate()
        binding.pdfWebView.settings.javaScriptEnabled = true
        binding.pdfWebView.settings.setSupportZoom(true)
            binding.pdfWebView.loadUrl("http://docs.google.com/gview?embedded=true&url=$imageString")
        binding.pdfWebView.webViewClient = object : WebViewClient() {
            var checkOnPageStartedCalled = false
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                checkOnPageStartedCalled = true
            }

            override fun onPageFinished(view: WebView, url: String) {
                if (checkOnPageStartedCalled) {
                    binding.pdfWebView.loadUrl(removePdfTopIcon)
                    appLoader.dismiss()
                } else {
                    showPdfFile(imageString)
                }
            }
        }
    }

    private fun setOnClickListener() {
        binding.backArrow.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backArrow -> {
                findNavController().popBackStack()
            }
        }
    }
}