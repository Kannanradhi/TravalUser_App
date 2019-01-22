package com.cabily.utils;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.mylibrary.xmpp.XmppService;

import java.util.HashMap;


/**
 * Created by Prem Kumar and Anitha on 1/25/2016.
 */
public class IdentifyAppKilled extends Service {
    SessionManager sessionManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sessionManager = new SessionManager(IdentifyAppKilled.this);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (sessionManager != null) {
            sessionManager.setAppStatus("dead");
        }

        HashMap<String, String> ride_status = sessionManager.getrideStatus();
        String status = ride_status.get(SessionManager.KEY_RIDE_STATUS);
        if (status.equalsIgnoreCase("1")) {

            System.out.println("----------------jai------------------ONRIDE-------------------------------"+status);
            if(isMyServiceRunning(XmppService.class)) {
                stopService(new Intent(getApplicationContext(), XmppService.class));
                System.out.println("----------prem service restarted-----------------");
                System.out.println("--------------service stopped...");
            }

            if(!isMyServiceRunning(XmppService.class)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !sessionManager.getAppStatus().get(SessionManager.KEY_APP_STATUS).equalsIgnoreCase("resume")) {
//                    startForegroundService(new Intent(getApplicationContext(), XmppService.class));
                    DemoSyncJob.scheduleJob(getApplicationContext());
                } else {
                    startService(new Intent(getApplicationContext(), XmppService.class));
                }
//                startService(new Intent(getApplicationContext(), XmppService.class));
                System.out.println("----------prem service restarted-----------------");
                System.out.println("*******************Xmpp =>service restarted...");
            }

        } else {
            System.out.println("----------------jai------------------DESTROY SERVICE1-------------------------------");
            stopService(new Intent(getApplicationContext(), XmppService.class));
            Log.e("ClearFromRecentService", "Service Destroyed");
        }

//        stopService(new Intent(getApplicationContext(), XmppService.class));
//        stopService(new Intent(getApplicationContext(), GEOService.class));
    }

    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        boolean b = false;
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                System.out.println("1 already running");
                b = true;
                break;
            } else {
                System.out.println("2 not running");
                b = false;
            }
        }
        System.out.println("3 not running");
        return b;
    }
}