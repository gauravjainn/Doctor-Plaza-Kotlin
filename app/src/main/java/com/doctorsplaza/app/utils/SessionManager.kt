package com.doctorsplaza.app.utils

import android.content.Context
import android.content.SharedPreferences


class SessionManager(var mcxt: Context) {

    companion object {
        val PREF_NAME = " BINJA"
        val PREF_GENERAL = "PREF_GENERAL"
        val KEY_ISLOGIN = "isloggedin"
        const val KEY_LOGIN_IMAGE = "loginimage"
        const val KEY_LOGIN_NAME = "loginname"
        const val KEY_LOGIN_EMAIL = "loginemail"
        const val KEY_LOGIN_PHONE = "loginphone"
        const val KEY_LOGIN_ADDRESS = "loginaddress"
        const val KEY_LOGIN_GENDER = "logingender"
        const val KEY_LOGIN_DOB = "logindob"
        const val KEY_LOGIN_AGE = "loginage"
        const val KEY_LOGIN_ID = "loginid"
        const val KEY_LOGIN_TYPE = "logintype"
        const val KEY_TOKEN = "token"
        const val KEY_DEVICE_ID = "deviceid"
        const val KEY_LATITUDE = "user_current_lat"
        const val KEY_LONGITUDE = "user_current_lon"
        const val KEY_CART_COUNT = "cart_count"

        const val KEY_PINCODE = "pincode"
        const val KEY_CITY = "city"
        const val KEY_STATE = "state"
        const val KEY_COUNTRY = "country"
        const val KEY_ADDRESS = "address"
        const val KEY_MOBILE = "mobile"

        const val KEY_CART_ID = "cart_id"
        const val KEY_CHECK_AMOUNT = "check_amount"
        const val KEY_CHECK_ADDRESS_ID = "address_id"

        const val KEY_PATIENT_ID = "patientId"
        const val KEY_DOCTOR_ID = "dotorId"

    }


    var generalEditor: SharedPreferences.Editor
    var generalPref: SharedPreferences

    private var PRIVATE_MODE = 0

    init {


        generalPref = mcxt.getSharedPreferences(PREF_GENERAL, PRIVATE_MODE)
        generalEditor = generalPref.edit()

    }

    var isLogin: Boolean
        get() = generalPref.getBoolean(KEY_ISLOGIN, false)
        set(status) {
            generalEditor.putBoolean(KEY_ISLOGIN, status)
            generalEditor.commit()
        }

    var loginImage: String?
        get() = generalPref.getString(KEY_LOGIN_IMAGE, "")
        set(image) {
            generalEditor.putString(KEY_LOGIN_IMAGE, image!!)
            generalEditor.commit()
        }

    var loginName: String?
        get() = generalPref.getString(KEY_LOGIN_NAME, "")
        set(image) {
            generalEditor.putString(KEY_LOGIN_NAME, image!!)
            generalEditor.commit()
        }

    var loginEmail: String?
        get() = generalPref.getString(KEY_LOGIN_EMAIL, "")
        set(image) {
            generalEditor.putString(KEY_LOGIN_EMAIL, image!!)
            generalEditor.commit()
        }

    var loginPhone: String?
        get() = generalPref.getString(KEY_LOGIN_PHONE, "").toString()
        set(phone) {
            generalEditor.putString(KEY_LOGIN_PHONE, phone)
            generalEditor.commit()
        }

    var loginDOB: String
        get() = generalPref.getString(KEY_LOGIN_DOB, "").toString()
        set(dob) {
            generalEditor.putString(KEY_LOGIN_DOB, dob)
            generalEditor.commit()
        }

    var loginAge: String
        get() = generalPref.getString(KEY_LOGIN_AGE, "").toString()
        set(dob) {
            generalEditor.putString(KEY_LOGIN_AGE, dob)
            generalEditor.commit()
        }

    var loginGender: String?
        get() = generalPref.getString(KEY_LOGIN_GENDER, "").toString()
        set(phone) {
            generalEditor.putString(KEY_LOGIN_GENDER, phone)
            generalEditor.commit()
        }

    var loginAddress: String?
        get() = generalPref.getString(KEY_LOGIN_ADDRESS, "").toString()
        set(phone) {
            generalEditor.putString(KEY_LOGIN_ADDRESS, phone)
            generalEditor.commit()
        }

    var loginId: String?
        get() = generalPref.getString(KEY_LOGIN_ID, "")
        set(image) {
            generalEditor.putString(KEY_LOGIN_ID, image!!)
            generalEditor.commit()
        }

    var token: String?
        get() = generalPref.getString(KEY_TOKEN, "")
        set(token) {
            generalEditor.putString(KEY_TOKEN, token!!)
            generalEditor.commit()
        }

    var mobile: String?
        get() = generalPref.getString(KEY_MOBILE, "")
        set(mobile) {
            generalEditor.putString(KEY_MOBILE, mobile!!)
            generalEditor.commit()
        }

    var address: String?
        get() = generalPref.getString(KEY_ADDRESS, "")
        set(pincode) {
            generalEditor.putString(KEY_ADDRESS, pincode!!)
            generalEditor.commit()
        }


    var loginType: String
        get() = generalPref.getString(KEY_LOGIN_TYPE, "").toString()
        set(loginType) {
            generalEditor.putString(KEY_LOGIN_TYPE, loginType)
            generalEditor.commit()
        }


    var patientId: String
        get() = generalPref.getString(KEY_PATIENT_ID, "").toString()
        set(patientID) {
            generalEditor.putString(KEY_PATIENT_ID, patientID)
            generalEditor.commit()
        }

    var doctorId: String
        get() = generalPref.getString(KEY_DOCTOR_ID, "").toString()
        set(doctorID) {
            generalEditor.putString(KEY_DOCTOR_ID, doctorID)
            generalEditor.commit()
        }
}