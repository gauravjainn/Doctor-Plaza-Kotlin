package com.doctorsplaza.app.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.doctorsplaza.app.R
import com.doctorsplaza.app.ui.patient.fragments.profile.model.UpdatedProfileData
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
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

@SuppressLint("CheckResult")
fun clinicRequestOption(): RequestOptions {
    return RequestOptions().apply {
        placeholder(R.drawable.ic_hospital)
        error(R.drawable.ic_hospital).centerInside()
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

val profileImageUpdated = MutableLiveData<String>()
val profileDetailsUpdated = MutableLiveData<UpdatedProfileData>()