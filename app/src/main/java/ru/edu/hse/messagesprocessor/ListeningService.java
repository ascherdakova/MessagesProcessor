package ru.edu.hse.messagesprocessor;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.widget.Toast;

public class ListeningService extends Service {

    SMSMonitor smsMonitor;

    public ListeningService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        filter.setPriority(100);
        smsMonitor = new SMSMonitor();
        registerReceiver(smsMonitor, filter, Manifest.permission.BROADCAST_SMS, null);
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId){
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(smsMonitor);
    }
}
