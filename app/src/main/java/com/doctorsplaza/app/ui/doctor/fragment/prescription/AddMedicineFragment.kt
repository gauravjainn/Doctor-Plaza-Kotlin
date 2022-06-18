package com.doctorsplaza.app.ui.doctor.fragment.prescription

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentAddMedicineBinding
import com.doctorsplaza.app.ui.doctor.fragment.prescription.model.Medicine
import com.doctorsplaza.app.ui.doctor.fragment.prescription.model.Time
import com.doctorsplaza.app.utils.DoctorPlazaLoader
import com.doctorsplaza.app.utils.SessionManager
import com.doctorsplaza.app.utils.addMedicine
import com.doctorsplaza.app.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddMedicineFragment : Fragment(R.layout.fragment_add_medicine),
    View.OnClickListener {

    private lateinit var medicineData: Medicine
    private var medicinePosition: Int? = null
    private var appointmentId: String? = ""

    private lateinit var binding: FragmentAddMedicineBinding

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader


    private val medicineTime: MutableList<Any> = ArrayList()

    var beforeBreakfast = 0
    var beforeLunch = 0
    var beforeDinner = 0

    var afterBreakfast = 0
    var afterLunch = 0
    var afterDinner = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_add_medicine, container, false)
            binding = FragmentAddMedicineBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())
        appointmentId = arguments?.getString("appointmentId")
        if (arguments?.getSerializable("data") != null) {
            medicineData = arguments?.getSerializable("data") as Medicine
            setMedicineData()
        }


    }

    private fun setMedicineData() {
        with(binding) {
            medicine.setText(medicineData.medicineName)
            medicineType.setText(medicineData.medicineType)
            days.setText(medicineData.days)
            instructions.setText(medicineData.medicineInstruction)
        }

        if (medicineData.time[0].beforeLunch == 0) {
            beforeLunch = 1
            setMedicineTimeSelectionViews(binding.beforeLunch, 1)
        } else {
            beforeLunch = 0
            setMedicineTimeSelectionViews(binding.beforeLunch, 0)
        }

        if (medicineData.time[0].afterLunch == 0) {
            afterLunch = 1
            setMedicineTimeSelectionViews(binding.afterLunch, 1)
        } else {
            afterLunch = 0
            setMedicineTimeSelectionViews(binding.afterLunch, 0)
        }

        if (medicineData.time[0].beforeBreakfast == 0) {
            beforeBreakfast = 1
            setMedicineTimeSelectionViews(binding.beforeBreakFast, 1)
        } else {
            beforeBreakfast = 0
            setMedicineTimeSelectionViews(binding.beforeBreakFast, 0)
        }


        if (medicineData.time[0].afterBreakfast == 0) {
            afterBreakfast = 1
            setMedicineTimeSelectionViews(binding.afterBreakFast, 1)
        } else {
            afterBreakfast = 0
            setMedicineTimeSelectionViews(binding.afterBreakFast, 0)
        }


        if (medicineData.time[0].beforeDinner == 0) {
            beforeDinner = 1
            setMedicineTimeSelectionViews(binding.beforeDinner, 1)
        } else {
            beforeDinner = 0
            setMedicineTimeSelectionViews(binding.beforeDinner, 0)
        }


        if (medicineData.time[0].afterDinner == 0) {
            afterDinner = 1
            setMedicineTimeSelectionViews(binding.afterDinner, 1)
        } else {
            afterDinner = 0
            setMedicineTimeSelectionViews(binding.afterDinner, 0)
        }

    }

    private fun setObserver() {

    }


    private fun setOnClickListener() {
        with(binding) {
            saveBtn.setOnClickListener(this@AddMedicineFragment)
            backArrow.setOnClickListener(this@AddMedicineFragment)
            beforeBreakFast.setOnClickListener(this@AddMedicineFragment)
            beforeLunch.setOnClickListener(this@AddMedicineFragment)
            beforeDinner.setOnClickListener(this@AddMedicineFragment)
            afterBreakFast.setOnClickListener(this@AddMedicineFragment)
            afterLunch.setOnClickListener(this@AddMedicineFragment)
            afterDinner.setOnClickListener(this@AddMedicineFragment)

        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backArrow -> {
                findNavController().popBackStack()
            }
            R.id.saveBtn -> {
                validateAndAddMedicine()
            }
            R.id.beforeBreakFast -> {
                if (beforeBreakfast == 0) {
                    beforeBreakfast = 1
                    setMedicineTimeSelectionViews(binding.beforeBreakFast, 1)
                } else {
                    beforeBreakfast = 0
                    setMedicineTimeSelectionViews(binding.beforeBreakFast, 0)
                }
            }
            R.id.beforeLunch -> {
                if (beforeLunch == 0) {
                    beforeLunch = 1
                    setMedicineTimeSelectionViews(binding.beforeLunch, 1)
                } else {
                    beforeLunch = 0
                    setMedicineTimeSelectionViews(binding.beforeLunch, 0)
                }
            }
            R.id.beforeDinner -> {
                if (beforeDinner == 0) {
                    beforeDinner = 1
                    setMedicineTimeSelectionViews(binding.beforeDinner, 1)
                } else {
                    beforeDinner = 0
                    setMedicineTimeSelectionViews(binding.beforeDinner, 0)
                }
            }
            R.id.afterBreakFast -> {
                if (afterBreakfast == 0) {
                    afterBreakfast = 1
                    setMedicineTimeSelectionViews(binding.afterBreakFast, 1)
                } else {
                    afterBreakfast = 0
                    setMedicineTimeSelectionViews(binding.afterBreakFast, 0)
                }
            }
            R.id.afterLunch -> {
                if (afterLunch == 0) {
                    afterLunch = 1
                    setMedicineTimeSelectionViews(binding.afterLunch, 1)
                } else {
                    afterLunch = 0
                    setMedicineTimeSelectionViews(binding.afterLunch, 0)
                }
            }
            R.id.afterDinner -> {
                if (afterDinner == 0) {
                    afterDinner = 1
                    setMedicineTimeSelectionViews(binding.afterDinner, 1)
                } else {
                    afterDinner = 0
                    setMedicineTimeSelectionViews(binding.afterDinner, 0)
                }
            }
        }
    }

    private fun validateAndAddMedicine() {
        val medicine = binding.medicine.text.toString()
        val medicineType = binding.medicineType.text.toString()
        val days = binding.days.text.toString()
        val instructions = binding.instructions.text.toString()

        if (medicine.isEmpty()) {
            showToast("Please enter any Medicine")
        } else if (medicineType.isEmpty()) {
            showToast("Please enter any Medicine Type")
        } else if (beforeBreakfast == 0 && beforeLunch == 0 && beforeDinner == 0 && afterBreakfast == 0 && afterLunch == 0 && afterDinner == 0) {
            showToast("Please select any medicine time")
        } else if (days.isEmpty()) {
            showToast("Please enter any days")
        } else {
            val medicineTime: MutableList<Time> = ArrayList()
            medicineTime.add(
                Time(
                    afterBreakfast,
                    afterDinner,
                    afterLunch,
                    beforeBreakfast,
                    beforeDinner,
                    beforeLunch
                )
            )
            val medicineData = Medicine(
                days,
                instructions,
                medicineName = medicine,
                medicineType,
                medicineTime,
                false
            )
            addMedicine.postValue(medicineData)
            findNavController().popBackStack()
        }
    }

    private fun setMedicineTimeSelectionViews(timView: TextView, status: Int) {
        if (status == 0) {
            timView.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.medicine_time_unselected)
        } else if (status == 1) {
            timView.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.medicine_time_selected)
        }
    }
}