package com.techninja01.callassistant.broadcasts;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.techninja01.callassistant.views.MainActivity;

public class OutgoingCall extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        Bundle bundle = intent.getExtras();
        String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        Toast.makeText(context, "Outgoing call catched: " + phoneNumber, Toast.LENGTH_LONG).show();
        if (MainActivity.telephonyManager.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
//            number = bundle.getString(Intent.EXTRA_PHONE_NUMBER);
            Toast.makeText(context, "Call accepted", Toast.LENGTH_SHORT).show();
        }
    }
}
