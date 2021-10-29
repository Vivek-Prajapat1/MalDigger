package com.example.maldigger

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.Result
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import me.dm7.barcodescanner.zxing.ZXingScannerView


class ScannerView : AppCompatActivity(), ZXingScannerView.ResultHandler {
    var scannerView: ZXingScannerView? = null
    var main: MainActivity? =null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scannerView = ZXingScannerView(this)
        setContentView(scannerView)

        main = MainActivity()

        Dexter.withContext(applicationContext) .withPermission(Manifest.permission.CAMERA) .withListener(object : PermissionListener {
                override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse) {
                    scannerView!!.startCamera()
                }
                override fun onPermissionDenied(permissionDeniedResponse: PermissionDeniedResponse) {
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                }
                override fun onPermissionRationaleShouldBeShown( permissionRequest: PermissionRequest?, permissionToken: PermissionToken)
                {
                    permissionToken.continuePermissionRequest()
                }
            }).check()
    }
    override fun handleResult(rawResult: Result?) {
        main?.
        onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        scannerView!!.stopCamera()
    }

    override fun onResume() {
        super.onResume()
        scannerView!!.setResultHandler(this)
        scannerView!!.startCamera()
    }




}
