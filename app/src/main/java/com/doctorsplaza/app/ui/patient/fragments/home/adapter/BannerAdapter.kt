package com.doctorsplaza.app.ui.patient.fragments.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.doctorsplaza.app.databinding.ItemBannerBinding
import com.doctorsplaza.app.ui.patient.fragments.home.model.BannerData
import com.doctorsplaza.app.utils.clinicRequestOption
import javax.inject.Inject

class BannerAdapter @Inject constructor(): RecyclerView.Adapter<BannerAdapter.ViewHolder>() {
    private lateinit var  context: Context

    inner class  ViewHolder(private val itemBannerBinding: ItemBannerBinding):RecyclerView.ViewHolder(itemBannerBinding.root){
        fun bind(data: BannerData) {
            with(itemBannerBinding){
                Glide.with(context).applyDefaultRequestOptions(clinicRequestOption()).load(data.banner_image).into(bannerImage)
            }
        }
    }

   private val diffUtil = object :DiffUtil.ItemCallback<BannerData>(){
        override fun areItemsTheSame(oldItem: BannerData, newItem: BannerData): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: BannerData, newItem: BannerData): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }
    val differ = AsyncListDiffer(this,diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(ItemBannerBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: BannerAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size
}