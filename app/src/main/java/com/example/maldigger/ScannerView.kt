package com.example.maldigger

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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

    var objMain: MainActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scannerView = ZXingScannerView(this)
        setContentView(scannerView)


        Dexter.withContext(applicationContext).withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse) {
                    scannerView!!.startCamera()
                }

                override fun onPermissionDenied(permissionDeniedResponse: PermissionDeniedResponse) {
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissionRequest: PermissionRequest?,
                    permissionToken: PermissionToken
                ) {
                    permissionToken.continuePermissionRequest()
                }
            }).check()
    }

    /* val intentIntegrator = IntentIntegrator(this)
     intentIntegrator.setPrompt("Scan a barcode or QR Code")
     intentIntegrator.setOrientationLocked(true)
     intentIntegrator.initiateScan()
     */

    /*override fun onActivityResult(resultCode: Int, requestCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        // if the intentResult is null then
        // toast a message as "cancelled"
        if (intentResult != null) {
            if (intentResult.contents == null) {
                Toast.makeText(baseContext, "Cancelled", Toast.LENGTH_SHORT).show()
            } else {
                // if the intentResult is not null we'll set
                // the content and format of scan message
                main?.scanTextView?.setText(intentResult.contents)
                //messageFormat.setText(intentResult.formatName)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }*/


    override fun handleResult(rawResult: Result?) {
        // Prints scan results
        Log.d("result", rawResult?.text.toString())
        // Prints the scan format (qrcode, pdf417 etc.)
        Log.d("result", rawResult?.barcodeFormat.toString())


        Toast.makeText(this, "" + rawResult?.text, Toast.LENGTH_LONG).show()

        objMain?.editText?.setText(rawResult?.text.toString())
        objMain?.scanTextView?.text = rawResult?.barcodeFormat.toString()
        onBackPressed()
        //scanTextView?.text = rawResult?.text
        /*
        if (rawResult == null){
            main?.showCustomToast("Cancelled",this)
           // Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
        }
        else {
            var link: String = rawResult?.getText().toString()
            main?.scanTextView?.setText(link)
            onBackPressed()
        }*/
    }


    override fun onResume() {
        super.onResume()
        scannerView!!.setResultHandler(this)
        scannerView!!.startCamera()
    }

    override fun onPause() {
        super.onPause()
        scannerView!!.stopCamera()
    }


}
