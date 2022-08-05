package com.techninja01.callassistant.views;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telecom.TelecomManager;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.techninja01.callassistant.R;
import com.techninja01.callassistant.services.CallAccept;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

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
    public static SensorManager sensorManager = null;
    public static Sensor shake = null;
    MediaPlayer mediaPlayer;
    public static SmsManager smsManager;
    Button submit;
    TextInputLayout number;
    public static String phoneNumber;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Permissions Declaration Started
        Dexter.withContext(MainActivity.this).withPermissions(Manifest.permission.PROCESS_OUTGOING_CALLS,Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.MANAGE_EXTERNAL_STORAGE,Manifest.permission.ANSWER_PHONE_CALLS, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.MANAGE_OWN_CALLS, Manifest.permission.BLUETOOTH, Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_SMS,Manifest.permission.SEND_SMS,Manifest.permission.RECEIVE_SMS,Manifest.permission.BROADCAST_SMS).withListener(new MultiplePermissionsListener() {
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
        callSP = findViewById(R.id.ReceiveSwitch);
        callSP.setChecked(false);
        number = findViewById(R.id.addNum);
        submit = findViewById(R.id.submitNum);
        listview = findViewById(R.id.listView);
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        telecomManager = (TelecomManager) getSystemService(TELECOM_SERVICE);
        context = this;
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        shake = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        smsManager = SmsManager.getDefault();
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

        //Number saved
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(number.getEditText().getText().toString().isEmpty()){
                    phoneNumber = "+918160081299";
                    Toast.makeText(MainActivity.this, phoneNumber, Toast.LENGTH_SHORT).show();
                }else{
                    phoneNumber = "+91"+number.getEditText().getText().toString().trim();
                    Toast.makeText(MainActivity.this, phoneNumber, Toast.LENGTH_SHORT).show();
                }
            }
        });
        //Number Saved

        SharedPreferences sharedPreferences2 = getSharedPreferences("Contact",MODE_PRIVATE);
        SharedPreferences.Editor editor2 = getSharedPreferences("Contact",MODE_PRIVATE).edit();
        editor2.putString("number",number.getEditText().getText().toString().trim());
        editor2.apply();

        //Number Saved



        //Service Management Started

        SharedPreferences sharedPreferences = getSharedPreferences("ServiceValue",MODE_PRIVATE);
        callSP.setChecked(sharedPreferences.getBoolean("value",true));
        callSP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(callSP.isChecked()){
                    if(number.getEditText().getText().toString().trim()!=null){
                        SharedPreferences.Editor editor = getSharedPreferences("ServiceValue",MODE_PRIVATE).edit();
                        editor.putBoolean("value",true);
                        editor.apply();
                        callSP.setChecked(true);
                        startForegroundService(new Intent(MainActivity.this,CallAccept.class));
                    }else{
                        number.requestFocus();
                        number.setError("Kindly Fill This First");
                        number.setErrorTextColor(ColorStateList.valueOf(Color.RED));
                    }
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