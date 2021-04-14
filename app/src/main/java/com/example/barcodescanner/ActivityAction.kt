package com.example.barcodescanner

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.barcodescanner.feature.tabs.BottomTabsActivity
import com.example.barcodescanner.restapi.RetrofitClient
import kotlinx.android.synthetic.main.activity_action.*

class ActivityAction : AppCompatActivity(), View.OnClickListener {
    private var action: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_action)
        buttonMale.setOnClickListener(this)
        buttonFemale.setOnClickListener(this)
        buttonNext.setOnClickListener(this)

    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.buttonMale -> {
                textViewGenderError.setVisibility(View.INVISIBLE)
                buttonMale.setBackgroundResource(R.drawable.join_rect)
                buttonFemale.setBackgroundResource(R.drawable.button_white)
                buttonMale.setTextColor(ContextCompat.getColor(baseContext, R.color.white))
                buttonFemale.setTextColor(ContextCompat.getColor(baseContext, R.color.theme_dark_text))
                action = "IN";
            }
            R.id.buttonFemale -> {
                baseContext
                textViewGenderError.setVisibility(View.INVISIBLE)
                buttonMale.setBackgroundResource(R.drawable.button_white)
                buttonFemale.setBackgroundResource(R.drawable.join_rect)
                buttonMale.setTextColor(ContextCompat.getColor(baseContext, R.color.theme_dark_text))
                buttonFemale.setTextColor(ContextCompat.getColor(baseContext, R.color.white))
                action = "OUT";
            }
            R.id.buttonNext -> validateAndNext()
        }
    }
    fun validateAndNext(){
        if(action!=null){
          openScan()
        }else{
            textViewGenderError.visibility=View.VISIBLE

        }
    }

    fun openScan(){
        val intent = Intent(baseContext, BottomTabsActivity::class.java).apply {

        }
        intent.putExtra(RetrofitClient.ACTION_TYPE,action);

        startActivity(intent)
        finish()
    }
}