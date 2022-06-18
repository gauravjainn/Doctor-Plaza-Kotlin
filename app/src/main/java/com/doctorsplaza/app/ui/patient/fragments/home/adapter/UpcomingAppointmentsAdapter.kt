package com.doctorsplaza.app.ui.patient.fragments.home.adapter

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.widget.PopupWindowCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.ItemUpcomingAppointmentsBinding
import com.doctorsplaza.app.data.commonModel.AppointmentData
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class UpcomingAppointmentsAdapter @Inject constructor(): RecyclerView.Adapter<UpcomingAppointmentsAdapter.ViewHolder>() {
    private var positionSize: Int = 0
    private lateinit var  context: Context
    private var doctorQualification = ""

    inner class  ViewHolder(private val binding: ItemUpcomingAppointmentsBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(data: AppointmentData) {
            with(binding){
                Glide.with(context).load(data.doctor_id.profile_picture).into(doctorImage)
                binding.doctorName.text = data.doctor_id.doctorName
                binding.doctorSpecialistIn.text = data.doctor_id.specialization
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val outputFormat = SimpleDateFormat("EEEE, dd MMM", Locale.getDefault())
                val date: Date = inputFormat.parse(data.date)
                val formattedDate: String = outputFormat.format(date)

                binding.appointmentDate.text = formattedDate
                binding.appointmentTime.text = "${data.room_time_slot_id.timeSlotData.start_time} - ${data.room_time_slot_id.timeSlotData.end_time}"

                root.setOnClickListener {
                    appointmentClickListener?.let {
                        it(data)
                    }
                }

                appointmentMore.setOnClickListener {
                    showMoreMenu(binding,data)
                }

            }
        }
    }

    private fun showMoreMenu(binding: ItemUpcomingAppointmentsBinding, data: AppointmentData) {
        val anchor = binding.appointmentMore
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.upcomming_appointment_more, null)

        val popupWindow = PopupWindow(
            popupView, LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT, true
        )
        popupWindow.elevation = 10f

        PopupWindowCompat.showAsDropDown(popupWindow, anchor, -220, -25, Gravity.END)
        PopupWindowCompat.setWindowLayoutType(
            popupWindow,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )

        val rescheduleAppointment = popupView.findViewById<TextView>(R.id.rescheduleAppointment)
        val cancelAppointment = popupView.findViewById<TextView>(R.id.cancelAppointment)

        rescheduleAppointment.setOnClickListener {
            rescheduleAppointmentClickListener?.let {
                it(data)
            }
            popupWindow.dismiss()
        }

        cancelAppointment.setOnClickListener {
            cancelAppointmentClickListener?.let {
                it(data)
            }
            popupWindow.dismiss()
        }

    }

    private val diffUtil = object :DiffUtil.ItemCallback<AppointmentData>(){
        override fun areItemsTheSame(oldItem: AppointmentData, newItem: AppointmentData): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: AppointmentData, newItem: AppointmentData): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this,diffUtil)

    private var cancelAppointmentClickListener: ((data: AppointmentData) -> Unit)? = null

    private var rescheduleAppointmentClickListener: ((data: AppointmentData) -> Unit)? = null

    private var appointmentClickListener: ((data: AppointmentData) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpcomingAppointmentsAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(ItemUpcomingAppointmentsBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: UpcomingAppointmentsAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int =differ.currentList.size

    fun setOnAppointmentClickListener(listener: (data: AppointmentData) -> Unit) {
        appointmentClickListener = listener
    }

    fun setOnRescheduleClickListener(listener: (data: AppointmentData) -> Unit) {
        rescheduleAppointmentClickListener = listener
    }

    fun setOnCancelClickListener(listener: (data: AppointmentData) -> Unit) {
        cancelAppointmentClickListener = listener
    }


}