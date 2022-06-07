package com.doctorsplaza.app.data

import com.doctorsplaza.app.utils.BASE_URL
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {
    companion object {
        val gson = GsonBuilder().setLenient().create()

        private val retrofit by lazy {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder().addInterceptor(logging).build()

            Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client).build()
        }
        val api: DoctorPlazaApi by lazy {
            retrofit.create(DoctorPlazaApi::class.java)
        }
    }
}
