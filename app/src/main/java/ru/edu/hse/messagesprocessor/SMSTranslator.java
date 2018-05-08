package ru.edu.hse.messagesprocessor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translation;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class SMSTranslator extends Service {
    public static final String defaultString = "default";

    public SMSTranslator() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId){
        if (intent == null){
            return START_STICKY;
        }
        SharedPreferences sharedPref = this.getApplication().getSharedPreferences(this.getApplication().getString(R.string.custom_shared_preferences), Context.MODE_PRIVATE);
        Boolean isEnabled = sharedPref.getBoolean(this.getApplication().getString(R.string.saved_is_user_enable_service), false);

        String targetLanguageCode = sharedPref.getString(this.getApplication().getString(R.string.saved_target_language_code), defaultString);
        String sourceLanguageCode = sharedPref.getString(this.getApplication().getString(R.string.saved_source_language_code), defaultString);
        String sourceText = intent.getStringExtra("sms_body");
        if (!isEnabled || targetLanguageCode.equals(defaultString) || sourceLanguageCode.equals(defaultString) || sourceText.isEmpty()){
            return START_STICKY;
        }

        try {
            NetworkTask networkTask =  new NetworkTask(this, sourceLanguageCode, targetLanguageCode, sourceText, GoogleCredentials.fromStream(this.getApplication().getResources().openRawResource(R.raw.credentials)));
            Translation result = networkTask.execute().get();

            //TODO: start the activity through push notification
            Intent dialogIntent = new Intent(this, TranslationDialog.class).putExtra(TranslationDialog.KEY, result.getTranslatedText());
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(dialogIntent);

        } catch (IOException | InterruptedException | ExecutionException e) {
            Toast.makeText(this, this.getApplication().getString(R.string.network_issue_message_service), Toast.LENGTH_LONG).show();
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
    }
}
