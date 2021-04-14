package com.example.barcodescanner.feature.tabs.scan

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.budiyev.android.codescanner.*
import com.example.barcodescanner.R
import com.example.barcodescanner.di.*
import com.example.barcodescanner.extension.*
import com.example.barcodescanner.feature.barcode.BarcodeActivity
import com.example.barcodescanner.feature.common.dialog.ConfirmBarcodeDialogFragment
import com.example.barcodescanner.feature.tabs.BottomTabsActivity
import com.example.barcodescanner.feature.tabs.scan.file.ScanBarcodeFromFileActivity
import com.example.barcodescanner.model.Barcode
import com.example.barcodescanner.model.Empdata
import com.example.barcodescanner.model.RequestModel
import com.example.barcodescanner.restapi.RetrofitClient
import com.example.barcodescanner.usecase.Logger
import com.example.barcodescanner.usecase.SupportedBarcodeFormats
import com.example.barcodescanner.usecase.Utils
import com.example.barcodescanner.usecase.save
import com.google.zxing.Result
import com.google.zxing.ResultMetadataType
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_scan_barcode_from_camera.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.util.concurrent.TimeUnit

class ScanBarcodeFromCameraFragment : Fragment(), ConfirmBarcodeDialogFragment.Listener {

    companion object {
        private val PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val PERMISSION_REQUEST_CODE = 101
        private const val ZXING_SCAN_INTENT_ACTION = "com.google.zxing.client.android.SCAN"
        private const val CONTINUOUS_SCANNING_PREVIEW_DELAY = 500L
    }
    public var action : String? = null;

    private val vibrationPattern = arrayOf<Long>(0, 350).toLongArray()
    private val disposable = CompositeDisposable()
    private var maxZoom: Int = 0
    private val zoomStep = 5
    private lateinit var codeScanner: CodeScanner
    private var toast: Toast? = null
    private var lastResult: Barcode? = null
    var alertDialog:AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_scan_barcode_from_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        action =   (activity as BottomTabsActivity).action;

        supportEdgeToEdge()
        setDarkStatusBar()
        initScanner()
        initFlashButton()
        handleScanFromFileClicked()
        handleZoomChanged()
        handleDecreaseZoomClicked()
        handleIncreaseZoomClicked()
        requestPermissions()
    }
    private fun callApi(barcode:String){
        val deviceId = activity?.let { Utils.getDeviceId(it) };
        CoroutineScope(IO).launch {
            try{
               val request = deviceId?.let { action?.let { it1 -> RequestModel(barcode, it, it1) } };
                val call = request?.let { RetrofitClient.apiInterface.postAttendence(it) };
                val resp= call?.execute()

                if( resp?.body()?.get(0)?.empdata!=null){

                    CoroutineScope(Dispatchers.Main).launch{
                        showDialog("Scanned successfully","Congrats", resp?.body()?.get(0)?.empdata!!)

                    }


                }

                else{
                    CoroutineScope(Dispatchers.Main).launch{
                        showErrorDialog("Employee data not found please scan proper data","Error")

                    }
                }
            }catch (e:Exception){
                Logger.log(e.fillInStackTrace())

            }


        }

    }

    override fun onResume() {
        super.onResume()
        if (areAllPermissionsGranted()) {
            initZoomSeekBar()
            codeScanner.startPreview()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE && areAllPermissionsGranted(grantResults)) {
            initZoomSeekBar()
            codeScanner.startPreview()
        }
    }

    override fun onBarcodeConfirmed(barcode: Barcode) {
        handleConfirmedBarcode(barcode)
    }

    override fun onBarcodeDeclined() {
        restartPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        setLightStatusBar()
        disposable.clear()
    }

    private fun supportEdgeToEdge() {
        image_view_flash.applySystemWindowInsets(applyTop = true)
        image_view_scan_from_file.applySystemWindowInsets(applyTop = true)
    }

    private fun setDarkStatusBar() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }

        if (settings.isDarkTheme) {
            return
        }

        requireActivity().window.decorView.apply {
            systemUiVisibility = systemUiVisibility xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private fun setLightStatusBar() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }

        if (settings.isDarkTheme) {
            return
        }

        requireActivity().window.decorView.apply {
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private fun initScanner() {
        codeScanner = CodeScanner(requireActivity(), scanner_view).apply {
            camera = if (settings.isBackCamera) {
                CodeScanner.CAMERA_BACK
            } else {
                CodeScanner.CAMERA_FRONT
            }
            autoFocusMode = if (settings.simpleAutoFocus) {
                AutoFocusMode.SAFE
            } else {
                AutoFocusMode.CONTINUOUS
            }
            formats = SupportedBarcodeFormats.FORMATS.filter(settings::isFormatSelected)
            scanMode = ScanMode.SINGLE
            isAutoFocusEnabled = true
            isFlashEnabled = settings.flash
            isTouchFocusEnabled = false
            decodeCallback = DecodeCallback(::handleScannedBarcode)
            errorCallback = ErrorCallback(::showError)
        }
    }

    private fun initZoomSeekBar() {
        scannerCameraHelper.getCameraParameters(settings.isBackCamera)?.apply {
            this@ScanBarcodeFromCameraFragment.maxZoom = maxZoom
            seek_bar_zoom.max = maxZoom
            seek_bar_zoom.progress = zoom
        }
    }

    private fun initFlashButton() {
        layout_flash_container.setOnClickListener {
            toggleFlash()
        }
        image_view_flash.isActivated = settings.flash
    }

    private fun handleScanFromFileClicked() {
        layout_scan_from_file_container.setOnClickListener {
            navigateToScanFromFileScreen()
        }
    }

    private fun handleZoomChanged() {
        seek_bar_zoom.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    codeScanner.zoom = progress
                }
            }
        })
    }

    private fun handleDecreaseZoomClicked() {
        button_decrease_zoom.setOnClickListener {
            decreaseZoom()
        }
    }

    private fun handleIncreaseZoomClicked() {
        button_increase_zoom.setOnClickListener {
            increaseZoom()
        }
    }

    private fun decreaseZoom() {
        codeScanner.apply {
            if (zoom > zoomStep) {
                zoom -= zoomStep
            } else {
                zoom = 0
            }
            seek_bar_zoom.progress = zoom
        }
    }

    private fun increaseZoom() {
        codeScanner.apply {
            if (zoom < maxZoom - zoomStep) {
                zoom += zoomStep
            } else {
                zoom = maxZoom
            }
            seek_bar_zoom.progress = zoom
        }
    }

    private fun handleScannedBarcode(result: Result) {
        if (requireActivity().intent?.action == ZXING_SCAN_INTENT_ACTION) {
            vibrateIfNeeded()
            finishWithResult(result)
            return
        }

        if (settings.continuousScanning && result.equalTo(lastResult)) {
            restartPreviewWithDelay(false)
            return
        }

        vibrateIfNeeded()

        val barcode = barcodeParser.parseResult(result)

        when {
            settings.confirmScansManually -> showScanConfirmationDialog(barcode)
            settings.saveScannedBarcodesToHistory || settings.continuousScanning -> saveScannedBarcode(barcode)
            else -> navigateToBarcodeScreen(barcode)
        }
    }

    private fun handleConfirmedBarcode(barcode: Barcode) {
        when {
            settings.saveScannedBarcodesToHistory || settings.continuousScanning -> saveScannedBarcode(barcode)
            else -> navigateToBarcodeScreen(barcode)
        }
    }

    private fun vibrateIfNeeded() {
        if (settings.vibrate) {
            requireActivity().apply {
                runOnUiThread {
                    applicationContext.vibrator?.vibrateOnce(vibrationPattern)
                }
            }
        }
    }

    private fun showScanConfirmationDialog(barcode: Barcode) {
        val dialog = ConfirmBarcodeDialogFragment.newInstance(barcode)
        dialog.show(childFragmentManager, "")
    }

    private fun saveScannedBarcode(barcode: Barcode) {
            barcodeDatabase.save(barcode, settings.doNotSaveDuplicates)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { id ->
                                lastResult = barcode
                                when (settings.continuousScanning) {
                                    true -> restartPreviewWithDelay(true)
                                    else -> navigateToBarcodeScreen(barcode.copy(id = id))
                                }
                            },
                            ::showError
                    )
                    .addTo(disposable)





    }

    private fun restartPreviewWithDelay(showMessage: Boolean) {
        Completable
            .timer(CONTINUOUS_SCANNING_PREVIEW_DELAY, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (showMessage) {
                    showToast(R.string.fragment_scan_barcode_from_camera_barcode_saved)
                }
                restartPreview()
            }
            .addTo(disposable)
    }

    private fun restartPreview() {
        requireActivity().runOnUiThread {
            codeScanner.startPreview()
        }
    }

    private fun toggleFlash() {
        image_view_flash.isActivated = image_view_flash.isActivated.not()
        codeScanner.isFlashEnabled = codeScanner.isFlashEnabled.not()
    }

    private fun showToast(stringId: Int) {
        toast?.cancel()
        toast = Toast.makeText(requireActivity(), stringId, Toast.LENGTH_SHORT).apply {
            show()
        }
    }

    private fun requestPermissions() {
        permissionsHelper.requestNotGrantedPermissions(requireActivity() as AppCompatActivity, PERMISSIONS, PERMISSION_REQUEST_CODE)
    }

    private fun areAllPermissionsGranted(): Boolean {
       return permissionsHelper.areAllPermissionsGranted(requireActivity(), PERMISSIONS)
    }

    private fun areAllPermissionsGranted(grantResults: IntArray): Boolean {
        return permissionsHelper.areAllPermissionsGranted(grantResults)
    }

    private fun navigateToScanFromFileScreen() {
        ScanBarcodeFromFileActivity.start(requireActivity())
    }

    private fun navigateToBarcodeScreen(barcode: Barcode) {
        if(AadhaarUtil.isAadhaarNumberValid(barcode.text)){
            callApi(barcode.text)

        }else{
            showErrorDialog("Scanned data : "+barcode.text+" \n Please scan properly","Invalid Scan")

        }


       // BarcodeActivity.start(requireActivity(), barcode)
    }

    private fun finishWithResult(result: Result) {
        val intent = Intent()
            .putExtra("SCAN_RESULT", result.text)
            .putExtra("SCAN_RESULT_FORMAT", result.barcodeFormat.toString())

        if (result.rawBytes?.isNotEmpty() == true) {
            intent.putExtra("SCAN_RESULT_BYTES", result.rawBytes)
        }

        result.resultMetadata?.let { metadata ->
            metadata[ResultMetadataType.UPC_EAN_EXTENSION]?.let {
                intent.putExtra("SCAN_RESULT_ORIENTATION", it.toString())
            }

            metadata[ResultMetadataType.ERROR_CORRECTION_LEVEL]?.let {
                intent.putExtra("SCAN_RESULT_ERROR_CORRECTION_LEVEL", it.toString())
            }

            metadata[ResultMetadataType.UPC_EAN_EXTENSION]?.let {
                intent.putExtra("SCAN_RESULT_UPC_EAN_EXTENSION", it.toString())
            }

            metadata[ResultMetadataType.BYTE_SEGMENTS]?.let {
                var i = 0
                @Suppress("UNCHECKED_CAST")
                for (seg in it as Iterable<ByteArray>) {
                    intent.putExtra("SCAN_RESULT_BYTE_SEGMENTS_$i", seg)
                    ++i
                }
            }
        }

        requireActivity().apply {
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
    fun showDialog(message:String,title:String,empdata:Empdata){

         var messagenew = message+" \n Mem Id : ${empdata.MEM_ID} \n Mem Name : ${empdata.MEM_NAME} \n Area Name : ${empdata.AREA_NAME} \n Mem Age : ${empdata.MEM_AGE} \n In Time : ${empdata.IN_TIME} \n Out Time : ${empdata.OUTTIME}"
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(title)
        builder.setMessage(messagenew)
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton("Ok"){dialogInterface, which ->
//            if(activity is BottomTabsActivity){
//                (activity as BottomTabsActivity).openScan()
//            }
            restartPreviewWithDelay(false)

        }

        // Create the AlertDialog
        alertDialog = builder.create()
        // Set other dialog properties
        alertDialog!!.setCancelable(false)
        alertDialog!!.show()

    }
    fun showDialog(message:String,title:String){
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton("Ok"){dialogInterface, which ->
//            if(activity is BottomTabsActivity){
//                (activity as BottomTabsActivity).openScan()
//            }
            restartPreviewWithDelay(false)

        }

        // Create the AlertDialog
        alertDialog = builder.create()
        // Set other dialog properties
        alertDialog!!.setCancelable(false)
        alertDialog!!.show()

    }
    fun showErrorDialog(message:String,title:String){
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton("ReScan"){dialogInterface, which ->
//            if(activity is BottomTabsActivity){
//                (activity as BottomTabsActivity).openScan()
//            }
            restartPreviewWithDelay(false)
        }

        // Create the AlertDialog
        alertDialog = builder.create()
        // Set other dialog properties
        alertDialog!!.setCancelable(false)
        alertDialog!!.show()

    }

}