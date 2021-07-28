package com.example.maldigger

import android.Manifest
import android.app.*
import android.app.Service.START_STICKY
import android.content.Intent
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
import androidx.core.app.NotificationCompat
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.regex.Pattern

open class MainActivity : AppCompatActivity() {

    var handler: Handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        scanMsg()
        ReadMsg()
    }

    fun scanMsg(){
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val hasReadSmsPermission = checkSelfPermission(Manifest.permission.READ_SMS)
                if (hasReadSmsPermission != PackageManager.PERMISSION_GRANTED) {
                    val REQUEST_CODE_ASK_PERMISSIONS = 30
                    requestPermissions(
                        arrayOf(Manifest.permission.READ_SMS),
                        REQUEST_CODE_ASK_PERMISSIONS
                    )
                    return
                }
            }

            val editText = findViewById<View>(R.id.editText) as EditText

            // Create Inbox box URI
            val inboxURI: Uri = Uri.parse("content://sms/inbox")
            val reqCols = arrayOf("_id", "address", "body")
            val cursor = contentResolver.query(inboxURI, reqCols, null, null, null)
            cursor?.moveToFirst()
            var StringVal = cursor?.getString(2)
            val StringVal1 = StringVal?.split(" ","-")?.toTypedArray()

            val p = Pattern.compile("(@)?(href=')?(HREF=')?(HREF=\")?(href=\")?(http://)?[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?\\+\\%/\\.\\w]+)?")
            var flag = 0
            if (StringVal1 != null)
                for (i in StringVal1.indices){

                if (p.matcher(StringVal1?.get(i)).matches()) {
                    makeText(this, "AutoScanned URL from SMS ", LENGTH_LONG).show()
                    editText.setText(StringVal1?.get(i))
                    flag++
                    break
                    }
                }

            if(flag==0)
                makeText(this," NO URL in SMS ", LENGTH_LONG).show()

            /*
            if (cursor != null)
                Toast.makeText(this, "Success read sms ${StringVal1?.get(5)} ", Toast.LENGTH_LONG ).show()
               */

            cursor?.close()
        }
        catch (e: Exception){
                makeText(this, "Error in ScanMsg() $e", LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
     fun scan(view:View?) {
       // val intent = Intent(this, Scan::class.java)
        val editText = findViewById<View>(R.id.editText) as EditText
        val url = editText.text.toString()
        //startActivity(intent)

        val p: Pattern =
            Pattern.compile("(@)?(href=')?(HREF=')?(HREF=\")?(href=\")?(http://)?[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?\\+\\%/\\.\\w]+)?")
        try {
            if(p.matcher(url).matches()) {
                post(url)
            }
            else
                makeText(this, "Invalid url", LENGTH_LONG).show()
            }
        catch (e: IOException) {
            Log.d("error", " $e")
        }

    }


    @Throws(IOException::class)
    private fun post(scan_url: String) {

        val gfgPolicy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(gfgPolicy)
        val body = MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("url", scan_url).build()
        val request = Request.Builder().url("https://www.virustotal.com/api/v3/urls").post(body).addHeader("x-apikey", "2e50561b4a38bc74e24303a15f4c4afb404d4a5252470225a4021994806042cb").build()
        val client: OkHttpClient = OkHttpClient()

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
                get(id)
            }
            response.close()

        }
        catch (e:Exception){
            Log.i ( "Error ", "in post() :  $e")

        }
    }


    @Throws(IOException::class)
    private fun get(id:String){

        val request = Request.Builder().url("https://www.virustotal.com/api/v3/urls/" + id ).get().addHeader("x-apikey", "2e50561b4a38bc74e24303a15f4c4afb404d4a5252470225a4021994806042cb").build()
        val client:OkHttpClient = OkHttpClient()
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



/*
    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
        startActivity(intent)
    }*/


}  //end of main class

open class ReadMsg: Service(){

    private val SERVICE_NOTIFICATION_ID = 54321
    private val CHANNEL_ID = "Notification service"

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    fun createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_MIN
            val channel = NotificationChannel(CHANNEL_ID, "Notification service", importance)
            channel.description = "CHANNEL DESCRIPTION"
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }

    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_REQUIRE_DEFAULT)
        startActivity(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //this.handler.post(this.runnableCode)
        createNotification()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Notification service")
            .setContentText("Running...")
            .setAutoCancel(false)
            .setOngoing(true)
            .build()
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
             startForeground(1, notification)

        return START_STICKY
    }
}






