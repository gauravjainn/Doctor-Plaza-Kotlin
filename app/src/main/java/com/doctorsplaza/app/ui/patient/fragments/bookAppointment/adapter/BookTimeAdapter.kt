package com.doctorsplaza.app.ui.patient.fragments.bookAppointment.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.ItemTimeBinding
import com.doctorsplaza.app.ui.patient.fragments.addAppointmentForm.model.RoomTimeSlotsData
import javax.inject.Inject

class BookTimeAdapter @Inject constructor() : RecyclerView.Adapter<BookTimeAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val binding: ItemTimeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: RoomTimeSlotsData) {
            with(binding) {
                try {
                    time.text = "${data.timeSlotData.start_time} - ${data.timeSlotData.end_time}"
                    if (data.timeSlotData.isSelected) {
                        root.background =
                            ContextCompat.getDrawable(context, R.drawable.radius_box_selected)
                        time.setTextColor(Color.WHITE)
                        timeAmPm.setTextColor(Color.WHITE)
                    } else {
                        root.background = ContextCompat.getDrawable(context, R.drawable.radius_box)
                        time.setTextColor(Color.BLACK)
                        timeAmPm.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.colorPrimaryLight
                            )
                        )
                    }

                    root.setOnClickListener {
                        selectOnTimeClickListener?.let {
                            it(data, adapterPosition)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<RoomTimeSlotsData>() {
        override fun areItemsTheSame(
            oldItem: RoomTimeSlotsData,
            newItem: RoomTimeSlotsData
        ): Boolean {
            return oldItem._id == newItem._id
        }

        override fun areContentsTheSame(
            oldItem: RoomTimeSlotsData,
            newItem: RoomTimeSlotsData
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }
    val differ = AsyncListDiffer(this, diffUtil)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookTimeAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemTimeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    private var selectOnTimeClickListener: ((data: RoomTimeSlotsData, position: Int) -> Unit)? =
        null


    override fun onBindViewHolder(holder: BookTimeAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    fun setOnTimeSelectedListener(listener: (data: RoomTimeSlotsData, position: Int) -> Unit) {
        selectOnTimeClickListener = listener
    }
}