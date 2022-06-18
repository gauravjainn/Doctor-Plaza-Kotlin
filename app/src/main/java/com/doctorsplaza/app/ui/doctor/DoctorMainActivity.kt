package com.doctorsplaza.app.ui.doctor

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.ActivityDoctorMainBinding
import com.doctorsplaza.app.ui.doctor.fragment.profile.DoctorProfileViewModel
import com.doctorsplaza.app.ui.patient.fragments.profile.ProfileViewModel
import com.doctorsplaza.app.ui.patient.loginSignUp.PatientLoginSignup
import com.doctorsplaza.app.utils.*
import com.doctorsplaza.app.utils.slidingrootnav.SlidingRootNav
import com.doctorsplaza.app.utils.slidingrootnav.SlidingRootNavBuilder
import dagger.hilt.android.AndroidEntryPoint
import hilt_aggregated_deps._dagger_hilt_android_internal_lifecycle_HiltWrapper_HiltViewModelFactory_ActivityCreatorEntryPoint
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        setObserver()
        setOnClickListener()
        setBottomNavigation()
        setNavigationDrawer(savedInstanceState)
    }


    private fun init() {
        profileViewModel.getDoctorProfile()

    }

    private fun setObserver() {
        doctorProfileUpdated.observe(this) {
            if (it) {
                profileViewModel.getDoctorProfile()
            }
        }

        profileViewModel.doctorProfile.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    Glide.with(this).applyDefaultRequestOptions(doctorRequestOption())
                        .load(response.data?.data?.get(0)?.profile_picture).into(drawerProfileImage)
                    Glide.with(this).applyDefaultRequestOptions(doctorRequestOption())
                        .load(response.data?.data?.get(0)?.profile_picture).into(binding.profile)

                    drawerName.text = response.data?.data?.get(0)?.doctorName
                }
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
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:+919910415720")
                startActivity(intent)
            }
            R.id.drawerLogout -> {
                session.isLogin = false
                slidingRootNav.closeMenu()
                startActivity(Intent(this, PatientLoginSignup::class.java))
                finish()
            }

            R.id.navIcon -> {
                slidingRootNav.openMenu()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        hideKeyboard(this)
    }
}