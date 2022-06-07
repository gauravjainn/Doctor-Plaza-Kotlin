package com.doctorsplaza.app.ui.patient.fragments.profile.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.doctorsplaza.app.databinding.ItemPatientReportBinding
import com.doctorsplaza.app.ui.patient.fragments.profile.model.PatientReportData
import javax.inject.Inject

class PatientReportAdapter @Inject constructor(): RecyclerView.Adapter<PatientReportAdapter.ViewHolder>() {
    private lateinit var  context: Context
    private var doctorQualification = ""
    inner class  ViewHolder(private val binding: ItemPatientReportBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(data: PatientReportData) {
            with(binding){
               Glide.with(context).load(data.image).into(reportImage)
                deleteReport.setOnClickListener {
                    reportDeleteClickListener?.let {
                        it(data._id)
                    }
                }
            }
        }
    }

   private val diffUtil = object :DiffUtil.ItemCallback<PatientReportData>(){
        override fun areItemsTheSame(oldItem: PatientReportData, newItem: PatientReportData): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: PatientReportData, newItem: PatientReportData): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }
    val differ = AsyncListDiffer(this,diffUtil)

    private var reportDeleteClickListener: ((id: String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientReportAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(ItemPatientReportBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: PatientReportAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    fun setOnReportDeleteClickListener(listener: (id: String) -> Unit) {
        reportDeleteClickListener = listener
    }
}