package com.doctorsplaza.app.ui.patient.fragments.clinics.adapter

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

class ClinicsAdapter @Inject constructor(): RecyclerView.Adapter<ClinicsAdapter.ViewHolder>() {
    private lateinit var  context: Context

    inner class  ViewHolder(private val itemClinicsBinding: ItemClinicsBinding):RecyclerView.ViewHolder(itemClinicsBinding.root){
        fun bind(data: ClinicData) {
            with(itemClinicsBinding){
                clinicName.text = data.clinicName
                clinicAddress.text = data.location
                clinicTimings.text = "${data.start_time} - ${data.end_time}"
                clinicPhone.text = data.clinicContactNumber.toString()
                clinicHereForDoctors.paintFlags = clinicHereForDoctors.paintFlags or Paint.UNDERLINE_TEXT_FLAG

                Glide.with(context).applyDefaultRequestOptions(clinicRequestOption()).load(data.image).fitCenter().into(clinicImage)
                clinicHereForDoctors.setOnClickListener {
                    viewDoctorsClickListener?.let {
                        it(data)
                    }
                }
            }
        }
    }

   private val diffUtil = object :DiffUtil.ItemCallback<ClinicData>(){
        override fun areItemsTheSame(oldItem: ClinicData, newItem: ClinicData): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ClinicData, newItem: ClinicData): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }
    val differ = AsyncListDiffer(this,diffUtil)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClinicsAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(ItemClinicsBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    private var viewDoctorsClickListener: ((data: ClinicData) -> Unit)? = null


    override fun onBindViewHolder(holder: ClinicsAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size


    fun setOnViewDoctors(listener: (data: ClinicData) -> Unit) {
        viewDoctorsClickListener = listener
    }
}