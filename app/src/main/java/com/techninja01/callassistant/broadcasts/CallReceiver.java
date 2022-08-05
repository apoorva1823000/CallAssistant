package com.techninja01.callassistant.broadcasts;

import static com.techninja01.callassistant.views.MainActivity.audioManager;
import static com.techninja01.callassistant.views.MainActivity.telecomManager;
import static com.techninja01.callassistant.views.MainActivity.telephonyManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.telephony.DisconnectCause;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.techninja01.callassistant.views.MainActivity;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

public class CallReceiver extends BroadcastReceiver implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {
    String msg_from;
    String msgBody, mainMsgBody, message;
    private TextToSpeech tts = null;
    private String msg = "";
    MediaPlayer mediaPlayer;
    String phone = MainActivity.phoneNumber;

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
//        if (MainActivity.telecomManager.) {
//            number = "10";
//            Toast.makeText(context, "CAll Accepted"+number+"\n", Toast.LENGTH_SHORT).show();
//        }
//        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
        if (MainActivity.telephonyManager.getCallState() == TelephonyManager.CALL_STATE_RINGING) {
            boolean state = audioManager.isStreamMute(AudioManager.FLAG_ALLOW_RINGER_MODES);
            Log.d("McState", String.valueOf(state)+"RINGING");
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            MainActivity.telecomManager.acceptRingingCall();
            String voiceMailNumber = telephonyManager.getVoiceMailNumber();
            Toast.makeText(context, "voice mail "+voiceMailNumber, Toast.LENGTH_SHORT).show();
//            number = bundle.getString(Intent.EXTRA_PHONE_NUMBER);
            //Toast.makeText(context, "Call accepted", Toast.LENGTH_SHORT).show();
        } else if (MainActivity.telephonyManager.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) {
//            boolean state = audioManager.isSpeakerphoneOn();
            boolean state = audioManager.isStreamMute(AudioManager.FLAG_PLAY_SOUND);
            Log.d("McState", String.valueOf(state)+"OFFHOOK");
            Log.d("CallAccepted", "Call is accepted");

            SensorEventListener sensorEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    if(event!=null){
                        float x_accl = event.values[0];
                        float y_accl = event.values[1];
                        float z_accl = event.values[2];
                        if(x_accl>0.5||x_accl<-0.5||y_accl>10||y_accl<-10|z_accl>0.5||z_accl<-0.5){
                            Toast.makeText(context, "Shook", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };

            MainActivity.sensorManager.registerListener(sensorEventListener,MainActivity.shake, SensorManager.SENSOR_DELAY_NORMAL);

            if(audioManager.getMode()!=AudioManager.MODE_NORMAL){
                if(!audioManager.isSpeakerphoneOn()||!audioManager.isWiredHeadsetOn()){
                    Toast.makeText(context, "Earpiece", Toast.LENGTH_SHORT).show();
                }
            }
            //Toast.makeText(context, "Called number is "+number, Toast.LENGTH_SHORT).show();
            final String[] message1 = new String[1];
            MainActivity.telephonyManager.listen(new PhoneStateListener(){
                @Override
                public void onCallStateChanged(int state, String phoneNumber) {
                    super.onCallStateChanged(state, phoneNumber);
                    message1[0] = phoneNumber;
                }
            },PhoneStateListener.LISTEN_CALL_STATE);
            message = "Dear caller,"+message1[0]+"the person you\\'re calling is not available";
            Calendar c = Calendar.getInstance();
            int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
            if(timeOfDay >= 0 && timeOfDay < 12){
//                message = "Good Morning dear caller, the person you\'re calling is busy";
                message = "Suprabhat, Aapne Apoorva Mehta ko call kiya hai aur ve abhi dusra application bna rhe hai";
            }else if(timeOfDay >= 12 && timeOfDay < 16){
                message = "Good Afternoon dear caller, the person you\'re calling is busy";
            }else if(timeOfDay >= 16 && timeOfDay < 21){
                message = "Good Evening dear caller, the person you\'re calling is busy";
            }else if(timeOfDay >= 21 && timeOfDay < 24){
                message = "Good Night dear caller, the person you\'re calling is sleeping";
            }

//            SharedPreferences sharedPreferences = context.getSharedPreferences("Contact",Context.MODE_PRIVATE);
//            String number  = "+91"+sharedPreferences.getString("number","8160081299");

            for(int i=0;i<1;i++){
                try{
                    MainActivity.smsManager.sendTextMessage("+918160081299",null,"0",null,null);
                    Log.d("SMS", "MSG Sent "+phone);
                }catch (Exception e){
                    Log.d("SMS", "Error "+phone);
                }
            }
            tts = new TextToSpeech(MainActivity.context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status != TextToSpeech.ERROR){
                        tts.setLanguage(Locale.UK);
                        tts.setPitch(1);
                        tts.setSpeechRate(0.5f);
                    }
                }
            });
            tts = new TextToSpeech(MainActivity.context,this);
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        while(true) {
                            sleep(1000);
                            audioManager.setMode(AudioManager.MODE_IN_CALL);
                            if (!audioManager.isSpeakerphoneOn())
                                audioManager.setSpeakerphoneOn(true);
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
            if (!MainActivity.bluetoothAdapter.isEnabled()) {
                MainActivity.bluetoothAdapter.enable();
                MainActivity.bluetoothAdapter.startDiscovery();
            }else{
                Toast.makeText(context, "Bluetooth already enabled", Toast.LENGTH_SHORT).show();
            }
        }else if(MainActivity.telephonyManager.getCallState()==TelephonyManager.CALL_STATE_IDLE){
            boolean state = audioManager.isSpeakerphoneOn();
            Log.d("McState", String.valueOf(state)+"IDLE");
            Log.d("CallAccepted","Call disconnected");
            MainActivity.bluetoothAdapter.disable();
            for(int i=0;i<1;i++){
                try{
                    MainActivity.smsManager.sendTextMessage("+918160081299",null,"1",null,null);
                    Log.d("SMS", "MSG Sent");
                }catch (Exception e){
                    Log.d("SMS", "Error");
                }
            }
        }
    }



    public void onInit(int status) {
        tts.speak("Call generated", TextToSpeech.QUEUE_FLUSH, null);
    }

    public void onUtteranceCompleted(String utteranceId) {
        tts.shutdown();
        tts = null;
    }
}
