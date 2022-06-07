package com.doctorsplaza.app.ui.doctor.fragment.clinics.adapter

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.doctorsplaza.app.databinding.ItemClinicsBinding
import com.doctorsplaza.app.ui.patient.fragments.clinics.model.ClinicData
import com.doctorsplaza.app.utils.clinicRequestOption
import javax.inject.Inject

class DoctorClinicsAdapter @Inject constructor() :
    RecyclerView.Adapter<DoctorClinicsAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val binding: ItemClinicsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            with(binding) {
                clinicHereForDoctors.text = "Click to View Appointments"
                clinicHereForDoctors.paintFlags = clinicHereForDoctors.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                /* clinicHereForDoctors.text = "Click to View Appointments"
                 clinicHereForDoctors.paintFlags = clinicHereForDoctors.paintFlags or Paint.UNDERLINE_TEXT_FLAG

                 clinicName.text = data.clinicName
                 clinicAddress.text = data.location
                 clinicTimings.text = "${data.start_time} - ${data.end_time}"
                 clinicPhone.text = data.clinicContactNumber.toString()
                 Glide.with(context).applyDefaultRequestOptions(clinicRequestOption()).load(data.image).fitCenter().into(clinicImage)
                 clinicHereForDoctors.setOnClickListener {
                     viewAppointmentsClickListener?.let {
                         it(data)
                     }
                 }*/

                clinicHereForDoctors.setOnClickListener {
                    viewAppointmentsClickListener?.let {
                        it(null)
                    }
                }

                root.setOnClickListener {
                        viewClinicsDetailsClickListener?.let {
                        it(null)
                    }
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<ClinicData>() {
        override fun areItemsTheSame(oldItem: ClinicData, newItem: ClinicData): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ClinicData, newItem: ClinicData): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }
    val differ = AsyncListDiffer(this, diffUtil)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DoctorClinicsAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemClinicsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    private var viewAppointmentsClickListener: ((data: ClinicData?) -> Unit)? = null

    private var viewClinicsDetailsClickListener: ((data: ClinicData?) -> Unit)? = null


    override fun onBindViewHolder(holder: DoctorClinicsAdapter.ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = /*differ.currentList.size*/ 10


    fun setOnViewAppointments(listener: (data: ClinicData?) -> Unit) {
        viewAppointmentsClickListener = listener
    }

    fun setOnViewClinicDetails(listener: (data: ClinicData?) -> Unit) {
        viewClinicsDetailsClickListener = listener
    }
}