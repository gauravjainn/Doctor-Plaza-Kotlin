package com.doctorsplaza.app.ui.doctor.fragment.prescription

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentAddMedicineBinding
import com.doctorsplaza.app.databinding.FragmentImagePrescriptionViewBinding
import com.doctorsplaza.app.ui.doctor.fragment.prescription.model.Medicine
import com.doctorsplaza.app.ui.doctor.fragment.prescription.model.Time
import com.doctorsplaza.app.utils.*
import javax.inject.Inject

import kotlin.math.max
import kotlin.math.min
class ImagePrescriptionViewFragment : Fragment(R.layout.fragment_image_prescription_view),
    View.OnClickListener {

    private lateinit var binding: FragmentImagePrescriptionViewBinding

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_image_prescription_view, container, false)
            binding = FragmentImagePrescriptionViewBinding.bind(currentView!!)
            init()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())
        val prescriptionImage = arguments?.getString("prescriptionImage")
        val from = arguments?.getString("from").toString()
        if(from == "appointmentDetails"){
            binding.appBarTitle.text = "Reports"
        }

        Glide.with(requireContext()).applyDefaultRequestOptions(prescriptionRequestOption()).load(prescriptionImage).into(binding.prescriptionImageView)
    }


    private fun setOnClickListener() {
        with(binding) {
            backArrow.setOnClickListener(this@ImagePrescriptionViewFragment)
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backArrow -> {
                findNavController().popBackStack()
            }

        }
    }


}