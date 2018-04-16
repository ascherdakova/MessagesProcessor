package ru.edu.hse.messagesprocessor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.io.Serializable;
import java.util.Objects;

public class SMSMonitor extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            StringBuilder smsBody = new StringBuilder();
            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                smsBody.append(smsMessage.getMessageBody());
            }
            String body = smsBody.toString();
            Intent translateIntent = new Intent(context, SMSTranslator.class);
            translateIntent.putExtra("sms_body", body);
            context.startService(translateIntent);
        }
    }
}