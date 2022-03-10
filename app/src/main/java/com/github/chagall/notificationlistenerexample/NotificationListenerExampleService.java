package com.github.chagall.notificationlistenerexample;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

/**
 * MIT License
 *
 *  Copyright (c) 2016 Fábio Alves Martins Pereira (Chagall)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class NotificationListenerExampleService extends NotificationListenerService {

    /*
        These are the package names of the apps. for which we want to
        listen the notifications
     */
    private static final class ApplicationPackageNames {
        public static final String GMAIL_PACK_NAME = "com.google.android.gm";
        public static final String SELF_PACK_NAME = "com.github.chagall.notificationlistenerexample";

    }

    /*
        These are the return codes we use in the method which intercepts
        the notifications, to decide whether we should do something or not
     */
    public static final class InterceptedNotificationCode {
        public static final int GMAIL_CODE = 1;
        public static final int OTHER_NOTIFICATIONS_CODE = 2;
        public static final int SELF_CODE = 3;

    }

    @Override
    public IBinder onBind(Intent intent) {
        postNotification();
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){

        int notificationCode = matchNotificationCode(sbn);
        if (notificationCode == 2)
            return;
        if (notificationCode == 3)
            return;


        String extraTitle = "";
        String extraText = "";
        String extraBigText = "";
        String allMessage = "";
        String speechMessage = "";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            if (sbn.getNotification().extras.get(Notification.EXTRA_TITLE) != null)
                extraTitle += sbn.getNotification().extras.get(Notification.EXTRA_TITLE);
            if (sbn.getNotification().extras.get(Notification.EXTRA_TEXT) != null)
                extraText += sbn.getNotification().extras.get(Notification.EXTRA_TEXT);
            if (sbn.getNotification().extras.get(Notification.EXTRA_BIG_TEXT) != null)
                extraBigText += sbn.getNotification().extras.get(Notification.EXTRA_BIG_TEXT);

        }

        speechMessage = extraText;
        //speechMessage = (String) sbn.getNotification().tickerText;

        int i = extraBigText.indexOf(extraText) + extraText.length();
        String bodyText = extraBigText.substring(i);
        allMessage += "差出人: " + extraTitle + "\nタイトル: " + extraText + "\n本文: " + bodyText;

        Intent intent = new  Intent("com.github.chagall.notificationlistenerexample");
        intent.putExtra("Notification Code", notificationCode );
        intent.putExtra("Notification SpeechMessage", speechMessage );
        intent.putExtra("Notification AllMessage", allMessage);
        sendBroadcast(intent);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        int notificationCode = matchNotificationCode(sbn);
        StatusBarNotification[] activeNotifications = this.getActiveNotifications();
        if(activeNotifications != null && activeNotifications.length > 0) {
            for (int i = 0; i < activeNotifications.length; i++) {
                if (notificationCode == matchNotificationCode(activeNotifications[i])) {
                    Intent intent = new  Intent("com.github.chagall.notificationlistenerexample");
                    intent.putExtra("Notification Code", notificationCode);
                    sendBroadcast(intent);
                    break;
                }
            }
        }

    }

    private int matchNotificationCode(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        Context context = getApplicationContext();
        //Toast.makeText(context , packageName, Toast.LENGTH_LONG).show();

        if (packageName.equals(ApplicationPackageNames.GMAIL_PACK_NAME)){
            return(InterceptedNotificationCode.GMAIL_CODE);
        } else if (packageName.equals(ApplicationPackageNames.SELF_PACK_NAME)) {
            return(InterceptedNotificationCode.SELF_CODE);
        } else {
            return(InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE);
        }
    }


    private void postNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel( "channel_id_1", "channel name 1", NotificationManager.IMPORTANCE_MIN);
            manager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, "channel_id_1")
                .setContentTitle("通知アプリ")
                .setContentText("通知があります")
                .setSmallIcon(android.R.drawable.ic_menu_delete)
                .setOngoing(true)
                .setShowWhen(false)
                .setContentIntent(
                        PendingIntent.getActivity(
                                this,
                                1,
                                new Intent(),
                                PendingIntent.FLAG_UPDATE_CURRENT
                        )
                )
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build();
        startForeground(1, notification);

    }

}
