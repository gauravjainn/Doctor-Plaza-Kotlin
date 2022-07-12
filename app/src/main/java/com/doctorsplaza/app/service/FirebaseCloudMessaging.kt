package com.doctorsplaza.app.service

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import com.doctorsplaza.app.R
import com.doctorsplaza.app.service.callNotification.HeadsUpNotificationService
import com.doctorsplaza.app.service.callNotification.mp
import com.doctorsplaza.app.ui.patient.PatientMainActivity
import com.doctorsplaza.app.ui.videoCall.VideoActivity
import com.doctorsplaza.app.utils.CALL_RESPONSE_ACTION_KEY
import com.doctorsplaza.app.utils.SessionManager
import com.doctorsplaza.app.utils.callEnded
import com.doctorsplaza.app.utils.showToast
import com.google.android.material.internal.ContextUtils.getActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.jetbrains.annotations.NotNull


class FirebaseCloudMessaging : FirebaseMessagingService() {

    private var context: Context? = null
    private lateinit var session: SessionManager
//    val CHANNEL_ID = getString(R.string.app_name)

    override fun onCreate() {
        super.onCreate()
        context = this
        session = SessionManager(context as FirebaseCloudMessaging)
    }

    @SuppressLint("WrongThread")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        println("From: " + remoteMessage.data.toString())
        if (remoteMessage.data.containsKey("type") && remoteMessage.data.getValue("type") == "Call Started" && !remoteMessage.data.getValue(
                "subtitle"
            ).contains("rejected")
        ) {
            session.callStatus = ""
            val pm = context!!.getSystemService(POWER_SERVICE) as PowerManager
            val isScreenOn = pm.isScreenOn
            if (!isScreenOn) {
                val wl: PowerManager.WakeLock = pm.newWakeLock(
                    PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                    "myApp:MyLock"
                )
                wl.acquire(10000)
                val wlCpu: PowerManager.WakeLock =
                    pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myApp:mycpuMyCpuLock")
                wlCpu.acquire(10000)

            }
            startService(Intent(applicationContext, HeadsUpNotificationService::class.java).apply {
                putExtra("title", "Call Started")
                putExtra("subTitle", remoteMessage.data.getValue("subtitle"))
                putExtra("appointmentid", remoteMessage.data.getValue("appointmentid"))
                putExtra("name", remoteMessage.data.getValue("name"))
            })

        } else if (remoteMessage.data.getValue("subtitle").contains("rejected")) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
                context?.sendBroadcast(it)
            }
            context?.stopService(Intent(context, HeadsUpNotificationService::class.java))
            callEnded.postValue("rejected")
            session.callStatus = "rejected"
            mp?.stop()

        } else {
            sendNotification(remoteMessage)
        }
    }


    override fun onNewToken(@NotNull token: String) {
        Log.d("Notification Tag", "Refreshed token: $token")

    }

    private fun sendNotification(remoteMessage: RemoteMessage) {

        val notificationIntent: Intent = if (session.loginType == "doctor") {
            Intent(context, PatientMainActivity::class.java)
        } else {
            Intent(context, PatientMainActivity::class.java)
        }

        if (remoteMessage.data.containsKey("type") && remoteMessage.data.getValue("type") == "Call Started") {
            notificationIntent.putExtra(
                "appointmentId",
                remoteMessage.data.getValue("appointmentid")
            )
            notificationIntent.putExtra("from", "notification")
        }

        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val channelId = getString(R.string.default_notification_channel_id)
        val soundUri =
            Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context!!.packageName + R.raw.notification_audio)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this, channelId)
        builder.setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_stethoscope)
            .setColor(context!!.resources.getColor(R.color.colorPrimary))
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(false)
            .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle())
            .setContentTitle(remoteMessage.data.getValue("title"))
            .setContentText(remoteMessage.data.getValue("subtitle"))

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O /*&& prefManager.getBoolean(Constants.NOTIFICATION)*/) {
            builder.setSound(soundUri)
        }

        val notification = builder.build()
        try {
            val r: Ringtone = RingtoneManager.getRingtone(context!!.applicationContext, soundUri)
            r.play()
        } catch (e: Exception) {
            println(e.message.toString())
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Doctors Plaza",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.lightColor = Color.GRAY
            channel.enableLights(true)
            channel.description = "Doctors Plaza"
            val audioAttributes: AudioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            channel.setSound(soundUri, audioAttributes)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0, notification)

    }

}