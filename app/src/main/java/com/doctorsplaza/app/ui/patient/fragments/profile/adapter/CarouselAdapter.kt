package com.doctorsplaza.app.ui.patient.fragments.profile.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.ItemPatientReportBinding
import com.doctorsplaza.app.databinding.ItemReportPhotoViewBinding
import com.doctorsplaza.app.ui.patient.fragments.profile.model.PatientReportData
import javax.inject.Inject

@SuppressLint("ClickableViewAccessibility")
class CarouselAdapter @Inject constructor() : RecyclerView.Adapter<CarouselAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val itemBinding: ItemReportPhotoViewBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(data: String) {
            with(itemBinding) {

                Glide.with(context)
                    .load(data)
                    .into(photoView)

                photoView.setOnTouchListener { v, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            v.parent.requestDisallowInterceptTouchEvent(true)
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            v.parent.requestDisallowInterceptTouchEvent(false)
                        }
                    }
                    false
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(
            oldItem: String,
            newItem: String,
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: String,
            newItem: String,
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }
    val differ = AsyncListDiffer(this, diffUtil)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemReportPhotoViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CarouselAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size
}