package com.doctorsplaza.app.ui.patient.prescription

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentPrescriptionBinding
import com.doctorsplaza.app.utils.DoctorPlazaLoader
import com.doctorsplaza.app.utils.SessionManager
import javax.inject.Inject

class PrescriptionFragment : Fragment(R.layout.fragment_prescription) , View.OnClickListener {
    private  var prescriptionUrl: String = ""

    private lateinit var binding: FragmentPrescriptionBinding

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader
    private val removePdfTopIcon =
        "javascript:(function() {" + "document.querySelector('[role=\"toolbar\"]').remove();})()"

    private var downloadId: Long = 0L

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

        prescriptionUrl = arguments?.getString("prescription").toString()
        val title = arguments?.getString("title").toString()

        val from = arguments?.getString("from").toString()

        if(from == "appointmentDetails"||from == "Profile"){
            binding.appBarTitle.text = "Reports"
        }

        if(from == "Profile"){
            binding.downLoad.isVisible = false
        }

        appLoader.show()
        showPdfFile(prescriptionUrl)
    }


    private fun showPdfFile(imageString: String) {

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
        binding.downLoad.setOnClickListener(this)
    }

    private fun downloadAppointmentSlip(url: String) {
        val fileName = url.substring(url.lastIndexOf('/') + 1)
        val downloadManager: DownloadManager? =
            requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setAllowedNetworkTypes(
            DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
        )
        request.setVisibleInDownloadsUi(true)
        request.allowScanningByMediaScanner()
        downloadId = downloadManager!!.enqueue(request)
        appLoader.dismiss()
    }

    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            Log.e("LOG", "Prescription Details onDownloadComplete")
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadId == id) {
                appLoader.dismiss()
                Toast.makeText(
                    requireContext(),
                    "Report Downloaded....",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backArrow -> {
                findNavController().popBackStack()
            }
            R.id.downLoad -> {
                appLoader.show()
                downloadAppointmentSlip(prescriptionUrl)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.registerReceiver(
                        requireActivity(),
                        onDownloadComplete,
                        IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                        ContextCompat.RECEIVER_NOT_EXPORTED
                    )
                } else {
                    requireActivity().registerReceiver(
                        onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                    )
                }
            }
        }
    }
}