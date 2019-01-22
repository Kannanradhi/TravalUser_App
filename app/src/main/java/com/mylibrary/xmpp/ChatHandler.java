package com.mylibrary.xmpp;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v7.app.NotificationCompat;
import android.view.View;

import com.cabily.app.AdsPage;
import com.cabily.app.ChatActivity;
import com.cabily.app.FareBreakUp;
import com.cabily.app.PushNotificationAlert;
import com.cabily.app.SplashPage;
import com.cabily.iconstant.Iconstant;
import com.cabily.pojo.ChatPojo;
import com.cabily.utils.SessionManager;
import com.casperon.app.cabily.R;
import com.mylibrary.sqliteDb.ChatDatabaseHelper;

import org.jivesoftware.smack.packet.Message;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by user88 on 11/4/2015.
 */
public class ChatHandler {
    private Context context;
    private IntentService service;

    static SimpleDateFormat df_time = new SimpleDateFormat("hh:mm a");
    ChatDatabaseHelper db;
    SessionManager sessionManager;
    private String rideID;

    public ChatHandler(Context context) {
        this.context = context;
        this.service = service;
        sessionManager = new SessionManager(context);

    }

    public void onHandleChatMessage(Message message) {
        try {
            String data = URLDecoder.decode(message.getBody(), "UTF-8");
            System.out.println("--------------xmpp service data----------------------" + data);
            JSONObject messageObject = new JSONObject(data);

            Calendar cal = Calendar.getInstance();
            String sCurrentTime = df_time.format(cal.getTime());
            db = new ChatDatabaseHelper(context);

            if (messageObject.length() > 0) {
                System.out.println("--------------xmpp service data----------------------" + data);
                String action = (String) messageObject.get(Iconstant.Push_Action);

                if (action.equalsIgnoreCase("PK-TYPING-STOP") || action.equalsIgnoreCase("PK_ONLINE") || action.equalsIgnoreCase("PK_OFFLINE") || action.equalsIgnoreCase("PK-TYPING-START")) {
                    Intent local = new Intent();
                    local.setAction("com.app.conversation.chat");
                    local.putExtra("chat_msg", data);
                    context.sendBroadcast(local);


                } else if (action.equalsIgnoreCase("dectar_chat")) {


                    HashMap<String, String> state = sessionManager.getAppStatus();
                    String Str_driverState = state.get(SessionManager.KEY_APP_STATUS);
                    String sMessage1 = "";
                    sMessage1 = messageObject.getString("desc");


                    db.addChat(new ChatPojo(sMessage1, "OTHER", sCurrentTime, "0"));
                    if (Str_driverState.equalsIgnoreCase("resume")) {
                        Intent local = new Intent();
                        local.setAction("com.app.conversation.chat.send");
                        local.putExtra("chat_msg", data);
                        context.sendBroadcast(local);

                    } else {

                        Intent notificationIntent;
                        if (Str_driverState.equalsIgnoreCase("pause")) {
                            HashMap<String, String> ride = sessionManager.getrideId();
                            rideID = ride.get(SessionManager.KEY_RIDE_ID);
                            notificationIntent = new Intent(context, ChatActivity.class);
                            notificationIntent.putExtra("Ride_id", rideID);
                            if (sessionManager == null) {
                                sessionManager = new SessionManager(context);
                            }

                            sessionManager.setchatNotify("1");
                            if (ChatActivity.chat_activity != null) {
                                ChatActivity.chat_activity.finish();
                            }

                        } else {
                            if (sessionManager == null) {
                                sessionManager = new SessionManager(context);
                            }
                            sessionManager.setchatNotify("1");
                            notificationIntent = new Intent(context, SplashPage.class);
                        }

                        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        Resources res = context.getResources();
                        Notification n;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            String channelId = "chat";
                            CharSequence channelName = "chatChannel";
                            int importance = NotificationManager.IMPORTANCE_HIGH;
                            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
                            nm.createNotificationChannel(notificationChannel);

                            Notification.Builder builder = new Notification.Builder(context, channelId);
                            builder.setContentIntent(contentIntent)
                                    .setSmallIcon(R.drawable.pushlogo)
                                    .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.app_logo))
                                    .setTicker(sMessage1)
                                    .setColor(context.getResources().getColor(R.color.app_color))
                                    .setWhen(System.currentTimeMillis())
                                    .setAutoCancel(true)
                                    .setContentTitle(context.getResources().getString(R.string.app_name))
                                    .setContentText(sMessage1);

                            n = builder.getNotification();
                        } else {
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                            builder.setContentIntent(contentIntent)
                                    .setSmallIcon(R.drawable.pushlogo)
                                    .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.app_logo))
                                    .setTicker(sMessage1)
                                    .setColor(context.getResources().getColor(R.color.app_color))
                                    .setWhen(System.currentTimeMillis())
                                    .setAutoCancel(true)
                                    .setContentTitle(context.getResources().getString(R.string.app_name))
                                    .setLights(0xffff0000, 100, 2000)
                                    .setPriority(Notification.DEFAULT_SOUND)
                                    .setContentText(sMessage1);
                            n = builder.getNotification();
                        }


                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            int smallIconViewId = context.getResources().getIdentifier("right_icon", "id", android.R.class.getPackage().getName());

                            if (smallIconViewId != 0) {
                                if (n.contentView != null)
                                    n.contentView.setViewVisibility(smallIconViewId, View.INVISIBLE);

                                if (n.headsUpContentView != null)
                                    n.headsUpContentView.setViewVisibility(smallIconViewId, View.INVISIBLE);

                                if (n.bigContentView != null)
                                    n.bigContentView.setViewVisibility(smallIconViewId, View.INVISIBLE);
                            }
                        }
                        n.flags |= Notification.FLAG_AUTO_CANCEL;
                        n.defaults |= Notification.DEFAULT_ALL;
                        nm.notify(0, n);
                    }


                } else {
                    if (action.equalsIgnoreCase(Iconstant.PushNotification_AcceptRide_Key)) {
                        sendBroadCastToRideConfirm(messageObject);
                    }
                    if (action.equalsIgnoreCase(Iconstant.PushNotification_AcceptRideLater_Key)) {
                        rideLaterAlert(messageObject);
                    } else if (action.equalsIgnoreCase(Iconstant.PushNotification_CabArrived_Key)) {
                        showCabArrivedAlert(messageObject);
                    } else if (action.equalsIgnoreCase(Iconstant.PushNotification_RideCancelled_Key)) {
                        rideCancelledAlert(messageObject);
                    } else if (action.equalsIgnoreCase(Iconstant.PushNotification_RideCompleted_Key)) {
                        rideCompletedAlert(messageObject);
                    } else if (action.equalsIgnoreCase(Iconstant.PushNotification_RequestPayment_Key)) {
                        requestPayment(messageObject);
                    } else if (action.equalsIgnoreCase(Iconstant.PushNotification_RequestPayment_makepayment_Stripe_Key)) {
                        rideCompletedAlert(messageObject);
                        //makePaymentStripAni(messageObject);
                    } else if (action.equalsIgnoreCase(Iconstant.PushNotification_PaymentPaid_Key)) {
                        paymentPaid(messageObject);
                    } else if (action.equalsIgnoreCase(Iconstant.pushNotificationBeginTrip)) {
                        beginTripMessage(messageObject);
                    } else if (action.equalsIgnoreCase(Iconstant.pushNotificationDriverLoc)) {
                        updateDriverLocation_TrackRide(messageObject);
                    } else if (action.equalsIgnoreCase(Iconstant.pushNotification_Ads)) {
                        display_Ads(messageObject);
                    } else if (action.equalsIgnoreCase(Iconstant.pushNotification_ReloadTrackingPage_Key)) {
                        reloadTracking(messageObject);

                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void reloadTracking(JSONObject jsonObject) throws Exception {

        Intent broadcastIntent1 = new Intent();
        broadcastIntent1.setAction("com.package.ACTION_CLASS_TrackYourRide_REFRESH_page");
        broadcastIntent1.putExtra("rideID", jsonObject.getString(Iconstant.ReloadTrackingPage_RideId));
        context.sendBroadcast(broadcastIntent1);


    }

    private void sendBroadCastToRideConfirm(JSONObject messageObject) throws Exception {

        db.clearTable();
        Intent local = new Intent();
        local.setAction("com.app.pushnotification.RideAccept");
        local.putExtra("driverID", messageObject.getString(Iconstant.DriverID));
        local.putExtra("driverName", messageObject.getString(Iconstant.DriverName));
        local.putExtra("driverEmail", messageObject.getString(Iconstant.DriverEmail));
        local.putExtra("driverImage", messageObject.getString(Iconstant.DriverImage));
        local.putExtra("driverRating", messageObject.getString(Iconstant.DriverRating));
        local.putExtra("driverLat", messageObject.getString(Iconstant.DriverLat));
        local.putExtra("driverLong", messageObject.getString(Iconstant.DriverLong));
        local.putExtra("driverTime", messageObject.getString(Iconstant.DriverTime));
        local.putExtra("rideID", messageObject.getString(Iconstant.RideID));
        local.putExtra("driverMobile", messageObject.getString(Iconstant.DriverMobile));
        local.putExtra("driverCar_no", messageObject.getString(Iconstant.DriverCar_No));
        local.putExtra("driverCar_model", messageObject.getString(Iconstant.DriverCar_Model));
        local.putExtra("userLatitude", messageObject.getString(Iconstant.UserLat));
        local.putExtra("userLongitude", messageObject.getString(Iconstant.UserLong));
        local.putExtra("message", messageObject.getString(Iconstant.Push_Message));
        local.putExtra("Action", messageObject.getString(Iconstant.Push_Action));
        context.sendBroadcast(local);


    }

    private void showCabArrivedAlert(JSONObject messageObject) throws Exception {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("com.package.ACTION_CLASS_TrackYourRide_REFRESH_Arrived_Driver");
        broadcastIntent.putExtra("driverLat", messageObject.getString("key3"));
        broadcastIntent.putExtra("driverLong", messageObject.getString("key4"));
        context.sendBroadcast(broadcastIntent);
    }

    private void rideCancelledAlert(JSONObject messageObject) throws Exception {

        Intent handler_intent = new Intent();
        handler_intent.setAction("com.handler.stop");
        context.sendBroadcast(handler_intent);


        refreshMethod();

        Intent i1 = new Intent(context, PushNotificationAlert.class);
        i1.putExtra("message", messageObject.getString(Iconstant.Push_Message_Cancelled));
        i1.putExtra("Action", messageObject.getString(Iconstant.Push_Action_Cancelled));
        i1.putExtra("RideID", messageObject.getString(Iconstant.RideID_Cancelled));
        i1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i1);
    }

    private void rideLaterAlert(JSONObject messageObject) throws Exception {
        refreshMethod();

        Intent i1 = new Intent(context, PushNotificationAlert.class);
        i1.putExtra("message", messageObject.getString(Iconstant.Push_Message_Cancelled));
        i1.putExtra("Action", messageObject.getString(Iconstant.Push_Action_Cancelled));
        i1.putExtra("RideID", messageObject.getString(Iconstant.RideID_Later));
        i1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i1);
    }

    private void rideCompletedAlert(JSONObject messageObject) throws Exception {
        refreshMethod();

        Intent i1 = new Intent(context, PushNotificationAlert.class);
        i1.putExtra("message", context.getResources().getString(R.string.pushnotification_alert_label_ride_completed));
        i1.putExtra("Action", messageObject.getString(Iconstant.Push_Action_Completed));
        i1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i1);

    }

    private void requestPayment(JSONObject messageObject) throws Exception {
        refreshMethod();

        Intent i1 = new Intent(context, FareBreakUp.class);
        i1.putExtra("message", messageObject.getString(Iconstant.Push_Message_Request_Payment));
        i1.putExtra("Action", messageObject.getString(Iconstant.Push_Action_Request_Payment));
        i1.putExtra("CurrencyCode", messageObject.getString(Iconstant.CurrencyCode_Request_Payment));
        i1.putExtra("TotalAmount", messageObject.getString(Iconstant.TotalAmount_Request_Payment));
        i1.putExtra("TravelDistance", messageObject.getString(Iconstant.TravelDistance_Request_Payment));
        i1.putExtra("Duration", messageObject.getString(Iconstant.Duration_Request_Payment));
        i1.putExtra("WaitingTime", messageObject.getString(Iconstant.WaitingTime_Request_Payment));
        i1.putExtra("RideID", messageObject.getString(Iconstant.RideID_Request_Payment));
        i1.putExtra("UserID", messageObject.getString(Iconstant.UserID_Request_Payment));
        i1.putExtra("DriverName", messageObject.getString(Iconstant.DriverName_Request_Payment));
        i1.putExtra("DriverImage", messageObject.getString(Iconstant.DriverImage_Request_Payment));
        i1.putExtra("DriverRating", messageObject.getString(Iconstant.DriverRating_Request_Payment));
        i1.putExtra("DriverLatitude", messageObject.getString(Iconstant.Driver_Latitude_Request_Payment));
        i1.putExtra("DriverLongitude", messageObject.getString(Iconstant.Driver_Longitude_Request_Payment));
        i1.putExtra("UserName", messageObject.getString(Iconstant.UserName_Request_Payment));
        i1.putExtra("UserLatitude", messageObject.getString(Iconstant.User_Latitude_Request_Payment));
        i1.putExtra("UserLongitude", messageObject.getString(Iconstant.User_Longitude_Request_Payment));
        i1.putExtra("SubTotal", messageObject.getString(Iconstant.subTotal_Request_Payment));
        i1.putExtra("ServiceTax", messageObject.getString(Iconstant.serviceTax_Request_Payment));
        i1.putExtra("TotalPayment", messageObject.getString(Iconstant.Total_Request_Payment));
        i1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i1);
    }

    private void paymentPaid(JSONObject messageObject) throws Exception {
        refreshMethod();

        Intent finish_fareBreakUp = new Intent();
        finish_fareBreakUp.setAction("com.pushnotification.finish.FareBreakUpPaymentList");
        context.sendBroadcast(finish_fareBreakUp);

        Intent finish_MyRidePaymentList = new Intent();
        finish_fareBreakUp.setAction("com.pushnotification.finish.MyRidePaymentList");
        context.sendBroadcast(finish_MyRidePaymentList);

        Intent i1 = new Intent(context, PushNotificationAlert.class);
        i1.putExtra("message", messageObject.getString(Iconstant.Push_Message_Payment_paid));
        i1.putExtra("Action", messageObject.getString(Iconstant.Push_Action_Payment_paid));
        i1.putExtra("RideID", messageObject.getString(Iconstant.RideID_Payment_paid));
        i1.putExtra("UserID", messageObject.getString(Iconstant.UserID_Payment_paid));
        i1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i1);
    }


    private void updateDriverLocation_TrackRide(JSONObject messageObject) throws Exception {

        System.out.println("--------chat handler driver update----------------");

        Intent local = new Intent();
        local.setAction("com.package.ACTION_CLASS_TrackYourRide_REFRESH_UpdateDriver");
        local.putExtra("isContinousRide", "");
        local.putExtra("latitude", messageObject.getString(Iconstant.latitude));
        local.putExtra("longitude", messageObject.getString(Iconstant.longitude));
        local.putExtra("bearing", messageObject.getString(Iconstant.bearing));
        local.putExtra("ride_id", messageObject.getString(Iconstant.ride_id));
        context.sendBroadcast(local);
    }

    private void beginTripMessage(JSONObject messageObject) throws Exception {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("com.package.ACTION_CLASS_TrackYourRide_REFRESH_BeginTrip");
        broadcastIntent.putExtra("drop_lat", messageObject.getString("key3"));
        broadcastIntent.putExtra("drop_lng", messageObject.getString("key4"));
        broadcastIntent.putExtra("pickUp_lat", messageObject.getString("key5"));
        broadcastIntent.putExtra("pickUp_lng", messageObject.getString("key6"));
        broadcastIntent.putExtra("drop_locc", messageObject.getString("key7"));
        context.sendBroadcast(broadcastIntent);
    }

    private void makePaymentStripAni(JSONObject messageObject) throws Exception {
        refreshMethod();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("com.package.ACTION_CLASS_TrackYourRide_REFRESH_MakePayment");
        context.sendBroadcast(broadcastIntent);

        Intent i1 = new Intent(context, FareBreakUp.class);
        i1.putExtra("RideID", messageObject.getString(Iconstant.Make_Payment));
        i1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i1);
    }


    private void display_Ads(JSONObject messageObject) throws Exception {
        Intent i1 = new Intent(context, AdsPage.class);
        i1.putExtra("AdsTitle", messageObject.getString(Iconstant.Ads_title));
        i1.putExtra("AdsMessage", messageObject.getString(Iconstant.Ads_Message));
        if (messageObject.has(Iconstant.Ads_image)) {
            i1.putExtra("AdsBanner", messageObject.getString(Iconstant.Ads_image));
        }
        i1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i1);
    }


    private void refreshMethod() {
        Intent finish_fareBreakUp = new Intent();
        finish_fareBreakUp.setAction("com.pushnotification.finish.FareBreakUp");
        context.sendBroadcast(finish_fareBreakUp);

        Intent finish_timerPage = new Intent();
        finish_timerPage.setAction("com.pushnotification.finish.TimerPage");
        context.sendBroadcast(finish_timerPage);

        Intent finish_pushAlert = new Intent();
        finish_pushAlert.setAction("com.pushnotification.finish.PushNotificationAlert");
        context.sendBroadcast(finish_pushAlert);

        Intent finish_MyRideDetails = new Intent();
        finish_MyRideDetails.setAction("com.pushnotification.finish.MyRideDetails");
        context.sendBroadcast(finish_MyRideDetails);

        Intent local = new Intent();
        local.setAction("com.pushnotification.finish.trackyourRide");
        context.sendBroadcast(local);

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("com.pushnotification.updateBottom_view");
        context.sendBroadcast(broadcastIntent);


    }

    private void trackRideRefreshMethod() {
        Intent finish_fareBreakUp = new Intent();
        finish_fareBreakUp.setAction("com.pushnotification.finish.FareBreakUp");
        context.sendBroadcast(finish_fareBreakUp);

        Intent finish_timerPage = new Intent();
        finish_timerPage.setAction("com.pushnotification.finish.TimerPage");
        context.sendBroadcast(finish_timerPage);

        Intent finish_pushAlert = new Intent();
        finish_pushAlert.setAction("com.pushnotification.finish.PushNotificationAlert");
        context.sendBroadcast(finish_pushAlert);

        Intent finish_MyRideDetails = new Intent();
        finish_MyRideDetails.setAction("com.pushnotification.finish.MyRideDetails");
        context.sendBroadcast(finish_MyRideDetails);

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("com.pushnotification.updateBottom_view");
        context.sendBroadcast(broadcastIntent);

    }
}
