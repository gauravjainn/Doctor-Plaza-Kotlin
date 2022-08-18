package com.doctorsplaza.app.ui.patient.fragments.clinicDoctors.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.doctorsplaza.app.databinding.ItemClinicDoctorsBinding
import com.doctorsplaza.app.databinding.ItemClinicsBinding
import com.doctorsplaza.app.ui.patient.fragments.clinicDoctors.model.DoctorData
import com.doctorsplaza.app.utils.clinicRequestOption
import javax.inject.Inject

class ClinicDoctorsAdapter @Inject constructor(): RecyclerView.Adapter<ClinicDoctorsAdapter.ViewHolder>() {
    private lateinit var  context: Context
    inner class  ViewHolder(private val itemClinicDoctorsBinding: ItemClinicDoctorsBinding):RecyclerView.ViewHolder(itemClinicDoctorsBinding.root){
        fun bind(data: DoctorData) {
            with(itemClinicDoctorsBinding){
                doctorName.text = data.doctorName
                doctorSpecialistIn.text = data.specialization
                doctorRating.text = data.rating.toString()
                doctorRatingCount.text = "(${data.ratings_count})"

                doctorExperience.text = "${data.experience} years"
                doctorDegree.text = data.qualification

                Glide.with(context).applyDefaultRequestOptions(clinicRequestOption()).load(data.profile_picture).fitCenter().into(doctorImage)
                root.setOnClickListener {
                    viewDoctorsClickListener?.let {
                        it(data)
                    }
                }
            }
        }
    }

   private val diffUtil = object :DiffUtil.ItemCallback<DoctorData>(){
        override fun areItemsTheSame(oldItem: DoctorData, newItem: DoctorData): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: DoctorData, newItem: DoctorData): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }
    val differ = AsyncListDiffer(this,diffUtil)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClinicDoctorsAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(ItemClinicDoctorsBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    private var viewDoctorsClickListener: ((data: DoctorData) -> Unit)? = null


    override fun onBindViewHolder(holder: ClinicDoctorsAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size


    fun setOnViewDoctors(listener: (data: DoctorData) -> Unit) {
        viewDoctorsClickListener = listener
    }

}