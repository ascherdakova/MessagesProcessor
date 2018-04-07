package ru.edu.hse.messagesprocessor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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

//import com.google.cloud.translate.Language;
//import com.google.cloud.translate.Translate;
//import com.google.cloud.translate.TranslateOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 0;

    ArrayList<String> sourceLanguages = new ArrayList<String>();
    ArrayList<String> targetLanguages = new ArrayList<>();
    CheckBox isEnabled;
    private View mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayout = findViewById(R.id.main_layout);

        //Translate translate = TranslateOptions.newBuilder().build().getService();
        //com.google.cloud.translate.Translate.LanguageListOption target = Translate.LanguageListOption.targetLanguage("en");
        //List<Language> languages = translate.listSupportedLanguages(target);

        //for (Language language : languages) {
            //System.out.printf("Name: %s, Code: %s\n", language.getName(), language.getCode());
        //    sourceLanguages.add(language.getName());
        //}

        sourceLanguages.add("ru");
        sourceLanguages.add("en");
        targetLanguages.add("ru");

        // адаптер
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, sourceLanguages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinnerSourceLanguage = (Spinner) findViewById(R.id.source_language_spinner);
        spinnerSourceLanguage.setAdapter(adapter);
        Spinner spinnerTargetLanguage = (Spinner) findViewById(R.id.target_language_spinner);
        spinnerTargetLanguage.setAdapter(adapter);

        //checkbox to check is user want to see service enabled or not
        isEnabled = (CheckBox) findViewById(R.id.check_box_is_enabled);
        isEnabled.setOnClickListener(checkBoxOnClickListener);
        //TODO: save state

        // выделяем элемент
        spinnerSourceLanguage.setSelection(1);
        spinnerTargetLanguage.setSelection(1);

        // устанавливаем обработчик нажатия
        spinnerSourceLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // показываем позиция нажатого элемента
                Toast.makeText(getBaseContext(), "Position = " + position, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        spinnerTargetLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // показываем позиция нажатого элемента
                Toast.makeText(getBaseContext(), "Position = " + position, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
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
}
