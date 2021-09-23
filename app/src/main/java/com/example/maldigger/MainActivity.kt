package com.example.maldigger

import android.Manifest
import android.app.*
import android.content.Context
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.Call
//import me.rosuh.filepicker.config.FilePickerManager
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.regex.Pattern
import java.util.regex.Pattern.*


class MainActivity : AppCompatActivity() {

    private val context :Context = this
    private val key = "2e50561b4a38bc74e24303a15f4c4afb404d4a5252470225a4021994806042cb"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        askPermissions()
       // extractURLfromSMS()

    }

    //function to get the key
    fun getKey():String{
        return key
    }


    //function to call the API of  VirusTotal
    @RequiresApi(Build.VERSION_CODES.O)
    fun callAPI(view: View?){
        // val intent = Intent(this, Scan::class.java)
        val editText = findViewById<View>(R.id.editText) as EditText
        val url = editText.text.toString()
        //startActivity(intent)

        val p =
            Pattern.compile("(@)?(href=')?(HREF=')?(HREF=\")?(href=\")?(http://)?[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?+%/.\\w]+)?")
        try {
            if (p.matcher(url).matches()) {
                val malicious = CallAPI().getUrlData(context,CallAPI().postURL(context,url))
                if (malicious< 10)
                    Toast(context).showCustomToast("Good To Go!!!",this)
                else
                    Toast(context).showCustomToast("Suspicious!!!",this)

            } else {
                Toast.makeText(context, "Invalid url", Toast.LENGTH_LONG).show()
            }
        }
        catch (e: IOException) {
            Log.d("error", " $e")
        }
    }


    val requestCode = 1
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
       super.onActivityResult(requestCode, resultCode, data)
        var id=""
        var malicious=0
        if(requestCode == requestCode && resultCode == Activity.RESULT_OK )
        {
            if (data==null)
                return
            val uri = data.data?.path
            Log.d("FilePicker1 : ","$uri")
            if (uri != null) {
                id = CallAPI().postFile(context,uri)
                //malicious = CallAPI().getFileData(context,id)
                Log.d("FilePicker2 : ","$uri  $id  $malicious")
            }

            if (malicious>2)
                Toast(context).showCustomToast("Good To Go!!!",this)
            else
                Toast(context).showCustomToast("Suspicious!!!",this)
        }
    }


    //function to choose files from the file manager
    fun filePicker(view:View?){
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "*/*" }
            startActivityForResult(intent,requestCode)

    }


    //function for asking permissions to Read SMS//
    private fun askPermissions(){

        try {

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_MMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_WAP_PUSH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED  ) {

                val MY_PERMISSIONS_REQUEST_SMS = 30
                val activity = this
                ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.READ_SMS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.RECEIVE_MMS,
                        Manifest.permission.RECEIVE_WAP_PUSH,
                        Manifest.permission.READ_EXTERNAL_STORAGE, ),
                    MY_PERMISSIONS_REQUEST_SMS)

            }//end of if block
        }
        catch(e: Exception)
        {
            makeText(context,"error in MainClass", LENGTH_LONG).show()
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

