package ru.edu.hse.messagesprocessor;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.widget.Toast;
import java.util.Objects;

public class SMSMonitor extends BroadcastReceiver {

    public static final String NOTIFICATION_CHANNEL_ID="messages_processor_channel";
    public static final String KEY_SMS_BODY = "sms_body";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                            .setSmallIcon(R.drawable.notification_icon)
                            .setContentTitle(context.getApplicationContext().getResources().getString(R.string.notification_title))
                            .setContentText(context.getApplicationContext().getResources().getString(R.string.notification_text));

            Intent notificationIntent = new Intent(context, SMSTranslator.class);
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, notificationIntent, 0);

            StringBuilder smsBody = new StringBuilder();
            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                smsBody.append(smsMessage.getMessageBody());
            }
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.custom_shared_preferences), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(KEY_SMS_BODY, smsBody.toString());
            editor.commit();

            mBuilder.setContentIntent(pendingIntent);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            final int notificationId = 1;
            notificationManager.notify(notificationId, mBuilder.build());
        }
    }
}