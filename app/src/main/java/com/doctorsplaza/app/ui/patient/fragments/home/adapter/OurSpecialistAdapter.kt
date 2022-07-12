package com.doctorsplaza.app.ui.patient.fragments.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.ItemOurSpecialistBinding
import com.doctorsplaza.app.ui.patient.fragments.home.model.SpecialistData
import com.doctorsplaza.app.ui.patient.fragments.search.model.SearchData
import com.doctorsplaza.app.utils.clinicRequestOption
import javax.inject.Inject

class OurSpecialistAdapter @Inject constructor() :
    RecyclerView.Adapter<OurSpecialistAdapter.ViewHolder>() {
    private lateinit var context: Context
    private var from = ""

    inner class ViewHolder(private val itemOurSpecialistBinding: ItemOurSpecialistBinding) :
        RecyclerView.ViewHolder(itemOurSpecialistBinding.root) {
        fun bind(data: SpecialistData) {
            with(itemOurSpecialistBinding) {
                Glide.with(context).applyDefaultRequestOptions(clinicRequestOption()).load(data.image).into(specialistIcon)
                specialistName.text = data.name
                root.setOnClickListener {
                    viewDoctorsClickListener?.let {
                        it(data)
                    }
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<SpecialistData>() {
        override fun areItemsTheSame(oldItem: SpecialistData, newItem: SpecialistData): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: SpecialistData, newItem: SpecialistData): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OurSpecialistAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemOurSpecialistBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
    private var viewDoctorsClickListener: ((data: SpecialistData) -> Unit)? = null

    override fun onBindViewHolder(holder: OurSpecialistAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = if (from == "search") {
        differ.currentList.size
    } else {
        if (differ.currentList.size > 9) 9 else differ.currentList.size
    }



    fun setAdapterSize(from: String) {
        this.from = from
    }


    fun setOnViewDoctors(listener: (data: SpecialistData) -> Unit) {
        viewDoctorsClickListener = listener
    }
}