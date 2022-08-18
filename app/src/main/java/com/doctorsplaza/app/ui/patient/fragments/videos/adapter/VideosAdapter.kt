package com.doctorsplaza.app.ui.patient.fragments.videos.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.doctorsplaza.app.databinding.ItemVideosMainBinding
import com.doctorsplaza.app.ui.patient.fragments.home.model.VideoData
import com.doctorsplaza.app.utils.clinicRequestOption
import javax.inject.Inject

class VideosAdapter @Inject constructor() : RecyclerView.Adapter<VideosAdapter.ViewHolder>() {

    private lateinit var context: Context

    inner class ViewHolder(private val binding: ItemVideosMainBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: VideoData) {
            with(binding) {
                Glide.with(context).load(data.thumbnail).centerCrop().into(videoThumbnail)
                videoTitle.text = data.title
                videoSlug.text = data.slug
                root.setOnClickListener {
                    videoClickListener?.let {
                        it(data)
                    }
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<VideoData>() {
        override fun areItemsTheSame(oldItem: VideoData, newItem: VideoData): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: VideoData, newItem: VideoData): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    val differ = AsyncListDiffer(this, diffUtil)

    private var videoClickListener: ((data: VideoData) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideosAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemVideosMainBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: VideosAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])

    }

    override fun getItemCount(): Int = differ.currentList.size

    fun setOnVideoClickListener(listener: (data: VideoData) -> Unit) {
        videoClickListener = listener
    }
}