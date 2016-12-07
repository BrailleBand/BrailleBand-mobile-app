package com.example.hareenudayanath.appbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.telephony.SmsMessage;
import android.widget.Toast;

/**
 * Created by Hareen Udayanath on 9/11/2016.
 */
public class SMS_Handler extends WakefulBroadcastReceiver {

    // Get the object of SmsManager
    public static final String SMS_BUNDLE = "pdus";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();
        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            String smsMessageStr = "";

            for (int i = 0; i < sms.length; ++i) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);

                String smsBody = smsMessage.getMessageBody().toString();

                smsMessageStr  = smsBody ;
            }


            //Toast.makeText(context, smsMessageStr, Toast.LENGTH_SHORT).show();

            //this will update the UI with message
            CommunicationActivity inst = CommunicationActivity.instance();
            inst.onSmsReceived(smsMessageStr);

        }
    }


}
