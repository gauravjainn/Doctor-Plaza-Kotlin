package com.doctorsplaza.app.utils

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.LinearLayout
import com.doctorsplaza.app.R

class DoctorPlazaLoader(context: Context) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doctor_plaza_loader)
        window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        window?.setBackgroundDrawableResource(R.color.white_transparent)
    }
}