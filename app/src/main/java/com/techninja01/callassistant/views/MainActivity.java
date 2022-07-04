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
    MediaPlayer mediaPlayer;
    public static SmsManager smsManager;
    BluetoothDevice[] bluetoothDevices;
    public static final int STATE_LISTENING = 1;
    public static final int STATE_CONNETING = 2;
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_CONNECTION_FAILED = 4;
    public static final int STATE_MESSAGE_RECEIVED = 5;
    public static SendReceive sendReceive = null;
    int REQUEST_ENABLE_BLUETOOTH = 1;
    public static final String APP_NAME = "BluetootChatAplication";
    public static final UUID MY_UUID = UUID.fromString("c4e38b07-a954-4d22-a745-977f8e22c69f");
    public static MediaRecorder mRecorder;
    public static MediaPlayer mPlayer;
    public static String mFileName = null;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
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
        Dexter.withContext(MainActivity.this).withPermissions(Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.MANAGE_EXTERNAL_STORAGE,Manifest.permission.ANSWER_PHONE_CALLS, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.MANAGE_OWN_CALLS, Manifest.permission.BLUETOOTH, Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_SMS,Manifest.permission.SEND_SMS,Manifest.permission.RECEIVE_SMS,Manifest.permission.BROADCAST_SMS).withListener(new MultiplePermissionsListener() {
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


    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case STATE_LISTENING:
                    Log.d("Status","Listening");
                    break;
                case STATE_CONNETING:
                    Log.d("Status","Connecting");
                    break;
                case STATE_CONNECTED:
                    Log.d("Status","Connected");
                    break;
                case STATE_CONNECTION_FAILED:
                    Log.d("Status","Connection Failed");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuffer = (byte[])msg.obj;
                    String tempMsg = new String(readBuffer,0,msg.arg1);
//                    sentMessage.setText(tempMsg);
                    break;
            }
            return true;
        }
    });


    private class ServerClass extends Thread {
        private BluetoothServerSocket serverSocket;

        public ServerClass() {
            if (ActivityCompat.checkSelfPermission(MainActivity.context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void run() {
            BluetoothSocket socket = null;
            while (socket == null) {
                try {
                    Message message = Message.obtain();
                    message.what = STATE_CONNETING;
                    handler.sendMessage(message);
                    socket = serverSocket.accept();
                } catch (Exception e) {
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }
                if (socket != null) {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);
                    sendReceive = new SendReceive(socket);
                    sendReceive.start();
                    break;
                }
            }
        }
    }

    private class ClientClass extends Thread {
        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass(BluetoothDevice device1) {
            device = device1;
            if (ActivityCompat.checkSelfPermission(MainActivity.context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            try {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                if (ActivityCompat.checkSelfPermission(MainActivity.context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                socket.connect();
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            }catch (Exception e){
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }

        }
    }

    public class SendReceive extends Thread{
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        public SendReceive(BluetoothSocket socket){
            bluetoothSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;
            try{
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            }catch (Exception e){
                e.printStackTrace();
            }
            inputStream = tempIn;
            outputStream = tempOut;
        }
        public void run(){
            byte[] buffer = new byte[1024];
            int bytes;
            while(true){
                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED,bytes,-1,buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        public void write(byte[] bytes){
            try{
                outputStream.write(bytes);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
}