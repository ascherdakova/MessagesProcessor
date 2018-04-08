package ru.edu.hse.messagesprocessor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Language;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 0;

    ArrayList<String> sourceLanguages = new ArrayList<>();
    Spinner spinnerSourceLanguage;
    ArrayList<String> targetLanguages = new ArrayList<>();
    Spinner spinnerTargetLanguage;
    HashMap <String, String> langCodes = new HashMap<>();
    CheckBox isEnabled;
    private View mLayout;
    GoogleCredentials credentials;
    SourceLanguageLoader ll;

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
        isEnabled = (CheckBox) findViewById(R.id.check_box_is_enabled);
        isEnabled.setOnClickListener(checkBoxOnClickListener);
        //TODO: save state

        ll =  new SourceLanguageLoader();
        ll.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ll.cancel(true);
    }

    private View.OnClickListener checkBoxOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isEnabled.isChecked()) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_SMS)) {
                        Snackbar.make(mLayout, R.string.sms_access_required, Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS}, SMS_PERMISSION_CODE);
                            }
                        }).show();
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS}, SMS_PERMISSION_CODE);
                    }
                } else {
                    //TODO separate thread
                    startService(new Intent(MainActivity.this, ListeningService.class));
                }
            } else {
                stopService(new Intent(MainActivity.this, ListeningService.class));
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case SMS_PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //TODO separate thread
                    startService(new Intent(MainActivity.this, ListeningService.class));
                } else {
                    isEnabled.setChecked(false);
                    Snackbar.make(mLayout, R.string.sms_unavailable, Snackbar.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    class SourceLanguageLoader extends AsyncTask<Void, Void, Void> {

        List<Language> languages;

        @Override
        protected Void doInBackground(Void... urls) {
            Translate translate = TranslateOptions.newBuilder().setCredentials(credentials).build().getService();
            Translate.LanguageListOption target = Translate.LanguageListOption.targetLanguage("en");
            languages = translate.listSupportedLanguages(target);
            for (Language language : languages) {
                sourceLanguages.add(language.getName());
                langCodes.put(language.getName(), language.getCode());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // адаптер
            ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, sourceLanguages);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinnerSourceLanguage = findViewById(R.id.source_language_spinner);
            spinnerSourceLanguage.setAdapter(adapter);

            // устанавливаем обработчик нажатия
            spinnerSourceLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    TargetLanguageLoader tll =  new TargetLanguageLoader();
                    tll.execute(langCodes.get(spinnerSourceLanguage.getSelectedItem().toString()));
                }
                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });

            spinnerSourceLanguage.setSelection(1);
        }
    }

    class TargetLanguageLoader extends AsyncTask<String, Void, Void>{

        List<Language> languages;

        @Override
        protected Void doInBackground(String... urls) {
            Translate translate = TranslateOptions.newBuilder().setCredentials(credentials).build().getService();
            Translate.LanguageListOption target = Translate.LanguageListOption.targetLanguage(urls[0]);
            languages = translate.listSupportedLanguages(target);
            targetLanguages.clear();
            for (Language language : languages) {
                targetLanguages.add(language.getName());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // адаптер
            ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, targetLanguages);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinnerTargetLanguage = findViewById(R.id.target_language_spinner);
            spinnerTargetLanguage.setAdapter(adapter);

            spinnerTargetLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // показываем позиция нажатого элемента
                    Toast.makeText(getBaseContext(), "Position = " + position, Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });

            spinnerTargetLanguage.setSelection(1);
        }

    }
}
