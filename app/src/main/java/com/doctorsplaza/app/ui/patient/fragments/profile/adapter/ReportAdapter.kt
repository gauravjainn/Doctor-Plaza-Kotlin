package com.doctorsplaza.app.ui.patient.fragments.profile.adapter

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
import com.doctorsplaza.app.ui.patient.fragments.profile.model.PatientReportData
import javax.inject.Inject

class ReportAdapter @Inject constructor() : RecyclerView.Adapter<ReportAdapter.ViewHolder>() {
    private var showDelete: Boolean = false
    private lateinit var context: Context

    inner class ViewHolder(private val itemBannerBinding: ItemPatientReportBinding) :
        RecyclerView.ViewHolder(itemBannerBinding.root) {
        fun bind(data: PatientReportData) {
            with(itemBannerBinding) {

                deleteReport.isVisible = showDelete

                val extension: String = data.file_name.substring(data.file_name.lastIndexOf("."))

                if (extension.lowercase() == ".pdf") {
                    Glide.with(context).load(R.drawable.pdf_view).centerCrop().into(reportImage)
                } else {
                    Glide.with(context).load(data.image).into(reportImage)
                }
                root.setOnClickListener {
                    if (extension.lowercase() == ".pdf") {
                        pdfClickListener?.let {
                            it(data)
                        }
                    } else {
                        imageClickListener?.let {
                            it(differ.currentList, absoluteAdapterPosition)
                        }
                    }
                }


                deleteReport.setOnClickListener {
                    reportDeleteClickListener?.let {
                        it(data._id)
                    }
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<PatientReportData>() {
        override fun areItemsTheSame(
            oldItem: PatientReportData,
            newItem: PatientReportData,
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: PatientReportData,
            newItem: PatientReportData,
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }
    val differ = AsyncListDiffer(this, diffUtil)

    private var imageClickListener: ((data: List<PatientReportData>, position: Int) -> Unit)? = null
    private var pdfClickListener: ((data: PatientReportData) -> Unit)? = null
    private var reportDeleteClickListener: ((id: String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemPatientReportBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ReportAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size


    fun setOnImageClickListener(listener: (data: List<PatientReportData>, position: Int) -> Unit) {
        imageClickListener = listener
    }

    fun setOnPDFClickListener(listener: (data: PatientReportData) -> Unit) {
        pdfClickListener = listener
    }

    fun setOnReportDeleteClickListener(listener: (id: String) -> Unit) {
        reportDeleteClickListener = listener
    }

    fun showDeleteButton(show:Boolean) {
        showDelete = show
    }
}