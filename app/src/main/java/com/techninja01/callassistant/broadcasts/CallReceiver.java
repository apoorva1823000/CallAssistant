package com.techninja01.callassistant.broadcasts;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.techninja01.callassistant.views.MainActivity;

import java.util.Calendar;

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
            Toast.makeText(context, "Call accepted", Toast.LENGTH_SHORT).show();
        } else if (MainActivity.telephonyManager.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) {
            Log.d("CallAccepted", "Call is accepted");
            String message = "Dear caller, the person you\'re calling is not available";
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



            if (!MainActivity.bluetoothAdapter.isEnabled()) {
                MainActivity.bluetoothAdapter.enable();
                MainActivity.bluetoothAdapter.startDiscovery();


                try{
                    MainActivity.smsManager.sendTextMessage("+918160081299",null,message,null,null);
                    Log.d("SMS", "MSG Sent");
                }catch (Exception e){
                    Log.d("SMS", "Error");
                }

//                AudioManager audioManager = (AudioManager) MainActivity.context.getSystemService(Context.AUDIO_SERVICE);
//                audioManager.setSpeakerphoneOn(true);
//                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);


            }else{
                Toast.makeText(context, "Bluetooth already enabled", Toast.LENGTH_SHORT).show();
                try{
                    MainActivity.smsManager.sendTextMessage("+918160081299",null,message,null,null);
                    Log.d("SMS", "MSG Sent");
                }catch (Exception e){
                    Log.d("SMS", "Error");
                }
            }
        }else if(MainActivity.telephonyManager.getCallState()==TelephonyManager.CALL_STATE_IDLE){
            Log.d("CallAccepted","Call disconnected");
            //MainActivity.smsManager.sendTextMessage("+918160081299",null,"Call Over",null,null);
            MainActivity.bluetoothAdapter.disable();
        }
    }
}
