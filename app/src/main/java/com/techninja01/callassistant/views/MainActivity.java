package com.techninja01.callassistant.views;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;


import android.Manifest;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.techninja01.callassistant.R;
import com.techninja01.callassistant.services.CallAccept;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView callP, responseP;
    SwitchCompat callSP, responseSP;
    public static ListView listview;
    public static TelephonyManager telephonyManager;
    public static TelecomManager telecomManager;
    public static Context context;
    public static AudioManager audioManager = null;
    public static BluetoothAdapter bluetoothAdapter = null;
    public static BluetoothManager bluetoothManager = null;
    MediaPlayer mediaPlayer;
//    public static BluetoothDevice[] bluetoothDevices;
//    public static final int STATE_LISTENING = 1;
//    public static final int STATE_CONNECTING = 2;
//    public static final int STATE_CONNECTED = 3;
//    public static final int STATE_CONNECTION_FAILED = 4;
//    public static final int STATE_MESSAGE_RECEIVED = 5;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Permissions Declaration Started
        Dexter.withContext(MainActivity.this).withPermissions(Manifest.permission.ANSWER_PHONE_CALLS, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.MANAGE_OWN_CALLS, Manifest.permission.BLUETOOTH, Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.RECORD_AUDIO).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                if (multiplePermissionsReport.areAllPermissionsGranted()) {
                    Toast.makeText(MainActivity.this, "All permissions are granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Not all permissions are granted", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
        MultiplePermissionsListener dialogMultiplePermissionsListener =
                DialogOnAnyDeniedMultiplePermissionsListener.Builder
                        .withContext(getApplicationContext())
                        .withTitle("Phone and bluetooth permissions required")
                        .withMessage("This application requires both phone and bluetooth permissions")
                        .withButtonText(android.R.string.ok)
                        .build();
//        Permissions Declaration Finished

//        Initializing Variables
        callP = findViewById(R.id.ReceiveText);
        responseP = findViewById(R.id.VoiceCallText);
        callSP = findViewById(R.id.ReceiveSwitch);
        responseSP = findViewById(R.id.VoiceCallSwitch);
        listview = findViewById(R.id.listView);
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        telecomManager = (TelecomManager) getSystemService(TELECOM_SERVICE);
        context = this;
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Your device doesn't support bluetooth", Toast.LENGTH_SHORT).show();
        }
//         Variable Initialization Finished

        //Service Started
        if(!foregroundServicesRunning()){
            Intent intent = new Intent(this, CallAccept.class);
            startForegroundService(intent);
        }
        //Service Terminated

        //Service Management Started

        SharedPreferences sharedPreferences = getSharedPreferences("ServiceValue",MODE_PRIVATE);
        callSP.setChecked(sharedPreferences.getBoolean("value",true));
        callSP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(callSP.isChecked()){
                    SharedPreferences.Editor editor = getSharedPreferences("ServiceValue",MODE_PRIVATE).edit();
                    editor.putBoolean("value",true);
                    editor.apply();
                    callSP.setChecked(true);
                    startForegroundService(new Intent(MainActivity.this,CallAccept.class));
                }else{
                    SharedPreferences.Editor editor = getSharedPreferences("ServiceValue",MODE_PRIVATE).edit();
                    editor.putBoolean("value",false);
                    editor.apply();
                    callSP.setChecked(false);
                    stopService(new Intent(MainActivity.this,CallAccept.class));
                }
            }
        });
        //Service Management Ended

        //Bluetooth Management Starts


        //Bluetooth Management Ends
//        Calendar c = Calendar.getInstance();
//        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
//        if(timeOfDay >= 0 && timeOfDay < 12){
//            mediaPlayer = MediaPlayer.create(MainActivity.context,R.raw.msc1);
//        }else if(timeOfDay >= 12 && timeOfDay < 16){
//            mediaPlayer = MediaPlayer.create(MainActivity.context,R.raw.msc2);
//        }else if(timeOfDay >= 16 && timeOfDay < 21){
//            mediaPlayer = MediaPlayer.create(MainActivity.context,R.raw.msc1);
//        }else if(timeOfDay >= 21 && timeOfDay < 24){
//            mediaPlayer = MediaPlayer.create(MainActivity.context,R.raw.msc2);
//        }
//
//        responseSP.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (responseSP.isChecked()){
//                    mediaPlayer.start();
//                }else{
//                    mediaPlayer.pause();
//                }
//            }
//        });



    }

    public boolean foregroundServicesRunning(){
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service:activityManager.getRunningServices(Integer.MAX_VALUE)){
            if(CallAccept.class.getName().equals(service.service.getClassName())){
                return true;
            }
        }
        return false;
    }

}