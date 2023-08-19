package com.doctorsplaza.app.ui.doctor.fragment.prescription

import android.os.Bundle
import android.util.Log
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
class AddMedicineFragment : Fragment(R.layout.fragment_add_medicine), View.OnClickListener {

    private lateinit var medicineData: Medicine
    private var appointmentId: String? = ""
    private lateinit var binding: FragmentAddMedicineBinding

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null
    private lateinit var appLoader: DoctorPlazaLoader

    private var beforeBreakfast: Boolean = false
    private var beforeLunch: Boolean = false
    private var beforeDinner: Boolean = false
    private var afterBreakfast: Boolean = false
    private var afterLunch: Boolean = false
    private var afterDinner: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_add_medicine, container, false)
            binding = FragmentAddMedicineBinding.bind(currentView!!)
            init()
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

        if (medicineData.time[0].beforeBreakfast) {
            beforeBreakfast = true
            setMedicineTimeSelectionViews(binding.beforeBreakFast, 1)
        } else {
            beforeBreakfast = false
            setMedicineTimeSelectionViews(binding.beforeBreakFast, 0)
        }

        if (medicineData.time[1].beforeLunch) {
            beforeLunch = true
            setMedicineTimeSelectionViews(binding.beforeLunch, 1)
        } else {
            beforeLunch = false
            setMedicineTimeSelectionViews(binding.beforeLunch, 0)
        }

        if (medicineData.time[2].beforeDinner) {
            beforeDinner = true
            setMedicineTimeSelectionViews(binding.beforeDinner, 1)
        } else {
            beforeDinner = false
            setMedicineTimeSelectionViews(binding.beforeDinner, 0)
        }

        if (medicineData.time[3].afterBreakfast) {
            afterBreakfast = true
            setMedicineTimeSelectionViews(binding.afterBreakFast, 1)
        } else {
            afterBreakfast = false
            setMedicineTimeSelectionViews(binding.afterBreakFast, 0)
        }

        if (medicineData.time[4].afterLunch) {
            afterLunch = true
            setMedicineTimeSelectionViews(binding.afterLunch, 1)
        } else {
            afterLunch = false
            setMedicineTimeSelectionViews(binding.afterLunch, 0)
        }

        if (medicineData.time[5].afterDinner) {
            afterDinner = true
            setMedicineTimeSelectionViews(binding.afterDinner, 1)
        } else {
            afterDinner = false
            setMedicineTimeSelectionViews(binding.afterDinner, 0)
        }
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
                if (!beforeBreakfast) {
                    beforeBreakfast = true
                    setMedicineTimeSelectionViews(binding.beforeBreakFast, 1)
                } else {
                    beforeBreakfast = false
                    setMedicineTimeSelectionViews(binding.beforeBreakFast, 0)
                }
                Log.e("TAG", "beforeBreakfast " + beforeBreakfast)
                Log.e("TAG", "beforeLunch " + beforeLunch)
                Log.e("TAG", "beforeDinner " + beforeDinner)
                Log.e("TAG", "afterBreakfast " + afterBreakfast)
                Log.e("TAG", "afterLunch " + afterLunch)
                Log.e("TAG", "afterDinner " + afterDinner)
            }

            R.id.beforeLunch -> {
                if (!beforeLunch) {
                    beforeLunch = true
                    setMedicineTimeSelectionViews(binding.beforeLunch, 1)
                } else {
                    beforeLunch = false
                    setMedicineTimeSelectionViews(binding.beforeLunch, 0)
                }
                Log.e("TAG", "beforeBreakfast " + beforeBreakfast)
                Log.e("TAG", "beforeLunch " + beforeLunch)
                Log.e("TAG", "beforeDinner " + beforeDinner)
                Log.e("TAG", "afterBreakfast " + afterBreakfast)
                Log.e("TAG", "afterLunch " + afterLunch)
                Log.e("TAG", "afterDinner " + afterDinner)
            }

            R.id.beforeDinner -> {
                if (!beforeDinner) {
                    beforeDinner = true
                    setMedicineTimeSelectionViews(binding.beforeDinner, 1)
                } else {
                    beforeDinner = false
                    setMedicineTimeSelectionViews(binding.beforeDinner, 0)
                }
                Log.e("TAG", "beforeBreakfast " + beforeBreakfast)
                Log.e("TAG", "beforeLunch " + beforeLunch)
                Log.e("TAG", "beforeDinner " + beforeDinner)
                Log.e("TAG", "afterBreakfast " + afterBreakfast)
                Log.e("TAG", "afterLunch " + afterLunch)
                Log.e("TAG", "afterDinner " + afterDinner)
            }

            R.id.afterBreakFast -> {
                if (!afterBreakfast) {
                    afterBreakfast = true
                    setMedicineTimeSelectionViews(binding.afterBreakFast, 1)
                } else {
                    afterBreakfast = false
                    setMedicineTimeSelectionViews(binding.afterBreakFast, 0)
                }
                Log.e("TAG", "beforeBreakfast " + beforeBreakfast)
                Log.e("TAG", "beforeLunch " + beforeLunch)
                Log.e("TAG", "beforeDinner " + beforeDinner)
                Log.e("TAG", "afterBreakfast " + afterBreakfast)
                Log.e("TAG", "afterLunch " + afterLunch)
                Log.e("TAG", "afterDinner " + afterDinner)
            }

            R.id.afterLunch -> {
                if (!afterLunch) {
                    afterLunch = true
                    setMedicineTimeSelectionViews(binding.afterLunch, 1)
                } else {
                    afterLunch = false
                    setMedicineTimeSelectionViews(binding.afterLunch, 0)
                }
                Log.e("TAG", "beforeBreakfast " + beforeBreakfast)
                Log.e("TAG", "beforeLunch " + beforeLunch)
                Log.e("TAG", "beforeDinner " + beforeDinner)
                Log.e("TAG", "afterBreakfast " + afterBreakfast)
                Log.e("TAG", "afterLunch " + afterLunch)
                Log.e("TAG", "afterDinner " + afterDinner)
            }

            R.id.afterDinner -> {
                if (!afterDinner) {
                    afterDinner = true
                    setMedicineTimeSelectionViews(binding.afterDinner, 1)
                } else {
                    afterDinner = false
                    setMedicineTimeSelectionViews(binding.afterDinner, 0)
                }

                Log.e("TAG", "beforeBreakfast " + beforeBreakfast)
                Log.e("TAG", "beforeLunch " + beforeLunch)
                Log.e("TAG", "beforeDinner " + beforeDinner)
                Log.e("TAG", "afterBreakfast " + afterBreakfast)
                Log.e("TAG", "afterLunch " + afterLunch)
                Log.e("TAG", "afterDinner " + afterDinner)
            }
        }
    }

    private fun validateAndAddMedicine() {
        Log.e("TAG", "beforeBreakfast " + beforeBreakfast)
        Log.e("TAG", "beforeLunch " + beforeLunch)
        Log.e("TAG", "beforeDinner " + beforeDinner)
        Log.e("TAG", "afterBreakfast " + afterBreakfast)
        Log.e("TAG", "afterLunch " + afterLunch)
        Log.e("TAG", "afterDinner " + afterDinner)

        val medicine = binding.medicine.text.toString()
        val medicineType = binding.medicineType.text.toString()
        val days = binding.days.text.toString()
        val instructions = binding.instructions.text.toString()

        if (medicine.isEmpty()) {
            showToast("Please enter any Medicine")
        } else if (medicineType.isEmpty()) {
            showToast("Please enter any Medicine Type")
        } else if (!beforeBreakfast && !beforeLunch && !beforeDinner && !afterBreakfast && !afterLunch && !afterDinner) {
            showToast("Please select any medicine time")
        } else if (days.isEmpty()) {
            showToast("Please enter any days")
        } else {
            val medicineTime: MutableList<Time> = ArrayList()

            medicineTime.add(
                Time(
                    beforeBreakfast,
                    beforeLunch,
                    beforeDinner,
                    afterBreakfast,
                    afterLunch,
                    afterDinner
                )
            )
            medicineTime.add(
                Time(
                    beforeBreakfast,
                    beforeLunch,
                    beforeDinner,
                    afterBreakfast,
                    afterLunch,
                    afterDinner
                )
            )
            medicineTime.add(
                Time(
                    beforeBreakfast,
                    beforeLunch,
                    beforeDinner,
                    afterBreakfast,
                    afterLunch,
                    afterDinner
                )
            )
            medicineTime.add(
                Time(
                    beforeBreakfast,
                    beforeLunch,
                    beforeDinner,
                    afterBreakfast,
                    afterLunch,
                    afterDinner
                )
            )
            medicineTime.add(
                Time(
                    beforeBreakfast,
                    beforeLunch,
                    beforeDinner,
                    afterBreakfast,
                    afterLunch,
                    afterDinner
                )
            )
            medicineTime.add(
                Time(
                    beforeBreakfast,
                    beforeLunch,
                    beforeDinner,
                    afterBreakfast,
                    afterLunch,
                    afterDinner
                )
            )
            Log.e("TAG", "medicineTime $medicineTime")
            val medicineData = Medicine(
                days, instructions, medicineName = medicine, medicineType, medicineTime, false
            )
            Log.e("TAG", "medicineData $medicineData")
            addMedicine?.postValue(medicineData)
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