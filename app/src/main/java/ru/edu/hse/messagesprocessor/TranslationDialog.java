package ru.edu.hse.messagesprocessor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class TranslationDialog extends Activity
{
    public static final String KEY = "translation";
    //static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
    String translated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_popup);

        Bundle extras = getIntent().getExtras();
        translated = (extras == null) ? null : extras.getString(KEY);

        if(translated != null)
            displayResult();

    }

    private void displayResult(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String message =  getResources().getString(R.string.message_string) + ":\n"
                        + translated + "\n\n"
                        + getResources().getString(R.string.voice_request);
        builder.setMessage(message).setCancelable(
                false).setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                }).setNegativeButton(R.string.exit,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
