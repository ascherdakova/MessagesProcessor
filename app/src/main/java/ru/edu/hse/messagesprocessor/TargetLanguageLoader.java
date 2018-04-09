package ru.edu.hse.messagesprocessor;

import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Language;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

class TargetLanguageLoader extends AsyncTask<String, Void, Void> {

    private WeakReference<MainActivity> activityReference;
    private ArrayList<String> targetLanguages = new ArrayList<>();
    private Translate translate;
    private GoogleCredentials credentials;

    TargetLanguageLoader(MainActivity context) {
        activityReference = new WeakReference<>(context);
        credentials = activityReference.get().credentials;
    }

    @Override
    protected Void doInBackground(String... urls) {
        translate = TranslateOptions.newBuilder().setCredentials(credentials).build().getService();
        Translate.LanguageListOption target = Translate.LanguageListOption.targetLanguage(urls[0]);
        List<Language> languages = translate.listSupportedLanguages(target);
        targetLanguages.clear();
        for (Language language : languages) {
            targetLanguages.add(language.getName());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

        MainActivity activity = activityReference.get();
        if (activity == null || activity.isFinishing()) return;

        // адаптер
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, targetLanguages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        activity.spinnerTargetLanguage = activity.findViewById(R.id.target_language_spinner);
        activity.spinnerTargetLanguage.setAdapter(adapter);

        activity.spinnerTargetLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // показываем позиция нажатого элемента
                //Toast.makeText(getBaseContext(), "Position = " + position, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        activity.spinnerTargetLanguage.setSelection(1);
    }

}
