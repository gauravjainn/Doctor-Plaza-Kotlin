package com.doctorsplaza.app.ui.doctor.fragment.appointments.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.ItemPatientReportBinding
import com.doctorsplaza.app.ui.patient.fragments.appointments.model.PatientReports
import javax.inject.Inject


class AppointmentPatientReportAdapter @Inject constructor(): RecyclerView.Adapter<AppointmentPatientReportAdapter.ViewHolder>() {
    private lateinit var  context: Context
    inner class  ViewHolder(private val binding: ItemPatientReportBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(data: PatientReports) {
            with(binding){
                deleteReport.isVisible = false

                val extension: String = data.title.substring(data.title.lastIndexOf("."))
                if(extension.lowercase()==".pdf"){
                    Glide.with(context).load(R.drawable.pdf_view).centerCrop().into(reportImage)
                }else{
                    Glide.with(context).load(data.image).into(reportImage)
                }

                root.setOnClickListener {

                    reportClickListener?.let {

                        it(data)
                    }
                }

            }
        }
    }

   private val diffUtil = object :DiffUtil.ItemCallback<PatientReports>(){
        override fun areItemsTheSame(oldItem: PatientReports, newItem: PatientReports): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: PatientReports, newItem: PatientReports): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }
    val differ = AsyncListDiffer(this,diffUtil)

    private var reportClickListener: ((data: PatientReports) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentPatientReportAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(ItemPatientReportBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: AppointmentPatientReportAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    fun setOnReportClickListener(listener: (data: PatientReports) -> Unit) {
        reportClickListener = listener
    }
}