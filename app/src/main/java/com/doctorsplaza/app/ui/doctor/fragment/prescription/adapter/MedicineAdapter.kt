package com.doctorsplaza.app.ui.doctor.fragment.prescription.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.ItemMedicineBinding
import com.doctorsplaza.app.ui.doctor.fragment.home.model.AppointmentData
import com.doctorsplaza.app.ui.doctor.fragment.prescription.model.Medicine
import com.doctorsplaza.app.ui.doctor.fragment.prescription.model.MedicineModel
import java.util.*
import javax.inject.Inject

class MedicineAdapter @Inject constructor() : RecyclerView.Adapter<MedicineAdapter.ViewHolder>() {
    private var showEditView: Boolean = true
    private lateinit var context: Context

    inner class ViewHolder(private val binding: ItemMedicineBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(medicineData: Medicine) {
            with(binding) {

                editLbl.isVisible = showEditView
                expandView.isVisible = medicineData.isExpanded

                medicine.text = medicineData.medicineName
                medicineType.text = medicineData.medicineType
                days.text = medicineData.days
                instructions.text = medicineData.medicineInstruction

                setMedicineTimeSelectionViews(beforeBreakFast, medicineData.time[0].beforeBreakfast)
                setMedicineTimeSelectionViews(beforeLunch, medicineData.time[1].beforeLunch)
                setMedicineTimeSelectionViews(beforeDinner, medicineData.time[2].beforeDinner)

                setMedicineTimeSelectionViews(afterBreakFast, medicineData.time[3].afterBreakfast)
                setMedicineTimeSelectionViews(afterLunch, medicineData.time[4].afterLunch)
                setMedicineTimeSelectionViews(afterDinner, medicineData.time[5].afterDinner)

                expandLbl.setOnClickListener {
                    medicineData.isExpanded = !medicineData.isExpanded
                    notifyDataSetChanged()
                }

                editLbl.setOnClickListener {
                    editMedicineClickListener?.let {
                        it(medicineData, adapterPosition)
                    }
                }
            }
        }
    }


    private fun setMedicineTimeSelectionViews(timView: TextView, status: Boolean) {
        if (!status) {
            timView.background =
                ContextCompat.getDrawable(context, R.drawable.medicine_time_unselected)
        } else if (status) {
            timView.background =
                ContextCompat.getDrawable(context, R.drawable.medicine_time_selected)
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<Medicine>() {
        override fun areItemsTheSame(oldItem: Medicine, newItem: Medicine): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: Medicine,
            newItem: Medicine
        ): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this, diffUtil)

    private var editMedicineClickListener: ((data: Medicine, position: Int) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MedicineAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemMedicineBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MedicineAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    fun setOnEditClickListener(listener: (data: Medicine, position: Int) -> Unit) {
        editMedicineClickListener = listener
    }

    fun hideEditView(status: Boolean) {
        showEditView = status
    }
}