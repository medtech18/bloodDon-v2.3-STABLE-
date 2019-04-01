package fr.blooddonbeta;

/**
 * Created by simohaj17 on 4/12/18.
 */

import com.google.firebase.messaging.FirebaseMessagingService;

import com.google.firebase.messaging.RemoteMessage;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;

import android.content.Intent;
import android.os.Bundle;

import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by brad on 2017/03/09.
 */

public class MyFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = MyFirebaseInstanceIDService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.v(TAG, "Message data payload: " + remoteMessage.getData());

//
        //String message = remoteMessage.getNotification().getBody();
        //String title = remoteMessage.getNotification().getTitle();
        //String from_user_id = remoteMessage.getData().get("frfrom_user_idom_user_id");


        Map<String,String> playLoad = remoteMessage.getData();


       Intent newMessage = new Intent(this, ChatActivity.class);

        newMessage.putExtra("sentFromNotification",true);
        newMessage.putExtra("from_user_id",playLoad.get("from_user_id"));


        showNotification(this, playLoad.get("title"), playLoad.get("body"), newMessage);
    }

    public void showNotification(Context context, String title, String body, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = 1;
        String channelId = "channel-01";
        String channelName = "Channel Name";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(resultPendingIntent);

        notificationManager.notify(notificationId, mBuilder.build());
    }
}