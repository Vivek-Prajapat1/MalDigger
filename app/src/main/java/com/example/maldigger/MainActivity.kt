package com.example.maldigger
// token for github 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
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun status(view: View?) {
        //val intent = Intent(this, Scan::class.java)
        val editText = findViewById<View>(R.id.editText) as EditText
        val url = editText.text.toString()
        //startActivity(intent)

        try {
            post("https://www.virustotal.com/api/v3/urls",url)
        } catch (e: IOException) {
            Log.d("error", "error to gadbad hai daya $e")
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
            duration = Toast.LENGTH_LONG
            view = layout
            show()
        }
    }

    @Throws(IOException::class)
    private fun scan(id:String){

        val request = Request.Builder().url("https://www.virustotal.com/api/v3/urls/" + id ).get().addHeader("x-apikey", "2e50561b4a38bc74e24303a15f4c4afb404d4a5252470225a4021994806042cb").build()
        val client: OkHttpClient = OkHttpClient()
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
                    Toast(this).showCustomToast("Good to Go!!", this)
                else
                    Toast(this).showCustomToast("Suspicious!!", this)
            }
            response.close()
        }
        catch (e:Exception){
            Log.i ( "Error ", "in scan() :  $e")

        }

    }

    @Throws(IOException::class)
    private fun post(url: String, scan_url: String) {

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
                scan(id)

            }
            response.close()

        }
        catch (e:Exception){
        Log.i ( "Error ", "in post() :  $e")

        }
    }

}  //end of main class






