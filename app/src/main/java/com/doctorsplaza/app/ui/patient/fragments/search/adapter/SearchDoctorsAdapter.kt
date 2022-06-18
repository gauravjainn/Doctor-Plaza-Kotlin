package com.doctorsplaza.app.ui.patient.fragments.search.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.doctorsplaza.app.databinding.ItemClinicDoctorsBinding
import com.doctorsplaza.app.ui.patient.fragments.clinicDoctors.model.DoctorData
import com.doctorsplaza.app.ui.patient.fragments.search.model.SearchData
import com.doctorsplaza.app.utils.clinicRequestOption
import javax.inject.Inject

class SearchDoctorsAdapter @Inject constructor(): RecyclerView.Adapter<SearchDoctorsAdapter.ViewHolder>() {
    private lateinit var  context: Context
    inner class  ViewHolder(private val itemClinicDoctorsBinding: ItemClinicDoctorsBinding):RecyclerView.ViewHolder(itemClinicDoctorsBinding.root){
        fun bind(data: SearchData) {
                with(itemClinicDoctorsBinding){
                doctorName.text = data.doctorName
                doctorSpecialistIn.text = data.specialization
                doctorRating.text = data.rating.toString()
                doctorRatingCount.text = "(${data.ratings_count})"

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

   private val diffUtil = object :DiffUtil.ItemCallback<SearchData>(){
        override fun areItemsTheSame(oldItem: SearchData, newItem: SearchData): Boolean {
            return oldItem._id == newItem._id
        }

        override fun areContentsTheSame(oldItem: SearchData, newItem: SearchData): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this,diffUtil)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchDoctorsAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(ItemClinicDoctorsBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    private var viewDoctorsClickListener: ((data: SearchData) -> Unit)? = null


    override fun onBindViewHolder(holder: SearchDoctorsAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size


    fun setOnViewDoctors(listener: (data: SearchData) -> Unit) {
        viewDoctorsClickListener = listener
    }

}