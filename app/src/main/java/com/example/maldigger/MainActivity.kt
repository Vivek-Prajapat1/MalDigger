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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        askPermissions()
        extractURLfromSMS()

    }


    //function for asking permissions to Read SMS//
    private fun askPermissions(){

        try {
            //  scanMsg()

            var context:Context = this

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_MMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_WAP_PUSH) != PackageManager.PERMISSION_GRANTED  ) {
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
            makeText(this,"error in MainClass", LENGTH_LONG).show()
            Log.d("Error ","error in MainClass $e")
        }

    }//end of permissions method


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
            var StringVal = cursor?.getString(2)
            val StringVal1 = StringVal?.split(" ","-")?.toTypedArray()

            val p = compile("(@)?(href=')?(HREF=')?(HREF=\")?(href=\")?(http://)?[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?+%/.\\w]+)?")
            var flag = 0
            if (StringVal1 != null)
                for (i in StringVal1.indices){

                if (p.matcher(StringVal1[i]).matches()) {
                    makeText(this, "AutoScanned URL from SMS ", LENGTH_LONG).show()
                    editText.setText(StringVal1[i])
                    flag++
                    break
                    }
                }

            if(flag==0) {
                //sendNotification()
                makeText(this, " NO URL in SMS ", LENGTH_LONG).show()
            }

            /*
            if (cursor != null)
                Toast.makeText(this, "Success read sms ${StringVal1?.get(5)} ", Toast.LENGTH_LONG ).show()
               */

            cursor?.close()
        }
        catch (e: Exception){
                Log.d("Error ","in ScanMsg()  $e ")
                makeText(this, "Error in ScanMsg() $e", LENGTH_LONG).show()
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
                postURL(url)
            } else {
                makeText(this, "Invalid url", LENGTH_LONG).show()
            }
        }
        catch (e: IOException) {
            Log.d("error", " $e")
        }

    }


    //function for sending the extracted URL to the server
    @Throws(IOException::class)
    private fun postURL(scan_url: String) {

        val gfgPolicy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(gfgPolicy)
        val body = MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("url", scan_url).build()
        val request = Request.Builder().url("https://www.virustotal.com/api/v3/urls").post(body).addHeader("x-apikey", "2e50561b4a38bc74e24303a15f4c4afb404d4a5252470225a4021994806042cb").build()
        val client = OkHttpClient()

        val call = client.newCall(request)
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                val body = response.body!!.string()
                val json = JSONObject(body)
                val data = json.getJSONObject("data")
                var id = data.getString("id")
                id = id.split("-").toTypedArray()[1]
                Log.i("URL Submitted", "ID generated:  $id")
                getData(id)
            }
            response.close()

        }
        catch (e:Exception){
            Log.i ( "Error ", "in post() :  $e")

        }
    }


    //function fot getting the data from the server
    @Throws(IOException::class)
    private fun getData(id:String){

        val request = Request.Builder().url("https://www.virustotal.com/api/v3/urls/" + id ).get().addHeader("x-apikey", "2e50561b4a38bc74e24303a15f4c4afb404d4a5252470225a4021994806042cb").build()
        val client = OkHttpClient()
        val call = client.newCall(request)
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                val body = response.body!!.string()
                val json = JSONObject(body)
                val data = json.getJSONObject("data")
                val attributes = data.getJSONObject("attributes")
                val last_analysis_stats = attributes.getJSONObject("last_analysis_stats")
                val harmless = last_analysis_stats.getInt("harmless")
                val malicious = last_analysis_stats.getInt("malicious")
                val suspicious = last_analysis_stats.getInt("suspicious")
                Log.i("Successful Scan ", " harmless count: $harmless")

                if (malicious<10)
                    Toast(this).showCustomToast("Good To Go!!!", this)

                else
                    Toast(this).showCustomToast("Suspicious!!", this)
            }
            response.close()
        }
        catch (e:Exception){
            Log.i ( "Error ", "in scan() :  $e")

        }

    }


    fun Toast.showCustomToast(message: String, activity: Activity) {
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










