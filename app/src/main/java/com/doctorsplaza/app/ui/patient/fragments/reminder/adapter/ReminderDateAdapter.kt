package com.doctorsplaza.app.ui.patient.fragments.reminder.adapter

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

class ReminderDateAdapter @Inject constructor(): RecyclerView.Adapter<ReminderDateAdapter.ViewHolder>() {
    private lateinit var  context: Context

    var dayOfWeek: SimpleDateFormat = SimpleDateFormat("E", Locale.getDefault())
    var dateofWeek: SimpleDateFormat = SimpleDateFormat("dd",Locale.getDefault())

    var dateFormat: SimpleDateFormat = SimpleDateFormat("dd-MMM-yyyy",Locale.getDefault())
    var dateFormatNew: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    var dateList = ArrayList<String>()
    var selectedPosition = 0

    inner class  ViewHolder(private val binding: ItemDateBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(dateModel: DateModel) {
            with(binding){
              /*  val calendar: Calendar = GregorianCalendar()
                calendar.add(Calendar.DATE, adapterPosition)
                dateList.add(dateFormat.format(calendar.time).toString())*/
                val dateFormated = dateFormatNew.parse(dateModel.date)

                day.text = dayOfWeek.format(dateFormated).substring(0,1)
                date.text = dateofWeek.format(dateFormated).toString()

                if(dateModel.selected){
//                    selectedPosition = adapterPosition
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
//                    dateModel.selected = true
//                    notifyDataSetChanged()
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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderDateAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(ItemDateBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    private var dateSelectedClickListener: ((data: Int,position:Int) -> Unit)? = null


    override fun onBindViewHolder(holder: ReminderDateAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size


    fun setOnDateSelected(listener: (data: Int,position:Int) -> Unit) {
        dateSelectedClickListener = listener
    }

}