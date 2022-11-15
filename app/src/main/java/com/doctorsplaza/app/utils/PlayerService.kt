package com.doctorsplaza.app.utils

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import androidx.media.VolumeProviderCompat
import com.doctorsplaza.app.service.callNotification.mp
import com.doctorsplaza.app.service.callNotification.r


class PlayerService : Service() {
    override fun onCreate() {
        super.onCreate()
        object : VolumeProviderCompat(VOLUME_CONTROL_RELATIVE,  100,50) {
            override fun onAdjustVolume(direction: Int) {
                Toast.makeText(applicationContext, "yes", Toast.LENGTH_SHORT).show()
                mp?.stop()
                r?.stop()
                /*
                -1 -- volume down
                1 -- volume up
                0 -- volume button released
                 */
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
    }
}