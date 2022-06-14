package com.techninja01.callassistant.broadcasts;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.techninja01.callassistant.views.MainActivity;

public class CallReceiver extends BroadcastReceiver {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        if (MainActivity.telephonyManager.getCallState() == TelephonyManager.CALL_STATE_RINGING) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            MainActivity.telecomManager.acceptRingingCall();
        } else if (MainActivity.telephonyManager.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) {
            Log.d("CallAccepted", "Call is accepted");
            if (!MainActivity.bluetoothAdapter.isEnabled()) {
                MainActivity.bluetoothAdapter.enable();
                MainActivity.bluetoothAdapter.startDiscovery();
            }else{
                Toast.makeText(context, "Bluetooth already enabled", Toast.LENGTH_SHORT).show();
            }
        }else if(MainActivity.telephonyManager.getCallState()==TelephonyManager.CALL_STATE_IDLE){
            Log.d("CallAccepted","Call disconnected");
            MainActivity.bluetoothAdapter.disable();
        }
    }
}
