package com.doctorsplaza.app.service.callNotification

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.doctorsplaza.app.R
import com.doctorsplaza.app.ui.doctor.DoctorMainActivity
import com.doctorsplaza.app.ui.patient.PatientMainActivity
import com.doctorsplaza.app.ui.videoCall.VideoActivity
import com.doctorsplaza.app.utils.*
import java.util.*


var mp: MediaPlayer? = null

class HeadsUpNotificationService : Service() {
    private val CHANNEL_ID = "VoipChannel"
    private val CHANNEL_NAME = "Voip Channel"
    lateinit var notificationBuilder: Notification

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var data: Bundle? = null
        var title: String = ""
        var subTitle: String = ""
        var videoToken: String = ""
        var appointmentid: String = ""
        var name: String = ""
        if (intent != null && intent.extras != null) {
            title = intent.getStringExtra("title").toString()
            subTitle = intent.getStringExtra("subTitle").toString()
            videoToken = intent.getStringExtra("token").toString()
            appointmentid = intent.getStringExtra("appointmentid").toString()
            name = intent.getStringExtra("name").toString()
        }
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

            receiveCallAction.flags =Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP


            val receiveCallPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getBroadcast(
                    applicationContext,
                    1200,
                    receiveCallAction,
                    PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getBroadcast(
                    applicationContext,
                    1200,
                    receiveCallAction,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }


            val cancelCallAction =   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Intent(applicationContext, HeadsUpNotificationActionReceiver::class.java).also {
                    it.putExtra(CALL_RESPONSE_ACTION_KEY, CALL_CANCEL_ACTION)
                    it.putExtra(FCM_DATA_KEY, data)
                    it.putExtra("appointmentid", appointmentid)
                    it.action = "CANCEL_CALL"
                }
            } else {Intent(applicationContext, HeadsUpNotificationActionReceiver::class.java).apply {
                putExtra(CALL_RESPONSE_ACTION_KEY, CALL_CANCEL_ACTION)
                putExtra(FCM_DATA_KEY, data)
                putExtra("appointmentid", appointmentid)
                action = "CANCEL_CALL"
            }}



            val cancelCallPendingIntent  = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getBroadcast(
                    applicationContext,
                    1201,
                    cancelCallAction,
                    PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getBroadcast(
                    applicationContext,
                    1201,
                    cancelCallAction,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

            createChannel()
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val am = getSystemService(AUDIO_SERVICE) as AudioManager

            am.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0
            )

            mp = MediaPlayer.create(this, defaultSoundUri)
            mp?.setOnCompletionListener { mp -> mp.release() }
            mp?.start()

            val viewCallAction = Intent(applicationContext, HeadsUpNotificationActionReceiver::class.java)
            val viewCallPendingIntent = PendingIntent.getActivity(
                applicationContext,
                1202,
                viewCallAction,
                PendingIntent.FLAG_IMMUTABLE
            )

            notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
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
                .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .addAction(R.drawable.ic_call_green, "Answer", receiveCallPendingIntent)
                .addAction(R.drawable.end_call, "Decline", cancelCallPendingIntent)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .build()


            val incomingCallNotification: Notification?
            incomingCallNotification = notificationBuilder

            startForeground(120, incomingCallNotification)

            am.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return START_STICKY
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