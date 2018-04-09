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
import java.util.Locale;

class TargetLanguageLoader extends AsyncTask<Void, Void, Void> {

    private WeakReference<MainActivity> activityReference;
    private HashMap<String, String> tempLangCodes = new HashMap<>();
    private ArrayList<String> targetLanguages = new ArrayList<>();
    private GoogleCredentials credentials;
    private int systemLanguagePosition;

    TargetLanguageLoader(MainActivity context) {
        activityReference = new WeakReference<>(context);
        credentials = activityReference.get().credentials;
    }

    @Override
    protected Void doInBackground(Void... urls) {
        Translate translate = TranslateOptions.newBuilder().setCredentials(credentials).build().getService();
        String systemLanguage = Locale.getDefault().getLanguage();
        Translate.LanguageListOption target = Translate.LanguageListOption.targetLanguage(systemLanguage);
        List<Language> languages = translate.listSupportedLanguages(target);
        int positionChecker = 0;
        for (Language language : languages) {
            targetLanguages.add(language.getName());
            tempLangCodes.put(language.getName(), language.getCode());
            if (language.getCode().equals(systemLanguage)){
                systemLanguagePosition = positionChecker;
            }
            positionChecker++;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

        final MainActivity activity = activityReference.get();
        if (activity == null || activity.isFinishing()) return;

        activity.langCodes.putAll(tempLangCodes);

        // адаптер
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, targetLanguages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        activity.spinnerTargetLanguage = activity.findViewById(R.id.target_language_spinner);
        activity.spinnerTargetLanguage.setAdapter(adapter);

        // устанавливаем обработчик нажатия
        activity.spinnerTargetLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SourceLanguageLoader tll =  new SourceLanguageLoader(activity);
                tll.execute(activity.langCodes.get(activity.spinnerTargetLanguage.getSelectedItem().toString()));
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        activity.spinnerTargetLanguage.setSelection(systemLanguagePosition);
    }
}
