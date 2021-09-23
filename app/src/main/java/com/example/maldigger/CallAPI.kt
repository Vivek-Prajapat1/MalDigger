package com.example.maldigger

import android.content.Context
import android.os.Build
import android.os.StrictMode
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.regex.Pattern

class CallAPI {

    val urlForPostUrl = "https://www.virustotal.com/api/v3/urls"
    val urlToGetUrlData = "https://www.virustotal.com/api/v3/urls/"
    val urlForPostFile = "https://www.virustotal.com/api/v3/files"
    val urlToGetFileData = "https://www.virustotal.com/api/v3/files"

    //function for sending the extracted URL to the server
    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(IOException::class)
    fun postURL(context: Context, urlToBeScanned: String):String {

        val gfgPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(gfgPolicy)
        val body = MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("url", urlToBeScanned).build()
        val request = Request.Builder().url(urlForPostUrl).post(body).addHeader("x-apikey", MainActivity().getKey()).build()
        val client = OkHttpClient()

        val call = client.newCall(request)
        var id = ""
        val response = call.execute()
        try {

            if (response.isSuccessful) {
                val body = response.body!!.string()
                val json = JSONObject(body)
                val data = json.getJSONObject("data")
                id = data.getString("id")
                id = id.split("-").toTypedArray()[1]
                Log.i("URL Submitted", "ID generated:  $id")
            }
            response.close()

        }
        catch (e:Exception){
            Log.i ( "Error ", "in post() :  $e")
            response.close()
        }

        return id
    }


    //function fot getting the data from the server
    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(IOException::class)
    fun getUrlData(context: Context, id:String): Int {

        var malicious = 0
        try {
            val request = Request.Builder().url(urlToGetUrlData + id ).get().addHeader("x-apikey", MainActivity().getKey()).build()
            val client = OkHttpClient()
            val call = client.newCall(request)
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


    //function to send hash of file to virusTotal
    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(IOException::class)
    fun postFile(context: Context, uri :String): String {

        var id = ""
        try {
            val gfgPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(gfgPolicy)
            val body = MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("url", uri).build()
            val request = Request.Builder().url(urlForPostFile).post(body).addHeader("x-apikey", MainActivity().getKey()).build()
            val client = OkHttpClient()

            val call = client.newCall(request)

            val response = call.execute()
            if (response.isSuccessful) {
                val body = response.body!!.string()
                val json = JSONObject(body)
                val data = json.getJSONObject("data")
                id = data.getString("id")
                id = id.split("-").toTypedArray()[1]
                Log.d("File Submitted", "ID generated:  $id")
            }
            else{Log.d("nothing ","happened")}
            response.close()

        }
        catch (e:Exception){
            Log.i ( "Error ", "in postFile() :  $e")
        }

        return id

    }


    //function to get
    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(IOException::class)
    fun getFileData(context: Context, id: String): Int {

        val request = Request.Builder().url(urlToGetFileData + id ).get().addHeader("x-apikey", MainActivity().getKey()).build()
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
                Log.d("Successful Scan ", " harmless count: $harmless")

            }
            response.close()
        }
        catch (e:Exception){
            Log.d ( "Error ", "in getFileData() :  $e")
        }
        return malicious
    }


}//end of class CallAPI