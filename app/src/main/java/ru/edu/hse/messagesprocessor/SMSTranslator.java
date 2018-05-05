package ru.edu.hse.messagesprocessor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;

public class SMSTranslator extends Service {

    public SMSTranslator() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId){
        SharedPreferences sharedPref = this.getApplication().getSharedPreferences(this.getApplication().getString(R.string.custom_shared_preferences), Context.MODE_PRIVATE);
        Boolean isEnabled = sharedPref.getBoolean(this.getApplication().getString(R.string.saved_is_user_enable_service), false);
        String defaultString = "default";
        String targetLanguageCode = sharedPref.getString(this.getApplication().getString(R.string.saved_target_language_code), defaultString);
        String sourceLanguageCode = sharedPref.getString(this.getApplication().getString(R.string.saved_source_language_code), defaultString);
        String sourceText = intent.getStringExtra("sms_body");
        if (!isEnabled || targetLanguageCode.equals(defaultString) || sourceLanguageCode.equals(defaultString) || sourceText.isEmpty()){
            return START_STICKY;
        }
        try {
            NetworkTask nt =  new NetworkTask(this, sourceLanguageCode, targetLanguageCode, sourceText, GoogleCredentials.fromStream(this.getApplication().getResources().openRawResource(R.raw.credentials)));
            nt.execute();
        } catch (IOException e) {
            e.printStackTrace();
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
        SharedPreferences sharedPref = this.getApplication().getSharedPreferences(this.getApplication().getString(R.string.custom_shared_preferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.saved_is_user_enable_service), false);
        editor.apply();
    }
}
