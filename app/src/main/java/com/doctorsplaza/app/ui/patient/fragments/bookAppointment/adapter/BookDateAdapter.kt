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
import com.doctorsplaza.app.databinding.ItemDateBinding
import com.doctorsplaza.app.ui.patient.fragments.reminder.model.DateModel
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class BookDateAdapter @Inject constructor(): RecyclerView.Adapter<BookDateAdapter.ViewHolder>() {
    private lateinit var  context: Context

    var dayOfWeek: SimpleDateFormat = SimpleDateFormat("E", Locale.getDefault())
    var dateOfWeek: SimpleDateFormat = SimpleDateFormat("dd",Locale.getDefault())
    var dateFormatNew: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())


    inner class  ViewHolder(private val binding: ItemDateBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(dateModel: DateModel) {
            with(binding){
                val dateFormated = dateFormatNew.parse(dateModel.date)
                day.text = dayOfWeek.format(dateFormated).substring(0,1)
                date.text = dateOfWeek.format(dateFormated).toString()
                if(dateModel.selected){
                    root.background = ContextCompat.getDrawable(context, R.drawable.radius_box_selected)
                    date.setTextColor(Color.WHITE)
                    day.setTextColor(Color.WHITE)
                }else{
                    root.background = ContextCompat.getDrawable(context, R.drawable.radius_box)
                    day.setTextColor(ContextCompat.getColor(context,R.color.colorPrimaryLight))
                    date.setTextColor(Color.BLACK)
                }

                root.setOnClickListener {
                    dateSelectedClickListener?.let {
                        it(dateModel.id,adapterPosition)
                    }
                }
            }
        }
    }

   private val diffUtil = object :DiffUtil.ItemCallback<DateModel>(){
        override fun areItemsTheSame(oldItem: DateModel, newItem: DateModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: DateModel, newItem: DateModel): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }
    val differ = AsyncListDiffer(this,diffUtil)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookDateAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(ItemDateBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    private var dateSelectedClickListener: ((data: Int,position:Int) -> Unit)? = null


    override fun onBindViewHolder(holder: BookDateAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size


    fun setOnDateSelected(listener: (data: Int,position:Int) -> Unit) {
        dateSelectedClickListener = listener
    }

}