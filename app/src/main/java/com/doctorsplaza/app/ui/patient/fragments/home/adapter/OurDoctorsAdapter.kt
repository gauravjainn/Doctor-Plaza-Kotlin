package com.doctorsplaza.app.ui.patient.fragments.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.doctorsplaza.app.databinding.ItemOurDoctorBinding
import com.doctorsplaza.app.ui.patient.fragments.clinicDoctors.model.DoctorData
import javax.inject.Inject

class OurDoctorsAdapter @Inject constructor(): RecyclerView.Adapter<OurDoctorsAdapter.ViewHolder>() {
    private lateinit var  context: Context
    inner class  ViewHolder(private val itemOurDoctorBinding: ItemOurDoctorBinding):RecyclerView.ViewHolder(itemOurDoctorBinding.root){
        fun bind(data: DoctorData) {
            with(itemOurDoctorBinding){
                doctorName.text = data.doctorName
                doctorSpecialistIn.text = data.specialization
                doctorRating.text = data.rating.toString()
                doctorDegree.text = data.qualification
                doctorRatingCount.text = "${data.ratings_count}"
                Glide.with(context).load(data.profile_picture).into(doctorImage)
                root.setOnClickListener {
                    doctorClickListener?.let {
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

    private var doctorClickListener: ((data: DoctorData) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OurDoctorsAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(ItemOurDoctorBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: OurDoctorsAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    fun setOnDoctorClickListener(listener: (data: DoctorData) -> Unit) {
        doctorClickListener = listener
    }
}