package com.example.maldigger

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Log.d
import android.util.Log.e
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkUrl(url: String) {

        //val find = Base64.getEncoder().withoutPadding().encodeToString(url.toByteArray())
        val queue = Volley.newRequestQueue(this)
        //val key = "2e50561b4a38bc74e24303a15f4c4afb404d4a5252470225a4021994806042cb"
        //val urltofind = "https://www.virustotal.com/vtapi/v2/url/scan"
        val urltofind = "https://www.virustotal.com/api/v3/urls"//+find+"/analyse"
       // Log.d("nothing",find)

        //val params = HashMap<String,String>()
        //params["url"] = "google.com"

        val jsonObject = object: JSONObject(){}
        jsonObject.put("url", "google.com")
        val str:String = jsonObject.toString()

        // Request a JsonObject response from the provided URL.

        val sr = object: JsonObjectRequest(
            Method.POST, urltofind, null,
            Response.Listener { response ->
                d("success request", "successful $response")
                /* val data = response.getString("data")
                val json = JSONObject(data)
                val attributes = json.getJSONObject("attributes")
                val last_analysis_stats = attributes.getJSONObject("last_analysis_stats")
                val harmless = last_analysis_stats.getInt("harmless")
                val malicious = last_analysis_stats.getInt("malicious")
                val suspicious = last_analysis_stats.getInt("suspicious")
                val undetected = last_analysis_stats.getInt("undetected")

                //d("success request", "this is attributes " + harmless.toString())
                //Toast.makeText(this, "you r safe", Toast.LENGTH_LONG).show()

                if (malicious<5)
                    Toast(this).showCustomToast("Good to Go!!", this)
                else
                    Toast(this).showCustomToast("Good to Go!!", this)*/
            },
            Response.ErrorListener { error ->

                e("error", "error occurred in error listener :  $error")
                //var digit = error.networkResponse.statusCode

            },
        )

        {   /*
            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                e("GetBody", "this is getbody()")
                return str.toByteArray(charset("utf-8"))
            }
            */
            @Throws(AuthFailureError::class)
             override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["url"] = "google.com"
                e("getParams", "we are here : $params")
                return params
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                headers["x-apikey"]="2e50561b4a38bc74e24303a15f4c4afb404d4a5252470225a4021994806042cb"
                e("Header", "this is header :  $headers")
                return headers
            }
        }

        /* Add the request to the RequestQueue. */
        queue.add(sr)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun status(view: View?) {
        //val intent = Intent(this, Scan::class.java)
        val editText = findViewById<View>(R.id.editText) as EditText
        val url = editText.text.toString()
        //startActivity(intent)
        try {
            checkUrl(url)
        } catch (e: IOException) {
            Log.d("error", "error $e")
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
}




