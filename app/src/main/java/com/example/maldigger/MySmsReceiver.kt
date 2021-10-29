package com.example.maldigger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.util.regex.Pattern

class MySmsReceiver : BroadcastReceiver() {
    private val SMS = "android.provider.Telephony.SMS_RECEIVED"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        Log.d("BroadcastReceiver", "onReceive")

        if (intent.action.equals(SMS)) {

            //makeText(context,"SMS Received ", LENGTH_LONG).show()
            // Will do stuff with message here

            val msgBody: StringBuilder = StringBuilder()
            var number = ""
            val bundle: Bundle? = intent.extras
            val messages: Array<SmsMessage?>

            if (bundle != null) {
                val msgObjects: Array<*>? = bundle.get("pdus") as Array<*>?
                messages = arrayOfNulls(msgObjects!!.size)
                for (i in messages.indices) {
                    messages[i] = SmsMessage.createFromPdu(msgObjects[i] as ByteArray?)
                    msgBody.append(messages[i]!!.messageBody)
                    number = messages[i]!!.originatingAddress.toString()
                }
            }

            //Now Splitting the above msgBody to extract URL from it

            val url = msgBody.split(" ", "-").toTypedArray()

            //creating regex pattern to validate url..

            val p =
                Pattern.compile("(@)?(href=')?(HREF=')?(HREF=\")?(href=\")?(http://)?[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?+%/.\\w]+)?")

            for (i in url.indices) {
                if (p.matcher(url[i]).matches()) {

                    //creating and sending notifications
                    val malicious = CallAPI().getUrlData(CallAPI().postURL(url[i]))
                    if (malicious < 10) {
                        Notification().createNotification(context)
                        Notification().sendNotification(context, url[i])
                    }
                    Toast.makeText(context, url[i], Toast.LENGTH_LONG).show()
                    Log.d("BroadcastReceiver", url[i])
                    break

                }//end of if block
            }//end of for loop

        }//end of if block

    }//end of onReceive()

}// end of class

