package ru.edu.hse.messagesprocessor;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.Objects;

public class SMSMonitor extends BroadcastReceiver {

    public static final String NOTIFICATION_CHANNEL_ID="message_processor_channel";
    public static final String NOTIFICATION_CHANNEL_NAME="message_processor_channel_name";
    public static final String KEY_SMS_BODY = "sms_body";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {

            String defaultString = SMSTranslator.defaultString;
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.custom_shared_preferences), Context.MODE_PRIVATE);
            Boolean isEnabled = sharedPref.getBoolean(context.getString(R.string.saved_is_user_enable_service), false);

            String targetLanguageCode = sharedPref.getString(context.getString(R.string.saved_target_language_code), defaultString);
            String sourceLanguageCode = sharedPref.getString(context.getString(R.string.saved_source_language_code), defaultString);
            String sourceText = sharedPref.getString(SMSMonitor.KEY_SMS_BODY, "");

            if (!isEnabled || targetLanguageCode.equals(defaultString) || sourceLanguageCode.equals(defaultString) || sourceText.isEmpty()){
                Log.e(this.getClass().getSimpleName(), "Something wrong with the input. Exiting.");
                return;
            }

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                            .setSmallIcon(R.drawable.notification_icon)
                            .setContentTitle(context.getApplicationContext().getResources().getString(R.string.notification_title))
                            .setContentText(context.getApplicationContext().getResources().getString(R.string.notification_text))
                            .setAutoCancel(true);

            Intent notificationIntent = new Intent(context, SMSTranslator.class);
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, notificationIntent, 0);

            StringBuilder smsBody = new StringBuilder();
            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                smsBody.append(smsMessage.getMessageBody());
            }
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(KEY_SMS_BODY, smsBody.toString());
            editor.apply();

            mBuilder.setContentIntent(pendingIntent);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_LOW;
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, importance);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.enableVibration(true);
                notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(notificationChannel);
                }
            }

            final int notificationId = 1;
            if (notificationManager != null) {
                notificationManager.notify(notificationId, mBuilder.build());
            }
        }
    }
}