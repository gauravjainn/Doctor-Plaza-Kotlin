package com.doctorsplaza.app.ui.doctor.fragment.appointments.adapter

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
import com.doctorsplaza.app.ui.doctor.fragment.home.model.AppointmentData
import com.doctorsplaza.app.utils.patientRequestOption

import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class DoctorPastAppointmentsAdapter @Inject constructor() :
    RecyclerView.Adapter<DoctorPastAppointmentsAdapter.ViewHolder>() {
    private lateinit var context: Context

    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    inner class ViewHolder(private val binding: ItemPastAppointmentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(appointmentData: AppointmentData) {
            with(binding) {
                doctorName.text = appointmentData.patientname
                doctorSpecialistIn.text = "Patient Id: ${appointmentData.patient_id}"

                if (appointmentData.user_id != null) {
                    Glide.with(context).applyDefaultRequestOptions(patientRequestOption())
                        .load(appointmentData.user_id.profile_picture).into(doctorImage)
                }

                consultationFees.text = "₹${appointmentData.consultation_fee}"
                val parsedDate =
                    inputFormat.parse(appointmentData.date)
                val formattedDate = outputFormat.format(parsedDate)

                appointmentTimings.text =
                    "$formattedDate (${appointmentData.room_time_slot_id.start_time} - ${appointmentData.room_time_slot_id.end_time})"


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
                            it(appointmentData)
                        }
                    }
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

    private var pastAppointmentClickListener: ((id: AppointmentData) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DoctorPastAppointmentsAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemPastAppointmentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DoctorPastAppointmentsAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    fun setOnPastAppointmentClickListener(listener: (id: AppointmentData) -> Unit) {
        pastAppointmentClickListener = listener
    }
}