package com.techninja01.callassistant.services;

import static com.techninja01.callassistant.views.MainActivity.audioManager;

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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.techninja01.callassistant.R;
import com.techninja01.callassistant.broadcasts.CallReceiver;
import com.techninja01.callassistant.views.MainActivity;
import com.techninja01.callassistant.views.SpeakTheMessage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Locale;

public class CallAccept extends Service implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {
    MediaPlayer mediaPlayer;
    private TextToSpeech tts = null;
    String message;
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
            String msg_from;
            String msgBody,mainMsgBody;

            @SuppressLint("UnsafeProtectedBroadcastReceiver")
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onReceive(Context context, Intent intent) {



//                if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
//                    Bundle bundle = intent.getExtras();
//                    SmsMessage[] smsMessages;
//                    if(bundle!=null){
//                        try{
//                            Object[] pdus = (Object[]) bundle.get("pdus");
//                            smsMessages = new SmsMessage[pdus.length];
//                            for(int i=0;i<smsMessages.length;i++){
//                                smsMessages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
//                                msg_from = smsMessages[i].getOriginatingAddress();
//                                msgBody = smsMessages[i].getMessageBody();
//                                mainMsgBody = smsMessages[0].getMessageBody();
//                                Toast.makeText(context, "From: "+msg_from+"\tContent: "+mainMsgBody, Toast.LENGTH_SHORT).show();
//                                Intent speechIntent = new Intent();
//                                speechIntent.setClass(context, SpeakTheMessage.class);
//                                speechIntent.putExtra("MESSAGE", msgBody);
//                                speechIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |  Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//                                context.startActivity(speechIntent);
//                            }
//                        }catch (Exception e){
//                            e.printStackTrace();
//                        }
//                    }
//                }




                if (MainActivity.telephonyManager.getCallState() == TelephonyManager.CALL_STATE_RINGING) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    MainActivity.telecomManager.acceptRingingCall();
                } else if (MainActivity.telephonyManager.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) {

                    message = "Dear caller, the person you\'re calling is not available";
                    Calendar c = Calendar.getInstance();
                    int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
                    if(timeOfDay >= 0 && timeOfDay < 12){
                        message = "Good Morning dear caller, the person you\'re calling is busy";
                    }else if(timeOfDay >= 12 && timeOfDay < 16){
                        message = "Good Afternoon dear caller, the person you\'re calling is busy";
                    }else if(timeOfDay >= 16 && timeOfDay < 21){
                        message = "Good Evening dear caller, the person you\'re calling is busy";
                    }else if(timeOfDay >= 21 && timeOfDay < 24){
                        message = "Good Night dear caller, the person you\'re calling is sleeping";
                    }
//                        MainActivity.sendReceive.write(message.getBytes(StandardCharsets.UTF_8));

                    try{
                        MainActivity.smsManager.sendTextMessage("+918160081299",null,message,null,null);
                        Log.d("SMS", "MSG Sent");
                    }catch (Exception e){
                        Log.d("SMS", "Error");
                    }

//                    Intent speechIntent = new Intent();
//                    speechIntent.setClass(context, SpeakTheMessage.class);
//                    speechIntent.putExtra("MESSAGE", message);
//                    speechIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |  Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//                    context.startActivity(speechIntent);
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
//                    tts = new TextToSpeech(MainActivity.context, (TextToSpeech.OnInitListener) this);
                    tts.speak("Call Generated", TextToSpeech.QUEUE_FLUSH, null);
                    Log.d("CallAccepted", "Call is accepted");
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



//                    MainActivity.mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
//                    MainActivity.mFileName += "/AudioRecording.3gp";
//                    MainActivity.mRecorder = new MediaRecorder();
//                    MainActivity.mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//                    MainActivity.mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//                    MainActivity.mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//                    MainActivity.mRecorder.setOutputFile(MainActivity.mFileName);
//                    try {
//                        MainActivity.mRecorder.prepare();
//                    } catch (IOException e) {
//                        Log.e("TAG", "prepare() failed");
//                    }
//                    MainActivity.mRecorder.start();






//                    Toast.makeText(context, "Call received", Toast.LENGTH_SHORT).show();
                    if (!MainActivity.bluetoothAdapter.isEnabled()) {
                        MainActivity.bluetoothAdapter.enable();
                        MainActivity.bluetoothAdapter.startDiscovery();

//                        AudioManager audioManager = (AudioManager) MainActivity.context.getSystemService(Context.AUDIO_SERVICE);
//                        audioManager.setSpeakerphoneOn(true);
//                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);


                    }else{
                        Toast.makeText(context, "Bluetooth already enabled", Toast.LENGTH_SHORT).show();
//                        String message = "Dear caller, the person you\'re calling is not available";
//                        Calendar c = Calendar.getInstance();
//                        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
//                        if(timeOfDay >= 0 && timeOfDay < 12){
//                            message = "Good Morning dear caller, the person you\'re calling is busy";
//                        }else if(timeOfDay >= 12 && timeOfDay < 16){
//                            message = "Good Afternoon dear caller, the person you\'re calling is busy";
//                        }else if(timeOfDay >= 16 && timeOfDay < 21){
//                            message = "Good Evening dear caller, the person you\'re calling is busy";
//                        }else if(timeOfDay >= 21 && timeOfDay < 24){
//                            message = "Good Night dear caller, the person you\'re calling is sleeping";
//                        }
////                        MainActivity.sendReceive.write(message.getBytes(StandardCharsets.UTF_8));
//
//                        try{
//                            MainActivity.smsManager.sendTextMessage("+918160081299",null,message,null,null);
//                            Log.d("SMS", "MSG Sent");
//                        }catch (Exception e){
//                            Log.d("SMS", "Error");
//                        }
                    }
//                    mediaPlayer.start();
                }else if(MainActivity.telephonyManager.getCallState()==TelephonyManager.CALL_STATE_IDLE){
                    Log.d("CallAccepted","Call disconnected");
                   // MainActivity.smsManager.sendTextMessage("+918160081299",null,"Call Over",null,null);
                    MainActivity.bluetoothAdapter.disable();
//                    mediaPlayer.pause();



//                    MainActivity.mRecorder.stop();
//                    MainActivity.mRecorder.release();
//                    MainActivity.mRecorder = null;
//                    MainActivity.mPlayer = new MediaPlayer();
//                    try {
//                        MainActivity.mPlayer.setDataSource(MainActivity.mFileName);
//                        MainActivity.mPlayer.prepare();
//                        MainActivity.mPlayer.start();
//                    } catch (IOException e) {
//                        Log.e("TAG", "prepare() failed");
//                    }



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

    public void onInit(int status) {
        tts.speak(message+"call generated", TextToSpeech.QUEUE_FLUSH, null);
    }

    // OnUtteranceCompletedListener impl
    public void onUtteranceCompleted(String utteranceId) {
        tts.shutdown();
        tts = null;
    }
}
