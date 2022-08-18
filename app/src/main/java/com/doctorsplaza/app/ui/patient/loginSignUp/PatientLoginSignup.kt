package com.doctorsplaza.app.ui.patient.loginSignUp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.doctorsplaza.app.databinding.ActivityPatientLoginSigupBinding
import com.doctorsplaza.app.ui.doctor.DoctorMainActivity
import com.doctorsplaza.app.ui.patient.PatientMainActivity
import com.doctorsplaza.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PatientLoginSignup : AppCompatActivity() {

    private lateinit var binding: ActivityPatientLoginSigupBinding

    @Inject
    lateinit var session: SessionManager

    companion object{
        lateinit var sessionManager: SessionManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientLoginSigupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager(this)
        if (session.isLogin) {
            if (session.loginType == "patient") {
                startActivity(Intent(this, PatientMainActivity::class.java))
                finish()
            } else if (session.loginType == "doctor") {
                startActivity(Intent(this, DoctorMainActivity::class.java))
                finish()
            }
        }
    }
}