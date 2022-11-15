package com.doctorsplaza.app.data

import com.doctorsplaza.app.ui.patient.loginSignUp.PatientLoginSignup.Companion.sessionManager
import com.doctorsplaza.app.utils.BASE_URL
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class RetrofitInstance {

    companion object {
        val gson = GsonBuilder().setLenient().create()

        private val retrofit by lazy {

            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)

            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY

            val builder = OkHttpClient.Builder()
            builder.addInterceptor(interceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .addInterceptor { chain ->
                    val newRequest = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer ${sessionManager.token}")
                        .build()
                    chain.proceed(newRequest)
                }

            Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(builder.build()).build()
        }
        val api: DoctorPlazaApi by lazy {
            retrofit.create(DoctorPlazaApi::class.java)
        }
    }
}