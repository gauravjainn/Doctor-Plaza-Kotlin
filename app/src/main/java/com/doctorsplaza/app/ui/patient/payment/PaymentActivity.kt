package com.doctorsplaza.app.ui.patient.payment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import com.doctorsplaza.app.R
import com.doctorsplaza.app.utils.SessionManager
import com.doctorsplaza.app.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class PaymentActivity : AppCompatActivity(), PaymentResultListener {

    private var consultationFee = ""

    private var orderId = ""
    @Inject
    lateinit var session:SessionManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.hasExtra("consultationFee")) {
            consultationFee = intent?.getStringExtra("consultationFee").toString()
            orderId = intent?.getStringExtra("orderId").toString()
        } else {
            finish()
        }
        startPayment()
    }

    private fun startPayment() {
        val co = Checkout()
        co.setKeyID("rzp_live_MzhGlLKZmluSYv")
        co.setImage(R.drawable.ic_dr_plaza_icon)
        try {
            val options = JSONObject()
            options.put("name", getString(R.string.app_name))
            options.put("description", "Doctor Consultation Fee")
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
            options.put("theme.color", "#0AC03C")
            options.put("currency", "INR")


            options.put("order_id", orderId)

            val amount = consultationFee.toDouble() * 100

            options.put("amount", amount.toString())

            val retryObj = JSONObject()
            retryObj.put("enabled", true)
            retryObj.put("max_count", 4)
            options.put("retry", retryObj)

            val prefill = JSONObject()
            prefill.put("email", session.loginEmail)
            prefill.put("contact", session.loginPhone)

            options.put("prefill", prefill)
            println("options: $options")
            co.open(this, options)
        } catch (e: Exception) {
            showToast( "Error in payment: ${e.message}" )
            println("Error in payment: " + e.message)
        }

    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        val resultIntent = Intent()
        resultIntent.putExtra("payment_id", razorpayPaymentId.toString())
        resultIntent.putExtra("order_id", orderId)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onPaymentError(errorCode: Int, response: String?) {
        showToast(errorCode.toString())
        println("$errorCode  == $response")
        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

}