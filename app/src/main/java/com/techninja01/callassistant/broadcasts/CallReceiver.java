package com.techninja01.callassistant.broadcasts;

import static com.techninja01.callassistant.views.MainActivity.audioManager;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.speech.tts.TextToSpeech;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.techninja01.callassistant.views.MainActivity;
import com.techninja01.callassistant.views.SpeakTheMessage;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

public class CallReceiver extends BroadcastReceiver implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {
    String msg_from;
    String msgBody,mainMsgBody,message;
    private TextToSpeech tts = null;
    private String msg = "";
    MediaPlayer mediaPlayer;
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {




//        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
//            Bundle bundle = intent.getExtras();
//            SmsMessage[] smsMessages;
//            if(bundle!=null){
//                try{
//                    Object[] pdus = (Object[]) bundle.get("pdus");
//                    smsMessages = new SmsMessage[pdus.length];
//                    for(int i=0;i<smsMessages.length;i++){
//                        smsMessages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
//                        msg_from = smsMessages[i].getOriginatingAddress();
//                        msgBody = smsMessages[i].getMessageBody();
//                        mainMsgBody = smsMessages[0].getMessageBody();
//                        Toast.makeText(context, "From: "+msg_from+"\tContent: "+mainMsgBody, Toast.LENGTH_SHORT).show();
//                        Intent speechIntent = new Intent();
//                        speechIntent.setClass(context, SpeakTheMessage.class);
//                        speechIntent.putExtra("MESSAGE", msgBody);
//                        speechIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |  Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//                        context.startActivity(speechIntent);
//                    }
//
//
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//        }






        if (MainActivity.telephonyManager.getCallState() == TelephonyManager.CALL_STATE_RINGING) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            MainActivity.telecomManager.acceptRingingCall();
            Toast.makeText(context, "Call accepted", Toast.LENGTH_SHORT).show();
        } else if (MainActivity.telephonyManager.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) {
            Log.d("CallAccepted", "Call is accepted");
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
                message = "Good Morning dear caller, the person you\'re calling is busy";
            }else if(timeOfDay >= 12 && timeOfDay < 16){
                message = "Good Afternoon dear caller, the person you\'re calling is busy";
            }else if(timeOfDay >= 16 && timeOfDay < 21){
                message = "Good Evening dear caller, the person you\'re calling is busy";
            }else if(timeOfDay >= 21 && timeOfDay < 24){
                message = "Good Night dear caller, the person you\'re calling is sleeping";
            }
//                        MainActivity.sendReceive.write(message.getBytes(StandardCharsets.UTF_8));

            for(int i=0;i<1;i++){
                try{
                    MainActivity.smsManager.sendTextMessage("+918160081299",null,message,null,null);
                    Log.d("SMS", "MSG Sent");
                }catch (Exception e){
                    Log.d("SMS", "Error");
                }
            }




//            Intent speechIntent = new Intent();
//            speechIntent.setClass(context, SpeakTheMessage.class);
//            speechIntent.putExtra("MESSAGE", message);
//            speechIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |  Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//            context.startActivity(speechIntent);

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

//
//
//            MainActivity.mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
//            MainActivity.mFileName += "/AudioRecording.3gp";
//            MainActivity.mRecorder = new MediaRecorder();
//            MainActivity.mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//            MainActivity.mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//            MainActivity.mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//            MainActivity.mRecorder.setOutputFile(MainActivity.mFileName);
//            try {
//                MainActivity.mRecorder.prepare();
//            } catch (IOException e) {
//                Log.e("TAG", "prepare() failed");
//            }
//            MainActivity.mRecorder.start();



//            audioManager.setMode(AudioManager.MODE_IN_CALL);
//            audioManager.setSpeakerphoneOn(true);
//            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

            if (!MainActivity.bluetoothAdapter.isEnabled()) {
                MainActivity.bluetoothAdapter.enable();
                MainActivity.bluetoothAdapter.startDiscovery();


//                try{
//                    MainActivity.smsManager.sendTextMessage("+916352468065",null,message,null,null);
//                    Log.d("SMS", "MSG Sent");
//                }catch (Exception e){
//                    Log.d("SMS", "Error");
//                }

//                AudioManager audioManager = (AudioManager) MainActivity.context.getSystemService(Context.AUDIO_SERVICE);
//                audioManager.setSpeakerphoneOn(true);
//                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);


            }else{
                Toast.makeText(context, "Bluetooth already enabled", Toast.LENGTH_SHORT).show();
//                try{
//                    MainActivity.smsManager.sendTextMessage("+916352468065",null,message,null,null);
//                    Log.d("SMS", "MSG Sent");
//                }catch (Exception e){
//                    Log.d("SMS", "Error");
//                }
            }
        }else if(MainActivity.telephonyManager.getCallState()==TelephonyManager.CALL_STATE_IDLE){
            Log.d("CallAccepted","Call disconnected");
            //MainActivity.smsManager.sendTextMessage("+918160081299",null,"Call Over",null,null);
            MainActivity.bluetoothAdapter.disable();


//            mediaPlayer.pause();



//            MainActivity.mRecorder.stop();
//            MainActivity.mRecorder.release();
//            MainActivity.mRecorder = null;
//            MainActivity.mPlayer = new MediaPlayer();
//            try {
//                MainActivity.mPlayer.setDataSource(MainActivity.mFileName);
//                MainActivity.mPlayer.prepare();
//                MainActivity.mPlayer.start();
//            } catch (IOException e) {
//                Log.e("TAG", "prepare() failed");
//            }


        }
    }



    public void onInit(int status) {
        tts.speak("Call generated", TextToSpeech.QUEUE_FLUSH, null);
    }

    // OnUtteranceCompletedListener impl
    public void onUtteranceCompleted(String utteranceId) {
        tts.shutdown();
        tts = null;
    }
}
