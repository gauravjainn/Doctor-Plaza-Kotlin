package com.doctorsplaza.app.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.doctorsplaza.app.R
import com.doctorsplaza.app.ui.doctor.fragment.prescription.model.Medicine
import com.doctorsplaza.app.ui.patient.fragments.profile.model.UpdatedProfileData
import com.doctorsplaza.app.ui.patient.loginSignUp.PatientLoginSignup
import com.gym.gymapp.utils.SingleLiveEvent
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern


val menuItems = arrayOf(
    CbnMenuItems(
        R.drawable.ic_home, // the icon
        R.drawable.ic_home_selected, // the AVD that will be shown in FAB
        R.id.homeFragment,
        title = "Home"
    ),
    CbnMenuItems(
        R.drawable.ic_hospital,
        R.drawable.ic_hospital_selected,
        R.id.clinicFragment,
        title = "Clinic"
    ),
    CbnMenuItems(
        R.drawable.ic_calendar,
        R.drawable.ic_calendar_selected,
        R.id.appointmentFragment,
        title = "Appointments"
    ),
    CbnMenuItems(
        R.drawable.ic_alarm_clock,
        R.drawable.ic_alarm_selected,
        R.id.reminderFragment,
        title = "Reminder"
    ),
    CbnMenuItems(
        R.drawable.ic_user,
        R.drawable.ic_user_selected,
        R.id.profileFragment,
        title = "User"

    )
)

val doctorMenuItems = arrayOf(
    CbnMenuItems(
        R.drawable.ic_home, // the icon
        R.drawable.ic_home_selected, // the AVD that will be shown in FAB
        R.id.doctorHomeFragment,
        title = "Home"
    ),
    CbnMenuItems(
        R.drawable.ic_hospital,
        R.drawable.ic_hospital_selected,
        R.id.clinicFragment,
        title = "Clinic"
    ),
    CbnMenuItems(
        R.drawable.ic_calendar,
        R.drawable.ic_calendar_selected,
        R.id.doctorAppointmentFragment,
        title = "Appointments"
    ),
    CbnMenuItems(
        R.drawable.ic_report,
        R.drawable.ic_report_filled,
        R.id.revenueReportsFragment,
        title = "Reminder"
    ),
    CbnMenuItems(
        R.drawable.ic_user,
        R.drawable.ic_user_selected,
        R.id.doctorProfileFragment,
        title = "User"

    )
)

fun CharSequence.isValidEmail(): Boolean =
    !TextUtils.isEmpty(this) && Patterns.EMAIL_ADDRESS.matcher(this).matches()

@SuppressLint("CheckResult")
fun clinicRequestOption(): RequestOptions {
    return RequestOptions().apply {
        placeholder(R.drawable.ic_hospital)
        error(R.drawable.ic_hospital).centerInside()
        diskCacheStrategy(DiskCacheStrategy.ALL)
    }
}

@SuppressLint("CheckResult")
fun videoRequestOption(): RequestOptions {
    return RequestOptions().apply {
        placeholder(R.drawable.video_button)
        error(R.drawable.video_button)
        diskCacheStrategy(DiskCacheStrategy.ALL)
    }
}

@SuppressLint("CheckResult")
fun profileRequestOption(): RequestOptions {
    return RequestOptions().apply {
        placeholder(R.drawable.ic_user_image)
        error(R.drawable.ic_user_image).centerInside()
        diskCacheStrategy(DiskCacheStrategy.NONE)
    }
}

@SuppressLint("CheckResult")
fun patientRequestOption(): RequestOptions {
    return RequestOptions().apply {
        placeholder(R.drawable.patient)
        error(R.drawable.patient).centerInside()
        diskCacheStrategy(DiskCacheStrategy.NONE)
    }
}

@SuppressLint("CheckResult")
fun prescriptionRequestOption(): RequestOptions {
    return RequestOptions().apply {
        placeholder(R.drawable.ic_medicine)
        error(R.drawable.ic_medicine).centerInside()
        diskCacheStrategy(DiskCacheStrategy.NONE)
    }
}

@SuppressLint("CheckResult")
fun doctorRequestOption(): RequestOptions {
    return RequestOptions().apply {
        placeholder(R.drawable.doctor_placeholder)
        error(R.drawable.doctor_placeholder).centerInside()
        diskCacheStrategy(DiskCacheStrategy.NONE)
    }
}


fun Context.showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Fragment.showToast(msg: String) {
    Toast.makeText(this.requireContext(), msg, Toast.LENGTH_SHORT).show()
}

fun checkEmail(email: String): Boolean {
    return EMAIL_ADDRESS_PATTERN.matcher(email).matches()
}

private val EMAIL_ADDRESS_PATTERN: Pattern = Pattern.compile(
    "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
            "\\@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+"
)

fun getDateFormatted(date: String, inputPattern: String, outputPattern: String): String {
    val input = SimpleDateFormat(inputPattern, Locale.getDefault())
    val output = SimpleDateFormat(outputPattern, Locale.getDefault())
    return output.format(input.parse(date))
}


fun getRequestBodyFromString(value: String): Any {
    return value.toRequestBody("multipart/form-data".toMediaTypeOrNull())
}

fun getRequestBodyFromFile(key: String, file: File?): MultipartBody.Part? {
    var imagePart: MultipartBody.Part? = null
    if (file != null) {
        val imageBody: RequestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        imagePart = MultipartBody.Part.createFormData(key, file.name, imageBody)
    }
    return imagePart
}

@SuppressLint("SimpleDateFormat")
fun covertTimeToText(dataDate: String?): String? {
    var convTime: String? = null
    val prefix = ""
    val suffix = "Ago"
    try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val pasTime = dateFormat.parse(dataDate)
        val nowTime = Date()
        val dateDiff = nowTime.time - pasTime.time
        val second: Long = TimeUnit.MILLISECONDS.toSeconds(dateDiff)
        val minute: Long = TimeUnit.MILLISECONDS.toMinutes(dateDiff)
        val hour: Long = TimeUnit.MILLISECONDS.toHours(dateDiff)
        val day: Long = TimeUnit.MILLISECONDS.toDays(dateDiff)
        when {
            second < 60 -> {
                convTime = "$second Seconds $suffix"
            }

            minute < 60 -> {
                convTime = "$minute Minutes $suffix"
            }

            hour < 24 -> {
                convTime = "$hour Hours $suffix"
            }

            day >= 7 -> {
                convTime = if (day > 360) {
                    (day / 360).toString() + " Years " + suffix
                } else if (day > 30) {
                    (day / 30).toString() + " Months " + suffix
                } else {
                    (day / 7).toString() + " Week " + suffix
                }
            }

            day < 7 && day == 1L -> {
                convTime = "$day Day $suffix"
            }

            day < 7 -> {
                convTime = "$day Days $suffix"
            }
        }
    } catch (e: ParseException) {
        e.printStackTrace()
        Log.e("ConvTimeE", e.message.toString())
    }
    return convTime
}

fun checkResponseBody(body: Any?): Any? = body?.let { it }

fun checkThrowable(t: Throwable): String {
    return when (t) {
        is IOException ->
            "Network Failure"

        else -> "Conversion Error ${t.message}"

    }
}


fun hideKeyboard(activity: Activity, view: View) {
    /*val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    var view = activity.currentFocus
    if (view == null) {
        view = View(activity)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)*/
    val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm!!.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Activity.showKeyboard(searchSpecialists: EditText) {
    val imm = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm!!.showSoftInput(searchSpecialists, 0)
}

fun Activity.turnScreenOnAndKeyguardOff() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        setShowWhenLocked(true)
        setTurnScreenOn(true)
    } else {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )
    }

    with(getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requestDismissKeyguard(this@turnScreenOnAndKeyguardOff, null)
        }
    }
}

fun logOutUnAuthorized(activity: Activity, message: String) {
    activity.showToast(message)
    activity.startActivity(Intent(activity, PatientLoginSignup::class.java))
    activity.finish()
}


val CALL_RESPONSE_ACTION_KEY = "CALL_RESPONSE_ACTION_KEY"
val CALL_RECEIVE_ACTION = "CALL_RECEIVE_ACTION"
val FCM_DATA_KEY = "FCM_DATA_KEY"
val CALL_CANCEL_ACTION = "CALL_CANCEL_ACTION"


val doctorProfileUpdated = SingleLiveEvent<Boolean>()
val profileImageUpdated = MutableLiveData<String>()
val profileDetailsUpdated = MutableLiveData<UpdatedProfileData>()
val addMedicine: SingleLiveEvent<Medicine>? = SingleLiveEvent()
val callEnded = SingleLiveEvent<String>()

