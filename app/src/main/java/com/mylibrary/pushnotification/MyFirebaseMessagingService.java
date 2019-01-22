package com.mylibrary.pushnotification;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;

import com.cabily.app.PushNotificationAlert;
import com.cabily.app.SplashPage;
import com.cabily.iconstant.Iconstant;
import com.cabily.utils.ConnectionDetector;
import com.cabily.utils.SessionManager;
import com.casperon.app.cabily.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import com.mylibrary.dialog.PkDialog;
import com.mylibrary.sqliteDb.ChatDatabaseHelper;
import com.mylibrary.volley.ServiceRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.graphics.BitmapFactory.decodeResource;

/**
 * Created by user145 on 8/4/2017.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    private String Job_status_message = "", actionnew = "";
    private String key1 = "", key2 = "", key3 = "", key5 = "", key8 = "", key9 = "", message = "", action = "", key4 = "", key6 = "", key7 = "", key10 = "", key11 = "", key12 = "", msg1, title, banner;
    ;

    int no = 114;

    private Boolean isInternetPresent = false;

    private String driver_id = "";
    private ServiceRequest mRequest;
    private boolean isAppInfoAvailable = false;
    SessionManager Session;
    private ConnectionDetector cd;
    private String driverID = "", driver_image = "", driverName = "";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());


        Session = new SessionManager(getApplicationContext());
        if (remoteMessage == null)
            return;

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotification(remoteMessage.getNotification().getBody());
        }

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Data Payload: " + remoteMessage.getData().toString());

            try {


                Map<String, String> params = remoteMessage.getData();
                JSONObject object = new JSONObject(params);
                Log.d("JSON_OBJECT", object.toString());

                handleDataMessage(object);
            } catch (Exception e) {
                Log.d(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    private void handleNotification(String message) {
        Job_status_message = message;

    }

    private void handleDataMessage(JSONObject json) {

        try {

            if (json.has("action")) {
                action = json.getString("action");
            }
            if (json.has("message")) {
                message = json.getString("message");
            }

            if (json.has("key1")) {
                key1 = json.getString("key1");
            }
            if (json.has("key2")) {
                key2 = json.getString("key2");
            }
            if (json.has("key3")) {
                key3 = json.getString("key3");
            }
            if (json.has("key4")) {
                key4 = json.getString("key4");
            }
            if (json.has("key5")) {
                key5 = json.getString("key5");
            }
            if (json.has("key6")) {
                key6 = json.getString("key6");
            }
            if (json.has("key7")) {
                key7 = json.getString("key7");
            }
            if (json.has("key8")) {
                key8 = json.getString("key8");
            }
            if (json.has("key9")) {
                key9 = json.getString("key9");
            }
            if (json.has("key10")) {
                key10 = json.getString("key10");
            }

            if (json.has("key11")) {
                key11 = json.getString("key11");
            }

            if (json.has("key12")) {
                key12 = json.getString("key12");
            }
            if (action.equalsIgnoreCase(Iconstant.pushNotification_Ads)) {
                title = json.getString("key1");
                msg1 = json.getString("key2");
                banner = json.getString("key3");
            }
            sendNotification(message.toString());
            if(action.equalsIgnoreCase(Iconstant.PushNotification_AcceptRide_Key))
            {
                ChatDatabaseHelper db=new ChatDatabaseHelper(getApplicationContext());
                db.clearTable();

            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    @SuppressWarnings("deprecation")
    private void sendNotification(String msg) {
        Intent notificationIntent = null;

        int id = createID();
        Session = new SessionManager(MyFirebaseMessagingService.this);

        notificationIntent = new Intent(MyFirebaseMessagingService.this, SplashPage.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.putExtra("type", "push");

        if (action.equalsIgnoreCase(Iconstant.PushNotification_AcceptRide_Key)) {
            notificationIntent.putExtra("page", "track");
            notificationIntent.putExtra("rideId", key9);

        } else if (action.equalsIgnoreCase(Iconstant.PushNotification_CabArrived_Key)) {
            notificationIntent.putExtra("page", "track");
            notificationIntent.putExtra("rideId", key1);

        } else if (action.equalsIgnoreCase(Iconstant.pushNotificationBeginTrip)) {
            notificationIntent.putExtra("page", "track");
            notificationIntent.putExtra("rideId", key1);

        } else if (action.equalsIgnoreCase(Iconstant.pushNotification_ReloadTrackingPage_Key)) {

            notificationIntent.putExtra("page", "track");
            notificationIntent.putExtra("rideId", key1);

        } else if (action.equalsIgnoreCase(Iconstant.PushNotification_RequestPayment_Key)) {

            notificationIntent.putExtra("page", "farebreakup");
            notificationIntent.putExtra("rideId", key6);


        } else if (action.equalsIgnoreCase(Iconstant.PushNotification_PaymentPaid_Key)) {

            notificationIntent.putExtra("page", "rating");
            notificationIntent.putExtra("rideId", key1);


        } else if (action.equalsIgnoreCase(Iconstant.pushNotification_Ads)) {

            notificationIntent.putExtra("page", "Ads");
            notificationIntent.putExtra("title", key1);
            notificationIntent.putExtra("msg", key2);
            notificationIntent.putExtra("banner", key3);

        } else if (action.equalsIgnoreCase(Iconstant.PushNotification_RideCompleted_Key)) {
            notificationIntent.putExtra("page", "details");
            notificationIntent.putExtra("rideId", key1);

        } else if (action.equalsIgnoreCase(Iconstant.PushNotification_RequestPayment_makepayment_Stripe_Key)) {
            notificationIntent.putExtra("page", "details");
            notificationIntent.putExtra("rideId", key1);

        } else if (action.equalsIgnoreCase(Iconstant.PushNotification_RideCancelled_Key)) {
            notificationIntent.putExtra("page", "details");
            notificationIntent.putExtra("rideId", key1);

        }



        if (Build.VERSION.SDK_INT >= 26) {
            final NotificationManager mNotific=
                    (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

            final int ncode=101;
            final String ChannelID="my_channel_01";
            CharSequence name="Ragav";
            String desc="this is notific";
            int imp=NotificationManager.IMPORTANCE_HIGH;

            @SuppressLint("WrongConstant") NotificationChannel mChannel = new NotificationChannel(ChannelID, name,
                    imp);
            mChannel.setDescription(desc);
            mChannel.setLightColor(Color.CYAN);
            mChannel.canShowBadge();
            mChannel.setShowBadge(true);
            mNotific.createNotificationChannel(mChannel);

            Intent intent = new Intent(getApplicationContext(), SplashPage.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, id, notificationIntent,  PendingIntent.FLAG_CANCEL_CURRENT);
            @SuppressLint("WrongConstant") Notification n= new Notification.Builder(this,ChannelID)
                    .setContentTitle(msg)
                    .setContentText(Job_status_message)
                    .setBadgeIconType(R.drawable.pushlogo)
                    .setNumber(5)
                    .setColor(Color.RED)
                    .setLights(0xffff0000, 100, 2000)
                    .setWhen(System.currentTimeMillis())
                    .setPriority(Notification.DEFAULT_SOUND)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.app_logo)
                    .setAutoCancel(true)
                    .build();
            mNotific.notify(ncode, n);


        }else {

            PendingIntent contentIntent = PendingIntent.getActivity(MyFirebaseMessagingService.this, 1, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            final NotificationManager nm = (NotificationManager) MyFirebaseMessagingService.this.getSystemService(Context.NOTIFICATION_SERVICE);

            Resources res = MyFirebaseMessagingService.this.getResources();
            NotificationCompat.Builder builder = new NotificationCompat.Builder(MyFirebaseMessagingService.this);
            builder.setContentIntent(contentIntent)
                    .setSmallIcon(R.drawable.pushlogo)
                    .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.app_logo))
                    .setTicker(msg)
                    .setColor(Color.RED)
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setContentTitle(getResources().getString(R.string.app_name))
                    .setLights(0xffff0000, 100, 2000)
                    .setPriority(Notification.DEFAULT_SOUND)
                    .setContentText(msg);

            Notification n = builder.getNotification();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int smallIconViewId = getResources().getIdentifier("right_icon", "id", android.R.class.getPackage().getName());

                if (smallIconViewId != 0) {
                    if (n.contentView != null)
                        n.contentView.setViewVisibility(smallIconViewId, View.INVISIBLE);

                    if (n.headsUpContentView != null)
                        n.headsUpContentView.setViewVisibility(smallIconViewId, View.INVISIBLE);

                    if (n.bigContentView != null)
                        n.bigContentView.setViewVisibility(smallIconViewId, View.INVISIBLE);
                }
            }

            n.defaults |= Notification.DEFAULT_ALL;
            nm.notify(id, n);
        }
//        removeNotification(NOTIFICATION_ID,nm);

    }
    private void Alert(String title, String message) {
        final PkDialog mDialog = new PkDialog(MyFirebaseMessagingService.this);
        mDialog.setDialogTitle(title);
        mDialog.setDialogMessage(message);
        mDialog.setPositiveButton(getResources().getString(R.string.alert_label_ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        mDialog.show();
    }

    public int createID() {
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss", Locale.US).format(now));
        return id;
    }

}











