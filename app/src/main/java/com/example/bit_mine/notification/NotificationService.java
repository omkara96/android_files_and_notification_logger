package com.example.bit_mine.notification;


import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.ArrayList;


public class NotificationService extends NotificationListenerService {
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public ArrayList<String> notificationList = new ArrayList<>();
    AppDatabase appDatabase;
    String packageName = "no-name";
    String title = "Untitled";
    String text = "empty";

    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate() {
        super.onCreate();
        appDatabase = new AppDatabase(getApplicationContext());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        /////////////////////////////////////////////////////////////
        if (Key.jobCompleted) {
            notificationList.clear();
            appDatabase.remove("Notification_List");
            Key.jobCompleted = false;
        }
        /////////////////////////////////////////////////////////////
        packageName = sbn.getPackageName();
       // if (packageName.equals("com.facebook.orca")) {
            Bundle extras = sbn.getNotification().extras;
            if (extras != null) {
                title = extras.getString("android.title");
                if (title != null && !title.equals("Chat heads active")) {
                    text = extras.getString("android.text");
                    String notification = title + " : " + text;
                    notificationList.add(notification);
                    Log.d(Key.TAG, notification);
                }
            }
       // }
        appDatabase.putListString("Notification_List", notificationList);
    }
}
