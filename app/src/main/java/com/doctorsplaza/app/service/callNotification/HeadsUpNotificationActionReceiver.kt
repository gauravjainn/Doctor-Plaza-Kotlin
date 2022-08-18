package com.doctorsplaza.app.service.callNotification


import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat.getSystemService
import com.doctorsplaza.app.data.Repository
import com.doctorsplaza.app.ui.videoCall.VideoActivity
import com.doctorsplaza.app.utils.*
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val LOCK_SCREEN_KEY = "lockScreenKey"

@AndroidEntryPoint
class HeadsUpNotificationActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var session: SessionManager

    companion object {
        fun build(context: Context, isLockScreen: Boolean): Intent {
            return Intent(context, HeadsUpNotificationActionReceiver::class.java).also {
                it.putExtra(LOCK_SCREEN_KEY, isLockScreen)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null && intent.extras != null) {
            if (mp != null && mp!!.isPlaying) {
                mp?.stop()
            }
            val action = intent.getStringExtra(CALL_RESPONSE_ACTION_KEY)
            action?.let { performClickAction(context, it, intent) }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
                context.sendBroadcast(it)
            }
            context.stopService(Intent(context, HeadsUpNotificationService::class.java))
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun performClickAction(context: Context, action: String, data: Intent) {
        if (action == CALL_RECEIVE_ACTION) {
            val openIntent: Intent?
            try {
                val videoToken = data.getStringExtra("videoToken").toString()
                val appointmentid = data.getStringExtra("appointmentid").toString()
                val name = data.getStringExtra("name").toString()

                if (data.getBooleanExtra(LOCK_SCREEN_KEY, true)) {
                    val fullScreenActivity = Intent(context, VideoActivity::class.java)
                    fullScreenActivity.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    fullScreenActivity.putExtra("videoToken", videoToken)
                    fullScreenActivity.putExtra("appointmentid", appointmentid)
                    fullScreenActivity.putExtra("name", name)
                    val km =
                        context.getSystemService(FirebaseMessagingService.KEYGUARD_SERVICE) as KeyguardManager
                    val locked = km.inKeyguardRestrictedInputMode()
                    if (locked) {
                        fullScreenActivity.putExtra("fromLockScreen", "yes")
                    }

                    context.startActivity(fullScreenActivity)

                    val fullActivityPendingIntent =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            context.startActivity(Intent(context, VideoActivity::class.java))

                            PendingIntent.getActivity(
                                context,
                                1204,
                                fullScreenActivity,
                                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                            )
                        } else {
                            PendingIntent.getActivity(
                                context,
                                1204,
                                fullScreenActivity,
                                PendingIntent.FLAG_ONE_SHOT
                            )
                        }
//                        fullActivityPendingIntent.send()


                } else {
                    openIntent = Intent(context, VideoActivity::class.java).apply {
                        putExtra("videoToken", videoToken)
                        putExtra("appointmentid", appointmentid)
                        putExtra("name", name)
                    }
                    openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(openIntent)
                }


            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
        } else if (action == CALL_CANCEL_ACTION) {
            val repository = Repository()
            val jsonObject = JsonObject().apply {
                addProperty("type", session.loginType)
                addProperty("name", "")
                addProperty("status", "declined")
                addProperty("appointmentId", data.getStringExtra("appointmentid"))
            }

            GlobalScope.launch { repository.callNotify(jsonObject) }
            context.stopService(Intent(context, HeadsUpNotificationService::class.java))
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
                context.sendBroadcast(it)
            }
        }
    }
}