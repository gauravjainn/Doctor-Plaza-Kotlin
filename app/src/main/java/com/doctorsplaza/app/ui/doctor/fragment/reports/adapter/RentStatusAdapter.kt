package com.doctorsplaza.app.ui.doctor.fragment.reports.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.ItemPastAppointmentBinding
import com.doctorsplaza.app.databinding.ItemRentStatusBinding
import com.doctorsplaza.app.ui.doctor.fragment.home.model.AppointmentData
import com.doctorsplaza.app.ui.doctor.fragment.reports.model.RentData
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class RentStatusAdapter @Inject constructor() :
    RecyclerView.Adapter<RentStatusAdapter.ViewHolder>() {
    private lateinit var context: Context

    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    inner class ViewHolder(private val binding: ItemRentStatusBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: RentData) {
            with(binding) {

                val parseDate = inputFormat.parse(data.month)
                val formattedDate = outputFormat.format(parseDate)

                dueLbl.isVisible = data.rent_status == "due"
                rentName.text = data.clinicData.clinicName
                rentDate.text = formattedDate
                rentAmt.text = "₹${data.rentPerMonth}"
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<RentData>() {
        override fun areItemsTheSame(oldItem: RentData, newItem: RentData): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: RentData,
            newItem: RentData
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }
    val differ = AsyncListDiffer(this, diffUtil)

    private var pastAppointmentClickListener: ((id: String) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RentStatusAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemRentStatusBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RentStatusAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    fun setOnPastAppointmentClickListener(listener: (id: String) -> Unit) {
        pastAppointmentClickListener = listener
    }
}