package ru.edu.hse.messagesprocessor;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 0;

    Spinner spinnerSourceLanguage;
    Spinner spinnerTargetLanguage;
    HashMap <String, String> targetLanguagesCodes = new HashMap<>();
    HashMap <String, String> sourceLanguagesCodes = new HashMap<>();
    CheckBox isEnabled;
    private View mLayout;
    GoogleCredentials credentials;
    TargetLanguageLoader tll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayout = findViewById(R.id.main_layout);

        try {
            credentials = GoogleCredentials.fromStream(getResources().openRawResource(R.raw.credentials));
        } catch (IOException e) {
            //TODO: correct processing
            e.printStackTrace();
        }

        //checkbox to check is user want to see service enabled or not
        isEnabled = findViewById(R.id.check_box_is_enabled);
        isEnabled.setOnClickListener(checkBoxOnClickListener);
        //TODO: save state

        tll = new TargetLanguageLoader(MainActivity.this);
        tll.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tll.cancel(true);
    }

    private View.OnClickListener checkBoxOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isEnabled.isChecked()) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.RECEIVE_SMS)) {
                        Snackbar.make(mLayout, R.string.sms_access_required, Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECEIVE_SMS}, SMS_PERMISSION_CODE);
                            }
                        }).show();
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECEIVE_SMS}, SMS_PERMISSION_CODE);
                    }
                } else {
                    enableTranslation();
                    //startService(new Intent(MainActivity.this, ListeningService.class));
                }
            } else {
                disableTranslation();
                //stopService(new Intent(MainActivity.this, ListeningService.class));
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case SMS_PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableTranslation();
                } else {
                    isEnabled.setChecked(false);
                    Snackbar.make(mLayout, R.string.sms_unavailable, Snackbar.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    public void enableTranslation() {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.custom_shared_preferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.saved_is_user_enable_service), true);
        editor.apply();
    }

    public void disableTranslation() {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.custom_shared_preferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.saved_is_user_enable_service), false);
        editor.apply();
    }

}
