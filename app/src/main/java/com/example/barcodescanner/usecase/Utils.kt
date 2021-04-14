package com.example.barcodescanner.usecase

import android.content.Context
import android.provider.Settings.Secure
import android.provider.Settings.Secure.*


object Utils {
    fun getDeviceId(context: Context):String{
        val m_androidId = getString(context.getContentResolver(), ANDROID_ID)
       //return m_androidId;
        return "9620803c8c913468";

    }

}