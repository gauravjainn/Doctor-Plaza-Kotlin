package com.doctorsplaza.app

import android.app.Application
import android.content.Context
import android.content.Intent
import com.doctorsplaza.app.ui.patient.loginSignUp.PatientLoginSignup.Companion.sessionManager
import com.doctorsplaza.app.ui.videoCall.VideoActivity
import com.doctorsplaza.app.utils.SessionManager
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class DoctorPlazaApp :Application(){


    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(applicationContext)
    }


}