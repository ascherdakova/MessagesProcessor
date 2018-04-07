package ru.edu.hse.messagesprocessor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

//import com.google.cloud.translate.Language;
//import com.google.cloud.translate.Translate;
//import com.google.cloud.translate.TranslateOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> sourceLanguages = new ArrayList<String>();
    ArrayList<String> targetLanguages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
}
