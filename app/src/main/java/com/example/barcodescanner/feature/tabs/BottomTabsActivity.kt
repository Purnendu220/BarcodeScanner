package com.example.barcodescanner.feature.tabs

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.example.barcodescanner.BuildConfig
import com.example.barcodescanner.R
import com.example.barcodescanner.extension.applySystemWindowInsets
import com.example.barcodescanner.feature.BaseActivity
import com.example.barcodescanner.feature.tabs.history.BarcodeHistoryFragment
import com.example.barcodescanner.feature.tabs.scan.ScanBarcodeFromCameraFragment
import com.example.barcodescanner.restapi.RetrofitClient
import com.example.barcodescanner.usecase.Logger
import com.example.barcodescanner.usecase.Utils
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_bottom_tabs.*
import kotlinx.coroutines.*

class BottomTabsActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    companion object {
        private const val ACTION_CREATE_BARCODE = "${BuildConfig.APPLICATION_ID}.CREATE_BARCODE"
        private const val ACTION_HISTORY = "${BuildConfig.APPLICATION_ID}.HISTORY"
    }
     var action : String? = null;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom_tabs)

        supportEdgeToEdge()
        initBottomNavigationView()
        action = intent.getStringExtra(RetrofitClient.ACTION_TYPE);

        if (savedInstanceState == null) {
            showInitialFragment()
        }
        callApi();
    }
    private fun callApi(){
        val deviceId = Utils.getDeviceId(this@BottomTabsActivity);
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val call = RetrofitClient.apiInterface.checkDeviceId(deviceId);
               val resp= call.execute()

                if( resp?.body()?.get(0)?.Results=="OK"){

                }else{
                    CoroutineScope(Dispatchers.Main).launch{
                        showDialog("Invalid Device Id: $deviceId ", "Warning");

                    }
                }
            }catch (e: Exception){
                Logger.log(e.fillInStackTrace())
                CoroutineScope(Dispatchers.Main).launch{
                    showDialog("Invalid Device Id : $deviceId", "Warning");

                }
            }


        }

    }

     fun openHistory(){
         bottom_navigation_view.selectedItemId = R.id.item_history

         showFragment(R.id.item_history)
    }
    fun openScan(){
        val intent = Intent(baseContext, BottomTabsActivity::class.java).apply {

        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

        startActivity(intent)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId == bottom_navigation_view.selectedItemId) {
            return false
        }
        showFragment(item.itemId)
        return true
    }

    override fun onBackPressed() {
        if (bottom_navigation_view.selectedItemId == R.id.item_scan) {
            super.onBackPressed()
        } else {
            bottom_navigation_view.selectedItemId = R.id.item_scan
        }
    }

    private fun supportEdgeToEdge() {
        bottom_navigation_view.applySystemWindowInsets(applyBottom = true)
    }

    private fun initBottomNavigationView() {
        bottom_navigation_view.apply {
            setOnNavigationItemSelectedListener(this@BottomTabsActivity)
        }
    }

    private fun showInitialFragment() {
        when (intent?.action) {
            ACTION_CREATE_BARCODE -> bottom_navigation_view.selectedItemId = R.id.item_scan
            ACTION_HISTORY -> bottom_navigation_view.selectedItemId = R.id.item_history
            else -> showFragment(R.id.item_scan)
        }
    }

    private fun showFragment(bottomItemId: Int) {
        val fragment = when (bottomItemId) {
            R.id.item_scan -> ScanBarcodeFromCameraFragment()
//            R.id.item_create -> CreateBarcodeFragment()
            R.id.item_history -> BarcodeHistoryFragment()
//            R.id.item_settings -> SettingsFragment()
            else -> null
        }
        fragment?.apply(::replaceFragment)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.layout_fragment_container, fragment)
            .setReorderingAllowed(true)
            .commit()
    }
}