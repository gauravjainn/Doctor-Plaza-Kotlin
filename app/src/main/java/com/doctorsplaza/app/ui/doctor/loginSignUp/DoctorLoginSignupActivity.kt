package com.doctorsplaza.app.ui.doctor.loginSignUp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.doctorsplaza.app.databinding.ActivityDoctorLoginSignupBinding
import com.doctorsplaza.app.ui.patient.PatientMainActivity
import com.doctorsplaza.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DoctorLoginSignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorLoginSignupBinding

    @Inject
    lateinit var session:SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorLoginSignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(session.isLogin){
            startActivity(Intent(this, PatientMainActivity::class.java))
            finish()
        }
    }
}