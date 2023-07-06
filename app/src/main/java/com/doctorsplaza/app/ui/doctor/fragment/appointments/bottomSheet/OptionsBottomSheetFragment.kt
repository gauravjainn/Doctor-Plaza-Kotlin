package com.doctorsplaza.app.ui.doctor.fragment.appointments.bottomSheet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.SelectPrescriptionTypeBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OptionsBottomSheetFragment : BottomSheetDialogFragment() {
    lateinit var binding: SelectPrescriptionTypeBinding
    lateinit var mContext:Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AppBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val currentView = inflater.inflate(R.layout.select_prescription_type, container, false)
        mContext = currentView.context
        binding = SelectPrescriptionTypeBinding.bind(currentView!!)
        return currentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
    }

    private fun setUpViews() {
        binding.addPrescription.setOnClickListener {
            dismissAllowingStateLoss()
            prescriptionClickListener?.let {
                it("add")
            }
        }

        binding.scanPrescription.setOnClickListener {
            dismissAllowingStateLoss()
            scanClickListener?.let { it("scan") }

        }

        binding.gallery.setOnClickListener {
            dismissAllowingStateLoss()
            imagePickerClickListener?.let { it("gallery") }

        }
    }

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): OptionsBottomSheetFragment {
            val fragment = OptionsBottomSheetFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private var scanClickListener: ((id: String) -> Unit)? = null
    private var prescriptionClickListener: ((id: String) -> Unit)? = null
    private var imagePickerClickListener: ((id: String) -> Unit)? = null

    fun setOnScanClickListener(listener: (id: String) -> Unit) {
        scanClickListener = listener
    }

    fun setOnPrescriptionClickListener(listener: (id: String) -> Unit) {
        prescriptionClickListener = listener
    }

    fun setOnImagePickerClickListener(listener: (id: String) -> Unit) {
        imagePickerClickListener = listener
    }

}
