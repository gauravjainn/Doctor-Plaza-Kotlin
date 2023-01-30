package com.doctorsplaza.app.ui.patient

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.ActivityPatientMainBinding
import com.doctorsplaza.app.service.callNotification.mp
import com.doctorsplaza.app.service.callNotification.r
import com.doctorsplaza.app.ui.patient.fragments.profile.ProfileViewModel
import com.doctorsplaza.app.ui.patient.loginSignUp.PatientLoginSignup
import com.doctorsplaza.app.utils.*
import com.doctorsplaza.app.utils.slidingrootnav.SlidingRootNav
import com.doctorsplaza.app.utils.slidingrootnav.SlidingRootNavBuilder
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class PatientMainActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var binding: ActivityPatientMainBinding
    private lateinit var navController: NavController
    private lateinit var slidingRootNav: SlidingRootNav
    private val profileViewModel: ProfileViewModel by viewModels()

    @Inject
    lateinit var session: SessionManager

    private var drawerOpen = false

    private lateinit  var drawerProfileImage:ImageView
    private lateinit  var drawerHome:TextView
    private lateinit  var drawerSettings:TextView
    private lateinit  var drawerContactUs:TextView
    private lateinit  var drawerCustomerSupport:TextView
    private lateinit  var drawerLogout:TextView
    private lateinit  var drawerName:TextView

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_DoctorPlaza)

        super.onCreate(savedInstanceState)
        binding = ActivityPatientMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setOnClickListener()
        addFcmToken()
        setObserver()
        setBottomNavigation()
        setNavigationDrawer(savedInstanceState)
    }


    private fun chekOverLayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            /*   val permissionDialog = Dialog(this)
               permissionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
               permissionDialog.setCancelable(true)
               permissionDialog.setContentView(R.layout.overlay_screen_permission_dialog)
               permissionDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

               val openSettings = permissionDialog.findViewById<TextView>(R.id.openSettings)

               openSettings.setOnClickListener {
                   if (!Settings.canDrawOverlays(this@DoctorMainActivity)) {
                       startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                           permissionDialog.dismiss()
                   }
               }
               permissionDialog.show()*/



            if (!Settings.canDrawOverlays(this@PatientMainActivity)) {
                val dialog = AlertDialog.Builder(this)
                dialog.setCancelable(false)
                dialog.setMessage(getString(R.string.overlay_permission))
                dialog.setPositiveButton("open settings") { _, _ ->

                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                }
                dialog.show()
            }

        }
    }


    private fun addFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }

            val token = task.result
            val jsonObject = JsonObject()
            jsonObject.addProperty("token", token)
            jsonObject.addProperty("type", session.loginType)
            jsonObject.addProperty("phone", session.loginPhone)
            jsonObject.addProperty("patientid", session.patientId)
            jsonObject.addProperty("device_type", "android")
            profileViewModel.refreshToken(jsonObject)

        })

    }

    private fun setObserver() {
        profileViewModel.getProfile()
        Glide.with(this).applyDefaultRequestOptions(patientRequestOption()).load(session.loginImage).into(binding.profile)
        profileImageUpdated.observe(this){
            Glide.with(this).applyDefaultRequestOptions(patientRequestOption()).load(it).into(binding.profile)
            Glide.with(this).applyDefaultRequestOptions(patientRequestOption()).load(it).into(drawerProfileImage)
        }
        profileDetailsUpdated.observe(this){
            drawerName.text = session.loginName
        }

        profileViewModel.profile.observe(this) { response ->
            when (response) {
                is Resource.Success -> {

                    if (response.data!!.status == 200) {
                        Glide.with(this).applyDefaultRequestOptions(patientRequestOption()).load(response.data.data.profile_picture).into(binding.profile)
                        Glide.with(this).applyDefaultRequestOptions(patientRequestOption()).load(response.data.data.profile_picture).into(drawerProfileImage)
                        drawerName.text = response.data.data.patient_name
                    }
                }
                is Resource.Loading -> { }
                is Resource.Error -> { }

            }
        }
        profileViewModel.refreshToken.observe(this){}
    }


    private fun setBottomNavigation() {
        navController = findNavController(R.id.navHostFragment)

        binding.bottomNavBar.setMenuItems(menuItems)
        binding.bottomNavBar.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->

            binding.bottomNavBar.isVisible =
                destination.id == R.id.clinicFragment ||
                        destination.id == R.id.appointmentFragment ||
                        destination.id == R.id.reminderFragment ||
                        destination.id == R.id.profileFragment ||
                        destination.id == R.id.homeFragment

            binding.mainAppBar.isVisible = destination.id == R.id.clinicFragment ||
                    destination.id == R.id.appointmentFragment ||
                    destination.id == R.id.reminderFragment ||
                    destination.id == R.id.profileFragment ||
                    destination.id == R.id.homeFragment

            when (destination.id) {
                R.id.clinicFragment -> {
                    binding.appLogo.isVisible = false
                    binding.tabTitle.isVisible = true
                    binding.profile.isVisible = false
                    binding.tabTitle.text = "Clinic"
                }
                R.id.appointmentFragment -> {
                    binding.appLogo.isVisible = false
                    binding.tabTitle.isVisible = true
                    binding.profile.isVisible = false
                    binding.tabTitle.text = "Appointment"
                }
                R.id.reminderFragment -> {
                    binding.appLogo.isVisible = false
                    binding.tabTitle.isVisible = true
                    binding.profile.isVisible = false
                    binding.tabTitle.text = "Reminder"
                }
                R.id.profileFragment -> {
                    binding.appLogo.isVisible = false
                    binding.tabTitle.isVisible = true
                    binding.profile.isVisible = false
                    binding.tabTitle.text = "User"
                }
                R.id.homeFragment -> {
                    binding.appLogo.isVisible = true
                    binding.profile.isVisible = true
                    binding.tabTitle.isVisible = false
                }
            }
        }

    }

    private fun setOnClickListener() {
        binding.notifications.setOnClickListener(this@PatientMainActivity)
        binding.navIcon.setOnClickListener(this@PatientMainActivity)
    }

    private fun setNavigationDrawer(savedInstanceState: Bundle?) {
        slidingRootNav = SlidingRootNavBuilder(this)
            .withMenuOpened(false)
            .withContentClickableWhenMenuOpened(false)
            .withSavedState(savedInstanceState)
            .withMenuLayout(R.layout.layout_drawer_menu)
            .inject()
        setDrawerClickListener()
    }

    private fun setDrawerClickListener() {

        val slideLayout = slidingRootNav.layout
         drawerProfileImage = slideLayout.findViewById(R.id.profileImage)
         drawerHome = slideLayout.findViewById(R.id.drawerHome)
         drawerSettings = slideLayout.findViewById(R.id.drawerSettings)
         drawerContactUs = slideLayout.findViewById(R.id.drawerContactUs)
         drawerCustomerSupport = slideLayout.findViewById(R.id.drawerCustomerSupport)
         drawerLogout = slideLayout.findViewById(R.id.drawerLogout)
         drawerName = slideLayout.findViewById(R.id.userName)

        Glide.with(this).applyDefaultRequestOptions(patientRequestOption()).load(session.loginImage).into(drawerProfileImage)
        drawerHome.setOnClickListener(this@PatientMainActivity)
        drawerSettings.setOnClickListener(this@PatientMainActivity)
        drawerContactUs.setOnClickListener(this@PatientMainActivity)
        drawerCustomerSupport.setOnClickListener(this@PatientMainActivity)
        drawerLogout.setOnClickListener(this@PatientMainActivity)
        drawerName.text = session.loginName
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.notifications -> {
                navController.navigate(R.id.notificationsFragment)
            }
            R.id.drawerHome -> {
                slidingRootNav.closeMenu()
                navController.navigate(R.id.homeFragment)
            }
            R.id.drawerSettings -> {
                slidingRootNav.closeMenu()
                navController.navigate(R.id.settingsFragment)
            }
            R.id.drawerContactUs -> {
                slidingRootNav.closeMenu()
                navController.navigate(R.id.contactUsFragment)
            }
            R.id.drawerCustomerSupport -> {
                slidingRootNav.closeMenu()
                val number = Uri.parse("tel:+918929280230")
                val callIntent = Intent(Intent.ACTION_DIAL, number)
                startActivity(callIntent)
            }
            R.id.drawerLogout -> {
                setLogout()

            }

            R.id.navIcon -> {
                slidingRootNav.openMenu()
            }
        }
    }

    private fun setLogout() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("type", session.loginType)
        jsonObject.addProperty("id", session.loginId)
        profileViewModel.logout(jsonObject = jsonObject)

        session.isLogin = false
        slidingRootNav.closeMenu()
        startActivity(Intent(this, PatientLoginSignup::class.java))
        finish()
    }
    /*override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mp?.stop()
            r?.stop()
        }
        return true
    }*/

    override fun onResume() {
        chekOverLayPermission()
        super.onResume()

    }

}