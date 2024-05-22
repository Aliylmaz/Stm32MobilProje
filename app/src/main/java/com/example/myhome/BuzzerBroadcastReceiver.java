package com.example.myhome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BuzzerBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "BuzzerBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && "com.example.myhome.BUZZER_ACTIVE".equals(intent.getAction())) {
            Log.d(TAG, "Buzzer active received.");
            Intent alarmIntent = new Intent(context, AlarmActivity.class);
            alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(alarmIntent);
        }
    }
}
