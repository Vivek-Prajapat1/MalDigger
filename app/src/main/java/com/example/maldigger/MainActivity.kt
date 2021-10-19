package com.example.maldigger

import android.Manifest
import android.app.*
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.makeText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.regex.Pattern.*


open class MainActivity : AppCompatActivity() {

    val context :Context = this
    private val PERMISSION_REQUEST_CAMERA = 0
    private val key = "2e50561b4a38bc74e24303a15f4c4afb404d4a5252470225a4021994806042cb"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        askPermissions()
        extractURLfromSMS()
        // Camera Code
        requestCamera()

    }

    fun getKey():String{
        return key
    }


    //function for asking permissions to Read SMS//
    private fun askPermissions(){

        try {

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_MMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_WAP_PUSH) != PackageManager.PERMISSION_GRANTED)
            {
                val MY_PERMISSIONS_REQUEST_SMS = 30
                val activity = this
                ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.READ_SMS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.RECEIVE_MMS,
                        Manifest.permission.RECEIVE_WAP_PUSH),
                    MY_PERMISSIONS_REQUEST_SMS)

            }//end of if block
        }
        catch(e: Exception)
        {
            makeText(context,"error in MainClass", LENGTH_LONG).show()
            Log.d("Error ","error in MainClass $e")
        }

    }//end of permissions method

    //Function for Requesting Camera // Camera Code
    open fun requestCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA ) == PackageManager.PERMISSION_GRANTED)
        {
            startCamera()
        }
        else
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA ))
            {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.CAMERA),PERMISSION_REQUEST_CAMERA)
            }
            else
            {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CAMERA)
            }
        }
    }//end of request camera function //Camera Code

    //Camera Code
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

//Camera Code
    //Method for Starting Camera
    private fun startCamera() {
        TODO("Not yet implemented")
    }
    //end of start camera method //Camera Code Till Now

    //function for searching the latest SMS and extracting URl from it.
    @RequiresApi(Build.VERSION_CODES.O)
    fun extractURLfromSMS(){
        try {

            val editText = findViewById<View>(R.id.editText) as EditText

            // Create Inbox box URI
            val inboxURI: Uri = Uri.parse("content://sms/inbox")
            val reqCols = arrayOf("_id", "address", "body")
            val cursor = contentResolver.query(inboxURI, reqCols, null, null, null)
            cursor?.moveToFirst()
            val StringVal = cursor?.getString(2)
            val StringVal1 = StringVal?.split(" ","-")?.toTypedArray()

            val p = compile("(@)?(href=')?(HREF=')?(HREF=\")?(href=\")?(http://)?[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?+%/.\\w]+)?")
            var flag = 0
            if (StringVal1 != null)
                for (i in StringVal1.indices){

                if (p.matcher(StringVal1[i]).matches()) {
                    makeText(context, "AutoScanned URL from SMS ", LENGTH_LONG).show()
                    editText.setText(StringVal1[i])
                    flag++
                    break
                    }
                }

            if(flag==0) {
                //sendNotification()
                makeText(context, " NO URL in SMS ", LENGTH_LONG).show()
            }

            /*
            if (cursor != null)
                Toast.makeText(this, "Success read sms ${StringVal1?.get(5)} ", Toast.LENGTH_LONG ).show()
               */

            cursor?.close()
        }
        catch (e: Exception){
                Log.d("Error ","in ScanMsg()  $e ")
                makeText(context, "Error in ScanMsg() $e", LENGTH_LONG).show()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun callAPI(view:View?){
       // val intent = Intent(this, Scan::class.java)
        val editText = findViewById<View>(R.id.editText) as EditText
        val url = editText.text.toString()
        //startActivity(intent)

        val p =
            compile("(@)?(href=')?(HREF=')?(HREF=\")?(href=\")?(http://)?[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?+%/.\\w]+)?")
        try {
            if (p.matcher(url).matches()) {
                val malicious = postURL(context,url)
                if (malicious<10)
                    Toast(context).showCustomToast("Good To Go!!!",this)
                else
                    Toast(context).showCustomToast("Suspicious!!!",this)

            } else {
                makeText(context, "Invalid url", LENGTH_LONG).show()
            }
        }
        catch (e: IOException) {
            Log.d("error", " $e")
        }

    }


    //function for sending the extracted URL to the server
    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(IOException::class)
    fun postURL(context: Context, urlToBeScanned: String):Int {

        val gfgPolicy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(gfgPolicy)
        val body = MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("url", urlToBeScanned).build()
        val request = Request.Builder().url("https://www.virustotal.com/api/v3/urls").post(body).addHeader("x-apikey", "2e50561b4a38bc74e24303a15f4c4afb404d4a5252470225a4021994806042cb").build()
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
                result = getData(context,id)
            }
            response.close()

        }
        catch (e:Exception){
            Log.i ( "Error ", "in post() :  $e")

        }

        return result
    }


    //function fot getting the data from the server
    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(IOException::class)
    fun getData(context:Context,id:String): Int {

        val request = Request.Builder().url("https://www.virustotal.com/api/v3/urls/" + id ).get().addHeader("x-apikey", "2e50561b4a38bc74e24303a15f4c4afb404d4a5252470225a4021994806042cb").build()
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
                val last_analysis_stats = attributes.getJSONObject("last_analysis_stats")
                val harmless = last_analysis_stats.getInt("harmless")
                    malicious = last_analysis_stats.getInt("malicious")
                val suspicious = last_analysis_stats.getInt("suspicious")
                Log.i("Successful Scan ", " harmless count: $harmless")

            }
            response.close()
        }
        catch (e:Exception){
            Log.i ( "Error ", "in scan() :  $e")

        }
        return malicious
    }


    fun Toast.showCustomToast(message: String,activity:Activity) {
        val layout = activity.layoutInflater.inflate(
            R.layout.activity_scan,
            activity.findViewById(R.id.toast_container)
        )

        // set the text of the TextView of the message
        val textView = layout.findViewById<TextView>(R.id.toast_text)
        message.also { textView.text = it }

        // use the application extension function
        this.apply {
            setGravity(Gravity.CENTER, 0, 40)
            duration = LENGTH_LONG
            view = layout
            show()
        }
    }


}///end of MainActivity class










