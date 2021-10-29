package com.example.maldigger

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.regex.Pattern.compile


open class MainActivity : AppCompatActivity() {

    val context: Context = this
    private val permissionRequestCamera = 0
    private val key = "2e50561b4a38bc74e24303a15f4c4afb404d4a5252470225a4021994806042cb"


    var editText: EditText? = null
    var scanTextView: TextView? = null
    private var scanQrBtn: Button? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        askPermissions()
        extractUrlFromSms()
        scanTextView = findViewById<View>(R.id.scanTextView) as TextView
        scanQrBtn = findViewById<View>(R.id.scanQrBtn) as Button

        scanTextView!!.text = "Scanning"
        scanQrBtn!!.setOnClickListener {
            makeText(context, "Scanning QR Code", LENGTH_SHORT).show()
            startActivity(Intent(applicationContext, ScannerView::class.java))
        }


    }

    fun getKey(): String {
        return key
    }


    //function for asking permissions to Read SMS//
    private fun askPermissions() {

        try {

            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_SMS
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECEIVE_SMS
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECEIVE_MMS
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECEIVE_WAP_PUSH
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val myPermissionRequestSms = 30
                val activity = this
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(
                        Manifest.permission.READ_SMS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.RECEIVE_MMS,
                        Manifest.permission.RECEIVE_WAP_PUSH
                    ),
                    myPermissionRequestSms
                )

            }//end of if block
        } catch (e: Exception) {
            makeText(context, "error in MainClass", LENGTH_LONG).show()
            Log.d("Error ", "error in MainClass $e")
        }

    }//end of permissions method


    //Camera Code
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestCamera) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //startCamera()
            } else {
                makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    //function for searching the latest SMS and extracting URl from it.
    @RequiresApi(Build.VERSION_CODES.O)
    fun extractUrlFromSms() {
        try {

            editText = findViewById<View>(R.id.editText) as EditText

            // Create Inbox box URI
            val inboxURI: Uri = Uri.parse("content://sms/inbox")
            val reqCols = arrayOf("_id", "address", "body")
            val cursor = contentResolver.query(inboxURI, reqCols, null, null, null)
            cursor?.moveToFirst()
            val stringVal = cursor?.getString(2)
            val stringVal1 = stringVal?.split(" ", "-")?.toTypedArray()

            val p =
                compile("(@)?(href=')?(HREF=')?(HREF=\")?(href=\")?(http://)?[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?+%/.\\w]+)?")
            var flag = 0
            if (stringVal1 != null)
                for (i in stringVal1.indices) {

                    if (p.matcher(stringVal1[i]).matches()) {
                        makeText(context, "AutoScanned URL from SMS ", LENGTH_LONG).show()
                        editText?.setText(stringVal1[i])
                        flag++
                        break
                    }
                }

            if (flag == 0) {
                //sendNotification()
                makeText(context, " NO URL in SMS ", LENGTH_LONG).show()
            }

            /*
            if (cursor != null)
                makeText(this, "Success read sms ${stringVal1?.get(5)} ", LENGTH_LONG ).show()

                */

            cursor?.close()
        } catch (e: Exception) {
            Log.d("Error ", "in ScanMsg()  $e ")
            makeText(context, "Error in ScanMsg() $e", LENGTH_LONG).show()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun callAPI(view: View) {
        // val intent = Intent(this, Scan::class.java)
        val editText = findViewById<View>(R.id.editText) as EditText
        val url = editText.text.toString()
        //startActivity(intent)
        val p =
            compile("(@)?(href=')?(HREF=')?(HREF=\")?(href=\")?(http://)?[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?+%/.\\w]+)?")
        try {
            //if url is valid then it gets entered in the database

            if (p.matcher(url).matches()) {

                //calling db handler and inserting url
                Log.d("MainActivity ", "Before DB handler")
                val db = DBHandler(this, null)
                db.addName(url)

                val malicious = postURL(url)
                if (malicious < 10)
                    showCustomToast("Good To Go!!!", this)
                else
                    showCustomToast("Suspicious!!!", this)

            } else {
                makeText(context, "Invalid url", LENGTH_LONG).show()
            }
        } catch (e: IOException) {
            Log.d("error", " $e")
        }
    }


    //function for sending the extracted URL to the server
    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(IOException::class)
    fun postURL(urlToBeScanned: String): Int {

        val gfgPolicy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(gfgPolicy)
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("url", urlToBeScanned).build()
        val request = Request.Builder().url("https://www.virustotal.com/api/v3/urls").post(body)
            .addHeader(
                "x-apikey",
                "2e50561b4a38bc74e24303a15f4c4afb404d4a5252470225a4021994806042cb"
            ).build()
        val client = OkHttpClient()

        val call = client.newCall(request)
        var result = 0
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                val body = response.body!!.string()
                val json = JSONObject(body)
                val data = json.getJSONObject("data")
                var id = data.getString("id")
                id = id.split("-").toTypedArray()[1]
                Log.i("URL Submitted", "ID generated:  $id")
                result = getData(id)
            }
            response.close()

        } catch (e: Exception) {
            Log.i("Error ", "in post() :  $e")

        }

        return result
    }


    //function fot getting the data from the server
    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(IOException::class)
    fun getData(id: String): Int {

        val request = Request.Builder().url("https://www.virustotal.com/api/v3/urls/$id").get()
            .addHeader(
                "x-apikey",
                "2e50561b4a38bc74e24303a15f4c4afb404d4a5252470225a4021994806042cb"
            ).build()
        val client = OkHttpClient()
        val call = client.newCall(request)
        var malicious = 0
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                val body = response.body!!.string()
                val json = JSONObject(body)
                val data = json.getJSONObject("data")
                val attributes = data.getJSONObject("attributes")
                val lastAnalysisStats = attributes.getJSONObject("last_analysis_stats")
                val harmless = lastAnalysisStats.getInt("harmless")
                malicious = lastAnalysisStats.getInt("malicious")
                //val suspicious = lastAnalysisStats.getInt("suspicious")
                Log.i("Successful Scan ", " harmless count: $harmless")

            }
            response.close()
        } catch (e: Exception) {
            Log.i("Error ", "in scan() :  $e")

        }
        return malicious
    }


    fun showCustomToast(message: String, activity: Activity) {
        val layout = activity.layoutInflater.inflate(
            R.layout.activity_scan, activity.findViewById(R.id.toast_container)
        )

        // set the text of the TextView of the message
        val textView = layout.findViewById<TextView>(R.id.toast_text)
        message.also { textView.text = it }

        // use the application extension function
        /*  this.apply { setGravity(Gravity.CENTER, 0, 40)
              duration = LENGTH_LONG
              view = layout
              show()
          }*/
    }


    private val requestCode = 1

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val id: String
        var malicious = 0
        if (requestCode == requestCode && resultCode == Activity.RESULT_OK) {
            if (data == null)
                return
            val uri = data.data?.path
            Log.d("FilePicker1 : ", "$uri")
            if (uri != null) {
                id = CallAPI().postFile(context, uri)
                malicious = CallAPI().getFileData(context, id)
                Log.d("FilePicker2 : ", "$uri  $id  $malicious")
            }

            if (malicious < 5)
                showCustomToast("Good To Go!!!", this)
            else
                showCustomToast("Suspicious!!!", this)
        }
    }

    //function to choose files from the file manager
    fun filePicker(view: View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "*/*" }
        startActivityForResult(intent, requestCode)
    }

}///end of MainActivity class








