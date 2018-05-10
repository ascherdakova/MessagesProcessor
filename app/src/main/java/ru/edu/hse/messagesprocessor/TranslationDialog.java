package ru.edu.hse.messagesprocessor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class TranslationDialog extends Activity {

    public static final String TRANSLATION_KEY = "translation_key";
    public static final String KEY_IS_VOICE_ENABLED = "is_voice_enabled";

    String translated;

    boolean bound = false;
    private ServiceConnection serviceConnection;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_popup);

        Bundle extras = getIntent().getExtras();
        translated = (extras == null) ? null : extras.getString(TRANSLATION_KEY);

        startService(new Intent(this, Speaker.class));

        if(translated != null)
            displayResult();

        serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                bound = true;
            }

            public void onServiceDisconnected(ComponentName name) {
                bound = false;
            }
        };
    }

    private void displayResult(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String message =  getResources().getString(R.string.message_string) + ":\n"
                        + translated + "\n\n"
                        + getResources().getString(R.string.voice_request);


        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        finish();
                        //TODO: close the app
                    }
                });
        final AlertDialog alert = builder.create();

        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

                final Button button = alert.getButton(AlertDialog.BUTTON_POSITIVE);;
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i(this.getClass().getSimpleName(), "Positive button clicked");

                        checkTTSVoiceData();

                        String customSharedPrefs = getResources().getString(R.string.custom_shared_preferences);
                        SharedPreferences sharedPref = getSharedPreferences(customSharedPrefs, Context.MODE_PRIVATE);
                        Boolean isVoiceEnabled = sharedPref.getBoolean(TranslationDialog.KEY_IS_VOICE_ENABLED, false);

                        Log.i(this.getClass().getSimpleName(), "isVoiceEnabled " + isVoiceEnabled);
                        if(isVoiceEnabled) {
                            Log.i(this.getClass().getSimpleName(), "Enabled. Binding service.");
                            intent = new Intent(TranslationDialog.this, Speaker.class);
                            intent.putExtra(Speaker.TEXT_KEY, translated);
                            bindService(intent, serviceConnection, BIND_AUTO_CREATE);
                            unbindService(serviceConnection);
                        }


                        // remove next two lines if you need the dialog to stay.
                        // Note: it won't be speaking more than once, idk why yet
                        //alert.dismiss();
                        //finish();
                    }
                });
            }
        });
        alert.show();
    }

    private void checkTTSVoiceData(){
        // Check if we have TTS voice data
        Intent ttsIntent = new Intent();
        ttsIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(ttsIntent, Speaker.CHECK_TTS_DATA);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Speaker.CHECK_TTS_DATA) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // Data exists, so we instantiate the TTS engine
                enableVoicing();

            } else {
                // Data is missing, so we start the TTS installation process
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                Toast.makeText(this, "Installation required", Toast.LENGTH_LONG).show();
                startActivity(installIntent);
            }
        }
    }

    private void enableVoicing(){
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.custom_shared_preferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(KEY_IS_VOICE_ENABLED, true);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        this.stopService(new Intent(this, Speaker.class));
        super.onDestroy();
    }
}
