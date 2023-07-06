package com.doctorsplaza.app.service

import android.annotation.SuppressLint
import android.app.*
import android.app.Notification.FOREGROUND_SERVICE_IMMEDIATE
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.doctorsplaza.app.R
import com.doctorsplaza.app.service.callNotification.HeadsUpNotificationActionReceiver
import com.doctorsplaza.app.service.callNotification.HeadsUpNotificationService
import com.doctorsplaza.app.service.callNotification.mp
import com.doctorsplaza.app.service.callNotification.r
import com.doctorsplaza.app.ui.doctor.DoctorMainActivity
import com.doctorsplaza.app.ui.patient.PatientMainActivity
import com.doctorsplaza.app.ui.videoCall.VideoActivity
import com.doctorsplaza.app.utils.*
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.jetbrains.annotations.NotNull
import java.util.*


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
                "subtitle").contains("rejected")
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

            val request = OneTimeWorkRequest.Builder(BackupWorker::class.java).addTag("BACKUP_WORKER_TAG")
            val data = Data.Builder()
            data.putString("title", "Call Started")
            data.putString("subTitle", remoteMessage.data.getValue("subtitle"))
            data.putString("appointmentid", remoteMessage.data.getValue("appointmentid"))
            data.putString("name", remoteMessage.data.getValue("name"))
            request.setInputData(data.build())
            WorkManager.getInstance(applicationContext).enqueue(request.build())


        } else if (remoteMessage.data.getValue("subtitle").contains("rejected")) {
            val mNotificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
            mNotificationManager!!.cancel(120)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
                context?.sendBroadcast(it)
            }
            context?.stopService(Intent(context, HeadsUpNotificationService::class.java))
            callEnded.postValue("rejected")
            session.callStatus = "rejected"
            mp?.stop()
            r?.stop()

        } else {
            mp?.stop()
            r?.stop()
            sendNotification(remoteMessage)
        }
    }


    override fun onNewToken(@NotNull token: String) {
        Log.d("Notification Tag", "Refreshed token: $token")

    }

    private fun sendNotification(remoteMessage: RemoteMessage) {

        val notificationIntent: Intent = if (session.loginType == "doctor") {
            Intent(context, DoctorMainActivity::class.java)
        } else {
            Intent(context, PatientMainActivity::class.java)
        }

        if (remoteMessage.data.containsKey("type") && remoteMessage.data.getValue("type") == "Call Started") {
            notificationIntent.putExtra("appointmentId",
                remoteMessage.data.getValue("appointmentid"))
            notificationIntent.putExtra("from", "notification")
        }

        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
            )

        } else {
            PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val channelId = getString(R.string.default_notification_channel_id)
        val soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context!!.packageName + R.raw.notification_audio)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(this, channelId)

        builder.setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_stethoscope)
            .setColor(context!!.resources.getColor(R.color.colorPrimary))
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(false)
            .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(NotificationCompat.BigTextStyle())
            .setContentTitle(remoteMessage.data.getValue("title"))
            .setContentText(remoteMessage.data.getValue("subtitle"))

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O /*&& prefManager.getBoolean(Constants.NOTIFICATION)*/) {
            builder.setSound(soundUri)
        }

        val notification = builder.build()

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
        notification.flags = Notification.FLAG_AUTO_CANCEL
        notificationManager.notify(0, notification)

    }

}

private val CHANNEL_ID = "VoipChannel"
private val CHANNEL_NAME = "Voip Channel"

class BackupWorker(val context: Context, val workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    lateinit var notificationBuilder: Notification

    override fun doWork(): Result {
        //call methods to perform background task
        Log.d("TAG", "Running Background tasks...")


        val notificationManager =
            context.getSystemService(FirebaseMessagingService.NOTIFICATION_SERVICE) as NotificationManager

        val data: Bundle? = null
        val title: String = inputData.getString("title").toString()
        val subTitle: String = inputData.getString("subTitle").toString()
        val videoToken: String = inputData.getString("token").toString()
        val appointmentid: String = inputData.getString("appointmentid").toString()
        val name: String = inputData.getString("name").toString()

        try {
            val receiveCallAction =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Intent(applicationContext, HeadsUpNotificationActionReceiver::class.java).also {
                        it.putExtra(CALL_RESPONSE_ACTION_KEY, CALL_RECEIVE_ACTION)
                        it.putExtra("videoToken", videoToken)
                        it.putExtra("appointmentid", appointmentid)
                        it.putExtra("name", name)
                        it.putExtra(FCM_DATA_KEY, data)
                        it.action = "RECEIVE_CALL"
                    }
                } else {
                    Intent(
                        applicationContext,
                        HeadsUpNotificationActionReceiver::class.java
                    ).apply {

                        putExtra(CALL_RESPONSE_ACTION_KEY, CALL_RECEIVE_ACTION)
                        putExtra("videoToken", videoToken)
                        putExtra("appointmentid", appointmentid)
                        putExtra("name", name)
                        putExtra(FCM_DATA_KEY, data)
                        action = "RECEIVE_CALL"
                    }
                }

            receiveCallAction.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP


            val receiveCallPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getBroadcast(
                    context,
                    1200,
                    receiveCallAction,
                    PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getBroadcast(
                    context,
                    1200,
                    receiveCallAction,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }


            val cancelCallAction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Intent(context, HeadsUpNotificationActionReceiver::class.java).also {
                    it.putExtra(CALL_RESPONSE_ACTION_KEY, CALL_CANCEL_ACTION)
                    it.putExtra(FCM_DATA_KEY, data)
                    it.putExtra("appointmentid", appointmentid)
                    it.action = "CANCEL_CALL"
                }
            } else {
                Intent(applicationContext, HeadsUpNotificationActionReceiver::class.java).apply {
                    putExtra(CALL_RESPONSE_ACTION_KEY, CALL_CANCEL_ACTION)
                    putExtra(FCM_DATA_KEY, data)
                    putExtra("appointmentid", appointmentid)
                    action = "CANCEL_CALL"
                }
            }


            val cancelCallPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getBroadcast(
                    context,
                    1201,
                    cancelCallAction,
                    PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getBroadcast(
                    context,
                    1201,
                    cancelCallAction,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

            createChannel()
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)


            mp = MediaPlayer.create(context, defaultSoundUri)
            mp?.setOnCompletionListener { mp -> mp.release() }
            mp?.start()

//            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL)
//            r = RingtoneManager.getRingtone(applicationContext, notification)
//            r?.isLooping = true
//            r?.play()


            val viewCallAction =
                Intent(context, HeadsUpNotificationActionReceiver::class.java)
            val viewCallPendingIntent = PendingIntent.getActivity(
                context,
                1202,
                viewCallAction,
                PendingIntent.FLAG_IMMUTABLE
            )

            notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setTicker("Call")
                .setContentText(subTitle)
                .setUsesChronometer(false)
                .setSmallIcon(R.drawable.ic_calling)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setVibrate(null)
                .setOngoing(false)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setFullScreenIntent(viewCallPendingIntent, true)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .addAction(R.drawable.ic_call_green, "Answer", receiveCallPendingIntent)
                .addAction(R.drawable.end_call, "Decline", cancelCallPendingIntent)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .build()

            val incomingCallNotification: Notification?
            incomingCallNotification = notificationBuilder
            notificationManager.notify(120, incomingCallNotification)
//            applicationContext.startForeground(120, incomingCallNotification)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Result.success()
    }

    companion object {
        private const val TAG = "BackupWorker"
    }

    private fun createChannel() {
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val att = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Call Notifications"
//            channel.setSound(defaultSoundUri, att)
            Objects.requireNonNull(applicationContext.getSystemService(NotificationManager::class.java))
                .createNotificationChannel(channel)
        }
    }
}