package com.doctorsplaza.app.ui.patient.fragments.Notifications.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.ItemNotificationBinding
import com.doctorsplaza.app.ui.patient.fragments.Notifications.model.NotificationData
import com.doctorsplaza.app.utils.SessionManager
import com.doctorsplaza.app.utils.covertTimeToText
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class NotificationAdapter @Inject constructor(private val session: SessionManager) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val itemNotificationBinding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(itemNotificationBinding.root) {
        fun bind(data: NotificationData) {
            with(itemNotificationBinding) {


                if (data.is_read) {
                    root.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                } else {
                    root.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.notification_bg
                        )
                    )
                }
                val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val calendar: Calendar = Calendar.getInstance()
                calendar.time = input.parse(data.createdAt)
                calendar.add(Calendar.HOUR_OF_DAY, 5)
                calendar.add(Calendar.MINUTE, 30)
                notification.text = getContentText(data)
                notificationTime.text = covertTimeToText(input.format(calendar.time))
                root.setOnClickListener {
                    notificationClickListener?.let {
                        it(data,adapterPosition)
                    }

                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<NotificationData>() {
        override fun areItemsTheSame(
            oldItem: NotificationData,
            newItem: NotificationData
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: NotificationData,
            newItem: NotificationData
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }
    val differ = AsyncListDiffer(this, diffUtil)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificationAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    private var notificationClickListener: ((data: NotificationData,position:Int) -> Unit)? = null


    override fun onBindViewHolder(holder: NotificationAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size


    private fun getContentText(notifications: NotificationData): String {
        if (session.loginType == "doctor") {
            when (notifications.type) {
                "welcome" -> {
                    return "Hi Dr. " + session.loginName + ", Welcome to Doctors Plaza"
                }
                "doctorturnoffday" -> {
                    return "Hi Dr. " + session.loginName + ",  Your Today's Appointments are cancelled"
                }
                "bookappfordoc" -> {
                    return "Hi Dr. " + session.loginName + ", " + notifications.content.patientname + "  has booked an " +
                            notifications.content.appointmenttype + " appointment on " + notifications.content.bookdate +
                            " Click here for further details."
                }
                "rentdue" -> {
                    return "Hi Dr. " + session.loginName + ",  Your Rent of amount " +
                            notifications.content.amount + " is due for the Month " + notifications.content.amount +
                            ". Please pay before " + notifications.content.amount + "  to continue our services."
                }
                "docaddbycm" -> {
                    return "Hi Dr. " + session.loginName + ",  You've been added by the Clinic manager & " +
                            notifications.content.clinicmanager + " to the clinic " + notifications.content.clinic +
                            ". Clinic" + notifications.content.clinic + "  Welcomes you!!"
                }
                "docaddbyadmin" -> {
                    return "Hi Dr. " + session.loginName + ",  You've been added by the main office. You can " +
                            "now access our services.Check your credentials below and sign in on to " +
                            "Doctors Plaza using those Credentials"
                }
                "docapprovebyadmin" -> {
                    return "Hi Dr. " + session.loginName + ",  You're application has been accepted by the " +
                            "main office.You can now access our services."
                }
                "patappointmetcancel" -> {
                    return "Hi Dr. " + session.loginName + ",  Your Appointment with Patient " + notifications.content.patname +
                            " and " + notifications.content.patid + " has been Cancelled by the Patient."
                }
                "patappointmetres" -> {
                    return "Hi Dr. " + session.loginName + ",  Your Appointment with Patient " + notifications.content.patid +
                            " has been rescheduled by the " + notifications.content.patname + " on " +
                            notifications.content.date + ". Sign in to check the details"
                }
                "adminspenot" -> {
                    return "Hi Dr. " + session.loginName + notifications.content.title +
                            notifications.content.url
                }
                "payrent" -> {
                    return "Hi Dr. " + session.loginName + ", You've successfully paid your " +
                            notifications.content.month + " rent."
                }
                "payrentdue" -> {
                    return "Hi Dr. " + session.loginName + ", Your Rent of amount " +
                            notifications.content.rent + "  is due for the Month" +
                            notifications.content.nextmonth + " Please pay before last day of" +
                            notifications.content.nextmonth + " month to continue our services"
                }
                "docstatus" -> {
                    return "Hi Dr. " + session.loginName + ", You've turned " +
                            notifications.content.status + "   by the main Office. For further details " +
                            "please contact Main Office."
                }
                "joincall" -> {
                    return "Hi Dr. " + session.loginName + ", " + notifications.content.name + " has joined the call"
                }
            }
        } else if (session.loginType == "patient") {
            when (notifications.type) {
                "welcome" -> {
                    return "Hi " + session.loginName + ", Welcome to Doctors Plaza."
                }
                "doctorturnoff" -> {
                    return "Hi " + session.loginName + ", We're sorry to inform you that Doctor " +
                            notifications.content.doctorname + " will not be available today.You can reschedule " +
                            "the appointment to some other day."
                }
                "bookapp" -> {
                    return "Hi " + session.loginName + ", Your Appointment under Doctor " +
                            notifications.content.doctorname + " has been booked Successfully. with Appointment Id " +
                            notifications.content.patid + " of " + notifications.content.docspe + " Specialization in Clinic " +
                            notifications.content.clinic + " on " + notifications.content.bookdate /*+ "  with Issue of  " +
                            notifications.content.problem +  " with " + notifications.content.mode_of_payment + "  Payment Method."*/
                }
                "patappcan" -> {
                    return "Hi " + session.loginName + ", You have cancelled today's appointment with doctor " +
                            notifications.content.doctorname
                }
                "pataddcm" -> {
                    return "Hi " + session.loginName + ", You've been added by the " +
                            notifications.content.clinicmanagername + " to the " + notifications.content.clinic +
                            " Clinic , " + notifications.content.clinic + " Welcomes you!!"
                }
                "patappres" -> {
                    return "Hi " + session.loginName + ", Your Appointment " +
                            notifications.content.patid + " Your appointment has been rescheduled to " + notifications.content.resdate
                }
                "paysuccess" -> {
                    return "Hi " + session.loginName + ", Payment done successfully!! Your appointment with Dr. " +
                            notifications.content.doctorname + " with AppointmentId " + notifications.content.appid
                }
                "reminder" -> {
                    return "It is reminder for " + notifications.content.title
                }
                "adminspenot" -> {
                    return "Hi " + session.loginName + ", " + notifications.content.title +
                            " " + notifications.content.url
                }
                "joincall" -> {
                    return "Hi " + session.loginName + ", Dr. " + notifications.content.name + " has joined the call"
                }
            }
        }
        return notifications.content.title
    }


    fun setOnNotificationClickListener(listener: (data: NotificationData,position:Int) -> Unit) {
        notificationClickListener = listener
    }


}