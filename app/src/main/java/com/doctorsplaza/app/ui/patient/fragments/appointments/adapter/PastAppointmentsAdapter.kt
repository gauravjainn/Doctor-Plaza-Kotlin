package com.doctorsplaza.app.ui.patient.fragments.appointments.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.ItemPastAppointmentBinding
import com.doctorsplaza.app.data.commonModel.AppointmentData
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class PastAppointmentsAdapter @Inject constructor() :
    RecyclerView.Adapter<PastAppointmentsAdapter.ViewHolder>() {
    private lateinit var context: Context
    private var doctorQualification = ""

    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    inner class ViewHolder(private val binding: ItemPastAppointmentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(appointmentData: AppointmentData) {
            with(binding) {
                try {
                    doctorName.text = appointmentData.doctor_id.doctorName
                    doctorSpecialistIn.text = appointmentData.doctor_id.specialization
                    doctorQualification = appointmentData.doctor_id.qualification
                    Glide.with(context).load(appointmentData.doctor_id.profile_picture)
                        .into(doctorImage)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                try {
                    consultationFees.text = "₹${appointmentData.consultation_fee}"
                    val parsedDate =
                        inputFormat.parse(appointmentData.date)
                    val formattedDate = outputFormat.format(parsedDate)

                    appointmentTimings.text =
                        "$formattedDate (${appointmentData.room_time_slot_id.timeSlotData.start_time} - ${appointmentData.room_time_slot_id.timeSlotData.end_time})"

                    if (appointmentData.status.lowercase() == "cancelled") {
                        appointmentStatus.text = "Cancelled"
                        appointmentStatus.background =
                            ContextCompat.getDrawable(context, R.drawable.red_corner_radius)
                    } else if (appointmentData.status.lowercase() == "done") {
                        appointmentStatus.background =
                            ContextCompat.getDrawable(context, R.drawable.gree_corner_radius)
                        appointmentStatus.text = "Success"
                    }
                    root.setOnClickListener {
                        if (appointmentData.status.lowercase() == "done") {
                            pastAppointmentClickListener?.let {
                                it(appointmentData._id)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<AppointmentData>() {
        override fun areItemsTheSame(oldItem: AppointmentData, newItem: AppointmentData): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: AppointmentData,
            newItem: AppointmentData
        ): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this, diffUtil)

    private var pastAppointmentClickListener: ((id: String) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PastAppointmentsAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemPastAppointmentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PastAppointmentsAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    fun setOnPastAppointmentClickListener(listener: (id: String) -> Unit) {
        pastAppointmentClickListener = listener
    }
}