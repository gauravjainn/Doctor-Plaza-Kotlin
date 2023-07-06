package com.doctorsplaza.app.ui.patient.fragments.profile.adapter

import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.ItemPatientAdminReportBinding
import com.doctorsplaza.app.ui.patient.fragments.profile.model.PatientReportData
import dagger.hilt.android.internal.managers.ViewComponentManager
import javax.inject.Inject


class PatientAdminReportAdapter @Inject constructor() :
    RecyclerView.Adapter<PatientAdminReportAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val binding: ItemPatientAdminReportBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(dataList: List<PatientReportData>, data: PatientReportData) {
            with(binding) {
                val extension: String = data.file_name.substring(data.file_name.lastIndexOf("."))

                if (extension.lowercase() == ".pdf") {
                    Glide.with(context).load(R.drawable.pdf_view).centerCrop().into(reportImage)
                } else {
                    Glide.with(context).load(data.image).into(reportImage)
                }

                reportImage.setOnClickListener {
                    try {
                        if (extension.lowercase() != ".pdf") {
                            val mContext =
                                if (context is ViewComponentManager.FragmentContextWrapper) (context as ContextWrapper).baseContext
                                else context

                            val ft: FragmentTransaction =
                                (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                            val frag = PatientReportAdapter.ImageZoomDialog()
                            frag.setDataList(dataList, frag)
                            frag.show(ft, "dialog")
                        } else {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(data.image)))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    class ImageZoomDialog : DialogFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setStyle(STYLE_NORMAL, R.style.ImageZoomDialog)
        }

        private lateinit var dataList: List<PatientReportData>
        private lateinit var imageZoomDialog: ImageZoomDialog

        fun setDataList(list: List<PatientReportData>, frag: ImageZoomDialog) {
            dataList = list
            imageZoomDialog = frag
        }

        override fun onStart() {
            super.onStart()
            val d: Dialog? = dialog
            if (d != null) {
                val width = ViewGroup.LayoutParams.MATCH_PARENT
                val height = ViewGroup.LayoutParams.MATCH_PARENT
                d.window!!.setLayout(width, height)
            }
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
        ): View {
            val root: View = inflater.inflate(R.layout.imagezoomviewpager, container, false)
            val viewPager: ViewPager = root.findViewById(R.id.viewPager)
            val adapter: ImageZoomAdapter = PatientAdminReportAdapter().ImageZoomAdapter(context!!)
            adapter.setDataList(dataList)
            viewPager.adapter = adapter
            val leftNav: AppCompatImageButton = root.findViewById(R.id.left_nav)
            val rightNav: AppCompatImageButton = root.findViewById(R.id.right_nav)

            // Images left navigation
            leftNav.setOnClickListener {
                try {
                    var tab = viewPager.currentItem
                    if (tab > 0) {
                        tab--
                        viewPager.currentItem = tab
                    } else if (tab == 0) {
                        viewPager.currentItem = 0
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }

            // Images right navigation
            rightNav.setOnClickListener {
                try {
                    var tab = viewPager.currentItem
                    tab++
                    viewPager.currentItem = tab
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
            val btnClose: AppCompatImageView = root.findViewById(R.id.btnClose)
            btnClose.setOnClickListener {
                try {
                    imageZoomDialog.dismiss()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
            return root
        }
    }

    inner class ImageZoomAdapter(var context: Context) : PagerAdapter() {
        var inflater: LayoutInflater? = null

        private lateinit var dataList: List<PatientReportData>

        fun setDataList(list: List<PatientReportData>) {
            dataList = list
        }

        override fun getCount(): Int {
            return dataList.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
//            val imgDisplay: TouchImageView
            inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val itemView: View =
                inflater!!.inflate(R.layout.imagezoomviewpagerlayout, container, false)
//            imgDisplay = itemView.findViewById(R.id.imgDisplay)
            try {
//                Glide.with(context).load(dataList[position].image).into(imgDisplay)
                Glide.with(context).load(dataList[position].image)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
//                            Glide.with(context).load(dataList[position].image).into(imgDisplay)
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
//                            imgDisplay.setImageDrawable(resource)
                            return false
                        }

                    }).into(object : CustomTarget<Drawable?>(1980, 1080) {
                        override fun onResourceReady(
                            resource: Drawable, transition: Transition<in Drawable?>?
                        ) {
//                            imgDisplay.setImageDrawable(resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            Log.e("onLoadCleared", "onLoadCleared")
                        }
                    })
                container.addView(itemView)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return itemView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as LinearLayout)
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<PatientReportData>() {
        override fun areItemsTheSame(
            oldItem: PatientReportData, newItem: PatientReportData
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: PatientReportData, newItem: PatientReportData
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }
    val differ = AsyncListDiffer(this, diffUtil)
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): PatientAdminReportAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemPatientAdminReportBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: PatientAdminReportAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList, differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size
}