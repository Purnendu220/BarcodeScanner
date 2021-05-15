package com.example.barcodescanner.restapi

import com.example.barcodescanner.model.RequestModel
import com.example.barcodescanner.model.ResponseModel
import com.example.barcodescanner.model.ResponseModelDetails
import com.example.barcodescanner.model.ResponseModelNew
import com.example.barcodescanner.restapi.RetrofitClient.CHECK_URL
import com.example.barcodescanner.restapi.RetrofitClient.DEVICE_ID
import com.example.barcodescanner.restapi.RetrofitClient.GET_DETAILS
import com.example.barcodescanner.restapi.RetrofitClient.POST_ATTENDANCE
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @GET(CHECK_URL)
    fun checkDeviceId(@Query(DEVICE_ID) deviceId:String) : Call<ResponseModel>

    @POST(POST_ATTENDANCE)
    fun postAttendence(@Body term:RequestModel) : Call<ResponseModelNew>

    @GET(GET_DETAILS)
    fun getDetails() : Call<ResponseModelDetails>
}