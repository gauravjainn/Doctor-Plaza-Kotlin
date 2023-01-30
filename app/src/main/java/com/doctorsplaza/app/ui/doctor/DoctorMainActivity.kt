package com.doctorsplaza.app.ui.doctor


import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.provider.Settings
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.ActivityDoctorMainBinding
import com.doctorsplaza.app.ui.doctor.fragment.profile.DoctorProfileViewModel
import com.doctorsplaza.app.ui.patient.loginSignUp.PatientLoginSignup
import com.doctorsplaza.app.utils.*
import com.doctorsplaza.app.utils.slidingrootnav.SlidingRootNav
import com.doctorsplaza.app.utils.slidingrootnav.SlidingRootNavBuilder
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class DoctorMainActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var binding: ActivityDoctorMainBinding

    private lateinit var navController: NavController

    private lateinit var slidingRootNav: SlidingRootNav

    private val profileViewModel: DoctorProfileViewModel by viewModels()

    @Inject
    lateinit var session: SessionManager

    private lateinit var drawerProfileImage: ImageView

    private lateinit var drawerHome: TextView

    private lateinit var drawerSettings: TextView

    private lateinit var drawerContactUs: TextView

    private lateinit var drawerCustomerSupport: TextView

    private lateinit var drawerLogout: TextView

    private lateinit var drawerName: TextView

    lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        addFcmToken()
        setObserver()
        setOnClickListener()
        setBottomNavigation()
        setNavigationDrawer(savedInstanceState)
    }

    private fun addFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }

            val token = task.result.toString()
            val jsonObject = JsonObject()
            jsonObject.addProperty("token", token)
            jsonObject.addProperty("type", session.loginType)
            jsonObject.addProperty("phone", session.mobile)
            jsonObject.addProperty("doctorid", session.loginId)
            jsonObject.addProperty("device_type", "android")
            profileViewModel.refreshToken(jsonObject)

        })
    }


    private fun init() {
        profileViewModel.getDoctorProfile()

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



            if (!Settings.canDrawOverlays(this@DoctorMainActivity)) {
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

    private fun setObserver() {
        doctorProfileUpdated.observe(this) {
            if (it) {
                profileViewModel.getDoctorProfile()
            }
        }

        profileViewModel.refreshToken.observe(this) { }

        profileViewModel.doctorProfile.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    Glide.with(this).applyDefaultRequestOptions(doctorRequestOption())
                        .load(response.data?.data?.get(0)?.profile_picture).into(drawerProfileImage)
                    Glide.with(this).applyDefaultRequestOptions(doctorRequestOption())
                        .load(response.data?.data?.get(0)?.profile_picture).into(binding.profile)

                    drawerName.text = response.data?.data?.get(0)?.doctorName
                }
                else -> {}
            }
        }

    }

    private fun setBottomNavigation() {

        navController = findNavController(R.id.navHostFragmentDoctor)
        binding.bottomNavBar.setMenuItems(doctorMenuItems)
        binding.bottomNavBar.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->

            binding.bottomNavBar.isVisible =
                destination.id == R.id.clinicFragment ||
                        destination.id == R.id.doctorAppointmentFragment ||
                        destination.id == R.id.revenueReportsFragment ||
                        destination.id == R.id.doctorProfileFragment ||
                        destination.id == R.id.doctorHomeFragment

            binding.mainAppBar.isVisible = destination.id == R.id.clinicFragment ||
                    destination.id == R.id.doctorAppointmentFragment ||
                    destination.id == R.id.revenueReportsFragment ||
                    destination.id == R.id.doctorProfileFragment ||
                    destination.id == R.id.doctorHomeFragment

            when (destination.id) {
                R.id.clinicFragment -> {
                    binding.appLogo.isVisible = false
                    binding.tabTitle.isVisible = true
                    binding.profile.isVisible = false
                    binding.tabTitle.text = "Clinic"
                }
                R.id.doctorAppointmentFragment -> {
                    binding.appLogo.isVisible = false
                    binding.tabTitle.isVisible = true
                    binding.profile.isVisible = false
                    binding.tabTitle.text = "Appointment History"
                }
                R.id.revenueReportsFragment -> {
                    binding.appLogo.isVisible = false
                    binding.tabTitle.isVisible = true
                    binding.profile.isVisible = false
                    binding.tabTitle.text = "Report"
                }
                R.id.doctorProfileFragment -> {
                    binding.appLogo.isVisible = false
                    binding.tabTitle.isVisible = true
                    binding.profile.isVisible = false
                    binding.tabTitle.text = "User"
                }
                R.id.doctorHomeFragment -> {
                    binding.appLogo.isVisible = true
                    binding.profile.isVisible = true
                    binding.tabTitle.isVisible = false
                }
            }
        }
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


        Glide.with(this).applyDefaultRequestOptions(doctorRequestOption()).load(session.loginImage)
            .into(drawerProfileImage)
        drawerProfileImage.setOnClickListener(this@DoctorMainActivity)
        drawerHome.setOnClickListener(this@DoctorMainActivity)
        drawerSettings.setOnClickListener(this@DoctorMainActivity)
        drawerContactUs.setOnClickListener(this@DoctorMainActivity)
        drawerCustomerSupport.setOnClickListener(this@DoctorMainActivity)
        drawerLogout.setOnClickListener(this@DoctorMainActivity)
        drawerName.text = session.loginName
    }

    private fun setOnClickListener() {
        binding.notifications.setOnClickListener(this@DoctorMainActivity)
        binding.profile.setOnClickListener(this@DoctorMainActivity)
        binding.navIcon.setOnClickListener(this@DoctorMainActivity)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.notifications -> {
                navController.navigate(R.id.doctorNotificationsFragment)
            }
            R.id.profile -> {
                slidingRootNav.closeMenu()
                navController.navigate(R.id.doctorProfileFragment)
            }
            R.id.profileImage -> {
                slidingRootNav.closeMenu()
                navController.navigate(R.id.doctorProfileFragment)
            }
            R.id.drawerHome -> {
                slidingRootNav.closeMenu()
                navController.navigate(R.id.doctorHomeFragment)
            }
            R.id.drawerSettings -> {
                slidingRootNav.closeMenu()
                navController.navigate(R.id.doctorsSettingsFragment)
            }
            R.id.drawerContactUs -> {
                slidingRootNav.closeMenu()
                navController.navigate(R.id.doctorContactUsFragment)
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
        jsonObject.addProperty("id", session.loginId)
        jsonObject.addProperty("type", session.loginType)
        profileViewModel.logout(jsonObject = jsonObject)

        session.isLogin = false
        slidingRootNav.closeMenu()
        startActivity(Intent(this, PatientLoginSignup::class.java))
        finish()
    }

    override fun onResume() {
        chekOverLayPermission()
        hideKeyboard(this, binding.profile)
        super.onResume()
    }

    /*  override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
          if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
              mp?.stop()
              r?.stop()
          }
          return true
      }*/
}