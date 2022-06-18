package com.doctorsplaza.app.ui.patient.fragments.notifications

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentNotificationsBinding
import com.doctorsplaza.app.ui.patient.fragments.notifications.adapter.NotificationAdapter
import com.doctorsplaza.app.ui.patient.fragments.notifications.model.NotificationData
import com.doctorsplaza.app.utils.DoctorPlazaLoader
import com.doctorsplaza.app.utils.Resource
import com.google.gson.JsonObject
import com.doctorsplaza.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotificationsFragment : Fragment(R.layout.fragment_notifications), View.OnClickListener {

    private lateinit var binding: FragmentNotificationsBinding

    @Inject
    lateinit var session: SessionManager

    private var currentView: View? = null

    private lateinit var appLoader: DoctorPlazaLoader

    private val notificationViewModel: NotificationViewModel by viewModels()

    @Inject
    lateinit var notificationAdapter: NotificationAdapter

    private var notificationList: MutableList<NotificationData> = ArrayList()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_notifications, container, false)
            binding = FragmentNotificationsBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
            setOnAdapterClickListener()
        }
        return currentView!!
    }


    private fun init() {
        appLoader = DoctorPlazaLoader(requireContext())
        val jsonObject = JsonObject()
        if (session.loginType == "doctor") {
            jsonObject.addProperty("id", session.loginId)
            jsonObject.addProperty("pageNumber", 1)
            notificationViewModel.getDoctorNotifications(jsonObject)
        } else {
            jsonObject.addProperty("id", session.patientId)
            jsonObject.addProperty("pageNumber", 1)
            notificationViewModel.getNotifications(jsonObject)
        }
    }


    private fun setObserver() {
        notificationViewModel.notification.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    appLoader.dismiss()
                    if (response.data?.status == 200) {
                        notificationList.clear()
                        notificationList.addAll(response.data.data)
                        setNotificationRv(notificationList)
                    } else {

                    }
                }
                is Resource.Loading -> {
                    appLoader.show()
                }
                is Resource.Error -> {
                    appLoader.dismiss()
                }
            }
        }
    }

    private fun setNotificationRv(data: List<NotificationData>) {
        notificationAdapter.differ.submitList(data)
        binding.notificationsRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationAdapter
        }
    }


    private fun setOnClickListener() {
        with(binding) {
            backArrow.setOnClickListener(this@NotificationsFragment)
        }
    }

    private fun setOnAdapterClickListener() {
        notificationAdapter.setOnNotificationClickListener { it, position ->
            if (!it.is_read) {
                notificationList[position]._id
            }
            if (session.loginType == "doctor") {
                if (it.type.contains("bookappfordoc")
                    || it.type.contains("patappointmetcancel")
                    || it.type.contains("patappointmetres")
                ) {
                    val bundle = Bundle()
                    bundle.putString(
                        "appointmentId",
                        it.content.appointmentid
                    )
//                    Constants.navigateFragment(findNavController(), R.id.navigation_doctor_appointment_detail, bundle)
                }
            } else {
                if (it.type.contains("bookapp")
                    || it.type.contains("patappcan")
                    || it.type.contains("patappres")
                ) {
                    val bundle = Bundle()
                    bundle.putString(
                        "appointmentId",
                        it.content.appointmentid
                    )
                    findNavController().navigate(R.id.appointmentDetailsFragment)
                } else if (it.type.contains("reminder")) {
                    findNavController().navigate(R.id.reminderFragment)
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backArrow -> {
                findNavController().popBackStack()
            }
        }
    }
}