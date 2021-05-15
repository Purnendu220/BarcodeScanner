package com.example.barcodescanner.restapi

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://api.amritbottlers.com/api/CMS/"
    const val CHECK_URL = "check_device"
    const val POST_ATTENDANCE = "post_attendance"
    const val GET_DETAILS = "get_summary"

    const val DEVICE_ID = "deviceid"
    const val TERM_MEDIA_TYPE = "media"

    const val SEARCH_TERM = "Michael jackson"
    const val MEDIA_TYPE = "musicVideo"

    const val ACTION_TYPE = "action_type"



    private val retrofitClient: Retrofit.Builder by lazy {

        val levelType: Level = Level.BODY

        val logging = HttpLoggingInterceptor()
        logging.setLevel(levelType)

        val okhttpClient = OkHttpClient.Builder()
        okhttpClient.addInterceptor(logging)

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okhttpClient.build())

            .addConverterFactory(GsonConverterFactory.create())
    }

    val apiInterface: ApiService by lazy {
        retrofitClient
            .build()
            .create(ApiService::class.java)
    }
}