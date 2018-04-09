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
import java.util.HashMap;
import java.util.List;

class SourceLanguageLoader extends AsyncTask<Void, Void, Void> {

    private WeakReference<MainActivity> activityReference;
    private HashMap<String, String> tempLangCodes = new HashMap<>();
    private ArrayList<String> sourceLanguages = new ArrayList<>();
    private GoogleCredentials credentials;

    SourceLanguageLoader(MainActivity context) {
        activityReference = new WeakReference<>(context);
        credentials = activityReference.get().credentials;
    }

    @Override
    protected Void doInBackground(Void... urls) {
        Translate translate = TranslateOptions.newBuilder().setCredentials(credentials).build().getService();
        Translate.LanguageListOption target = Translate.LanguageListOption.targetLanguage("en");
        List<Language> languages = translate.listSupportedLanguages(target);
        for (Language language : languages) {
            sourceLanguages.add(language.getName());
            tempLangCodes.put(language.getName(), language.getCode());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

        final MainActivity activity = activityReference.get();
        if (activity == null || activity.isFinishing()) return;

        activity.langCodes.putAll(tempLangCodes);

        // адаптер
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, sourceLanguages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        activity.spinnerSourceLanguage = activity.findViewById(R.id.source_language_spinner);
        activity.spinnerSourceLanguage.setAdapter(adapter);

        // устанавливаем обработчик нажатия
        activity.spinnerSourceLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TargetLanguageLoader tll =  new TargetLanguageLoader(activity);
                tll.execute(activity.langCodes.get(activity.spinnerSourceLanguage.getSelectedItem().toString()));
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        activity.spinnerSourceLanguage.setSelection(1);
    }
}
