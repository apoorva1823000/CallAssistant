package com.techninja01.callassistant.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.techninja01.callassistant.R;
import com.techninja01.callassistant.broadcasts.CallReceiver;
import com.techninja01.callassistant.views.MainActivity;

public class CallAccept extends Service {

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        //Broadcast Receiver Starts
        class CallReceiver extends BroadcastReceiver {
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
        //Broadcast Receiver Ends
        //Notification Management Starts

        final String CHANNEL_ID="Foreground Services ID";
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_ID,
                NotificationManager.IMPORTANCE_HIGH
        );
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification = new Notification.Builder(this,CHANNEL_ID)
                .setContentTitle("CallAssistant Running")
                .setContentText("CallAssistant running foreground services of call management")
                .setSmallIcon(R.mipmap.ic_launcher_round);
        startForeground(1001,notification.build());

        //Notification Management Ends

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
