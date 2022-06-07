package com.doctorsplaza.app.ui.patient.fragments.reminder.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.doctorsplaza.app.databinding.ItemMedicineReminderBinding
import com.doctorsplaza.app.ui.patient.fragments.reminder.model.ReminderData
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class MedicineReminderAdapter @Inject constructor() :
    RecyclerView.Adapter<MedicineReminderAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val itemMedicineReminder: ItemMedicineReminderBinding) :
        RecyclerView.ViewHolder(itemMedicineReminder.root) {
        fun bind(data: ReminderData) {
            with(itemMedicineReminder) {
                medicineName.text = data.title
                medicineDoses.text = data.type
                medicineTime.text = data.dutime
                more.setOnClickListener {
                    updateReminderClickListener?.let {
                        it(data)
                    }

                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<ReminderData>() {
        override fun areItemsTheSame(oldItem: ReminderData, newItem: ReminderData): Boolean {
            return oldItem._id == newItem._id
        }

        override fun areContentsTheSame(oldItem: ReminderData, newItem: ReminderData): Boolean {
            return oldItem._id == newItem._id
        }

    }
    val differ = AsyncListDiffer(this, diffUtil)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MedicineReminderAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemMedicineReminderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    private var updateReminderClickListener: ((data: ReminderData) -> Unit)? = null


    override fun onBindViewHolder(holder: MedicineReminderAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size


    fun setOnUpdateReminderClickListener(listener: (data: ReminderData) -> Unit) {
        updateReminderClickListener = listener
    }


}