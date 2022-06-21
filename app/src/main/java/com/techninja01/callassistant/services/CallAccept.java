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
import android.media.MediaPlayer;
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

import java.util.Calendar;

public class CallAccept extends Service {
    MediaPlayer mediaPlayer;

    @Override
    public void onCreate() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        if(timeOfDay >= 0 && timeOfDay < 12){
            mediaPlayer = MediaPlayer.create(MainActivity.context,R.raw.msc1);
        }else if(timeOfDay >= 12 && timeOfDay < 16){
            mediaPlayer = MediaPlayer.create(MainActivity.context,R.raw.msc2);
        }else if(timeOfDay >= 16 && timeOfDay < 21){
            mediaPlayer = MediaPlayer.create(MainActivity.context,R.raw.msc1);
        }else if(timeOfDay >= 21 && timeOfDay < 24){
            mediaPlayer = MediaPlayer.create(MainActivity.context,R.raw.msc2);
        }
        mediaPlayer.setLooping(true);
        super.onCreate();
    }

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
                    mediaPlayer.start();
                }else if(MainActivity.telephonyManager.getCallState()==TelephonyManager.CALL_STATE_IDLE){
                    Log.d("CallAccepted","Call disconnected");
                    MainActivity.bluetoothAdapter.disable();
                    mediaPlayer.pause();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mediaPlayer!=null&& mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }
    }
}
