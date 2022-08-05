package com.techninja01.callassistant.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.techninja01.callassistant.R;
import com.techninja01.callassistant.broadcasts.CallReceiver;
import com.techninja01.callassistant.broadcasts.OutgoingCall;

public class CallAccept extends Service{
    MediaPlayer mediaPlayer;
    String message;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new CallReceiver();
        new OutgoingCall();
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