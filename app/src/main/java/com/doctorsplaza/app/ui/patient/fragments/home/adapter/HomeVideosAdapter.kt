package com.doctorsplaza.app.ui.patient.fragments.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.doctorsplaza.app.databinding.ItemVideosBinding
import com.doctorsplaza.app.ui.patient.fragments.home.model.VideoData
import com.doctorsplaza.app.utils.clinicRequestOption
import javax.inject.Inject

class HomeVideosAdapter @Inject constructor(): RecyclerView.Adapter<HomeVideosAdapter.ViewHolder>() {
    private lateinit var  context: Context

    inner class  ViewHolder(private val binding: ItemVideosBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(data: VideoData) {
            with(binding){
                Glide.with(context).applyDefaultRequestOptions(clinicRequestOption()).load(data.thumbnail).into(videoThumbnail)
                videoTitle.text = data.title
                root.setOnClickListener {
                    videoClickListener?.let {
                        it(data)
                    }
                }
            }
        }
    }

   private val diffUtil = object :DiffUtil.ItemCallback<VideoData>(){
        override fun areItemsTheSame(oldItem: VideoData, newItem: VideoData): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: VideoData, newItem: VideoData): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }
    val differ = AsyncListDiffer(this,diffUtil)

    private var videoClickListener: ((data: VideoData) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeVideosAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(ItemVideosBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: HomeVideosAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])

    }

    override fun getItemCount(): Int = differ.currentList.size

    fun setOnVideoClickListener(listener: (data: VideoData) -> Unit){
        videoClickListener = listener
    }
}