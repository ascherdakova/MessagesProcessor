package ru.edu.hse.messagesprocessor;

import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Language;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

class SourceLanguageLoader extends AsyncTask<String, Void, Void> {

    private WeakReference<MainActivity> activityReference;
    private ArrayList<String> sourceLanguages = new ArrayList<>();
    private GoogleCredentials credentials;

    SourceLanguageLoader(MainActivity context) {
        activityReference = new WeakReference<>(context);
        credentials = activityReference.get().credentials;
    }

    @Override
    protected Void doInBackground(String... urls) {
        Translate translate = TranslateOptions.newBuilder().setCredentials(credentials).build().getService();
        Translate.LanguageListOption target = Translate.LanguageListOption.targetLanguage(urls[0]);
        List<Language> languages = translate.listSupportedLanguages(target);
        sourceLanguages.clear();
        for (Language language : languages) {
            sourceLanguages.add(language.getName());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

        MainActivity activity = activityReference.get();
        if (activity == null || activity.isFinishing()) return;

        // адаптер
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, sourceLanguages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        activity.spinnerSourceLanguage = activity.findViewById(R.id.source_language_spinner);
        activity.spinnerSourceLanguage.setAdapter(adapter);

        activity.spinnerSourceLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // показываем позиция нажатого элемента
                //Toast.makeText(getBaseContext(), "Position = " + position, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        activity.spinnerSourceLanguage.setSelection(1);
    }

}
