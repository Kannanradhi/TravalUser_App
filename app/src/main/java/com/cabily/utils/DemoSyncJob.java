package com.cabily.utils;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.casperon.app.cabily.R;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.mylibrary.pushnotification.MyFirebaseMessagingService;
import com.mylibrary.xmpp.MyXMPP;
import com.mylibrary.xmpp.XmppService;

import java.util.HashMap;

public class DemoSyncJob extends Job {
    private static int jobId = -1;
    public static final String TAG = ">>>> job_demo_tag";
    public static SessionManager sessionManager;
    static Context context;

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        // run your job here
        Log.d(TAG, "onRunJob: ");
        if (sessionManager == null) {
            sessionManager = new SessionManager(this.getContext());
        }
        context=this.getContext();
        scheduleJob(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = "1";
            String channelName = getContext().getResources().getString(R.string.app_name);
            NotificationManager notificationManager =getContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW));
        }else{
            getContext().startService(new Intent(getContext(), MyFirebaseMessagingService.class));
        }

        if (!isMyServiceRunning(this.getContext(), XmppService.class)) {

            if (MyXMPP.connected) {
                Log.d(TAG, "onRunJob: Xmpp connected");
            } else {

                // get user data from session
                HashMap<String, String> domain = sessionManager.getXmpp();
                String ServiceName = domain.get(SessionManager.KEY_HOST_NAME);
                String HostAddress = domain.get(SessionManager.KEY_HOST_URL);

                HashMap<String, String> user = sessionManager.getUserDetails();
                String USERNAME = user.get(SessionManager.KEY_USERID);
                String PASSWORD = user.get(SessionManager.KEY_XMPP_SEC_KEY);

                System.out.println("----------xmpp ServiceName------------" + ServiceName);
                System.out.println("----------xmpp HostAddress------------" + HostAddress);
                System.out.println("----------xmpp USERNAME------------" + USERNAME);
                System.out.println("----------xmpp PASSWORD------------" + PASSWORD);

                MyXMPP xmpp = MyXMPP.getInstance(this.getContext(), ServiceName, HostAddress, USERNAME, PASSWORD);
                xmpp.connect("onCreate");
                System.out.println("--------------Xmpp Service Created-----------");
            }
        } else {
            if (MyXMPP.connected) {
                Log.d(TAG, "onRunJob: Xmpp connected");
            } else {

                // get user data from session
                HashMap<String, String> domain = sessionManager.getXmpp();
                String ServiceName = domain.get(SessionManager.KEY_HOST_NAME);
                String HostAddress = domain.get(SessionManager.KEY_HOST_URL);

                HashMap<String, String> user = sessionManager.getUserDetails();
                String USERNAME = user.get(SessionManager.KEY_USERID);
                String PASSWORD = user.get(SessionManager.KEY_XMPP_SEC_KEY);

                System.out.println("----------xmpp ServiceName------------" + ServiceName);
                System.out.println("----------xmpp HostAddress------------" + HostAddress);
                System.out.println("----------xmpp USERNAME------------" + USERNAME);
                System.out.println("----------xmpp PASSWORD------------" + PASSWORD);

                MyXMPP xmpp = MyXMPP.getInstance(this.getContext(), ServiceName, HostAddress, USERNAME, PASSWORD);
                xmpp.connect("onCreate");
                System.out.println("--------------Xmpp Service Created-----------");
            }

        }

        return Result.SUCCESS;
    }

    public static void scheduleJob(Context context) {
        if (sessionManager == null) {
            sessionManager = new SessionManager(context);
        }
        int demoSyncJob=new JobRequest.Builder(DemoSyncJob.TAG)
                .setExecutionWindow(1_000L, 30_000L)
                .setUpdateCurrent(true)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .build()
                .schedule();

        sessionManager.setjobid(demoSyncJob);
    }


    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "isMyServiceRunning: ", e);
        }
        return false;
    }
}