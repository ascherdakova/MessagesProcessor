package ru.edu.hse.messagesprocessor;

import android.content.Context;
import android.content.SharedPreferences;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

class LanguageLoader extends AsyncTask<String, Void, Void> {

    private WeakReference<MainActivity> activityReference;
    private HashMap<String, String> fullLanguageNamesToCodes = new HashMap<>();
    private ArrayList<String> languages = new ArrayList<>();
    private GoogleCredentials credentials;
    private boolean isTarget;
    private boolean isIssue;

    LanguageLoader(MainActivity context, boolean isTarget) {
        activityReference = new WeakReference<>(context);
        credentials = activityReference.get().credentials;
        this.isTarget = isTarget;
    }

    @Override
    protected Void doInBackground(String... urls) {
        isIssue = false;
        Translate translate = TranslateOptions.newBuilder().setCredentials(credentials).build().getService();
        String systemLanguage = Locale.getDefault().getLanguage();
        Translate.LanguageListOption language;
        if (isTarget) {
            language = Translate.LanguageListOption.targetLanguage(systemLanguage);
        } else {
            language = Translate.LanguageListOption.targetLanguage(urls[0]);
        }
        try {
            List<Language> languages = translate.listSupportedLanguages(language);
            for (Language languageIterator : languages) {
                this.languages.add(languageIterator.getName());
                fullLanguageNamesToCodes.put(languageIterator.getName(), languageIterator.getCode());
            }
        } catch (Exception e) {
            isIssue = true;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

        final MainActivity activity = activityReference.get();
        if (activity == null || activity.isFinishing()) return;

        if (isIssue) {
            Toast.makeText(activity, activity.getString(R.string.network_issue_message), Toast.LENGTH_LONG).show();
            activity.progressBar.setVisibility(View.GONE);
            activity.progressText.setVisibility(View.GONE);
            return;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        SharedPreferences sharedPref = activity.getSharedPreferences(activity.getString(R.string.custom_shared_preferences), Context.MODE_PRIVATE);

        if (isTarget) {
            activity.targetLanguagesCodes.putAll(fullLanguageNamesToCodes);
            activity.spinnerTargetLanguage = activity.findViewById(R.id.target_language_spinner);
            activity.spinnerTargetLanguage.setAdapter(adapter);

            activity.spinnerTargetLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    LanguageLoader sll = new LanguageLoader(activity, false);
                    String langName = activity.spinnerTargetLanguage.getSelectedItem().toString();
                    //Let's store languages settings in shared preferences
                    SharedPreferences sharedPref = activity.getSharedPreferences(activity.getString(R.string.custom_shared_preferences), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    String langCode = activity.targetLanguagesCodes.get(langName);
                    editor.putString(activity.getString(R.string.saved_target_language_code), langCode);
                    editor.putString(activity.getString(R.string.saved_target_language_name), langName);
                    editor.apply();
                    activity.progressBar.setVisibility(View.VISIBLE);
                    activity.progressText.setVisibility(View.VISIBLE);
                    activity.isLoading = true;
                    sll.execute(langCode);
                }
                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });
            String locale = sharedPref.getString(activity.getString(R.string.saved_target_language_name), Locale.getDefault().getDisplayLanguage());
            activity.spinnerTargetLanguage.setSelection(adapter.getPosition(locale));
        } else {
            activity.sourceLanguagesCodes.putAll(fullLanguageNamesToCodes);
            activity.spinnerSourceLanguage = activity.findViewById(R.id.source_language_spinner);
            activity.spinnerSourceLanguage.setAdapter(adapter);

            activity.spinnerSourceLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String langName = activity.spinnerSourceLanguage.getSelectedItem().toString();
                    SharedPreferences sharedPref = activity.getSharedPreferences(activity.getString(R.string.custom_shared_preferences), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    String langCode = activity.sourceLanguagesCodes.get(langName);
                    editor.putString(activity.getString(R.string.saved_source_language_code), langCode);
                    editor.putString(activity.getString(R.string.saved_source_language_name), langName);
                    editor.apply();
                }
                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });

            if (sharedPref.contains(activity.getString(R.string.saved_source_language_name))) {
                String locale = sharedPref.getString(activity.getString(R.string.saved_source_language_name), Locale.getDefault().getDisplayLanguage());
                activity.spinnerSourceLanguage.setSelection(adapter.getPosition(locale));
            }

            activity.progressBar.setVisibility(View.GONE);
            activity.progressText.setVisibility(View.GONE);
            activity.isLoading = false;
        }
    }
}
