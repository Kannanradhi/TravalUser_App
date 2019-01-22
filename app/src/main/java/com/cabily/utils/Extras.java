package com.cabily.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;


public class Extras {
    public Context mcontext;

    public Extras(Context context) {
        mcontext = context;
    }

    public boolean isConnectingToInternet() {
        ConnectivityManager connectivity = (ConnectivityManager) mcontext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }

    public boolean checkAccessFineLocationPermission() {
        int result = ContextCompat.checkSelfPermission(mcontext, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkAccessCoarseLocationPermission() {
        int result = ContextCompat.checkSelfPermission(mcontext, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkWriteExternalStoragePermission() {
        int result = ContextCompat.checkSelfPermission(mcontext, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            int result1 = ContextCompat.checkSelfPermission(mcontext, Manifest.permission.READ_EXTERNAL_STORAGE);

            return true;
        } else {
            return false;
        }
    }

    public boolean hasReadPermissions() {
        return (ContextCompat.checkSelfPermission(mcontext, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    public boolean hasWritePermissions() {
        return (ContextCompat.checkSelfPermission(mcontext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    public boolean hasLocatePermissions() {
        return (ContextCompat.checkSelfPermission(mcontext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }


    public Class createInstanceOfClass(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
        Class classTemp = Class.forName(className);
        return classTemp;
    }

    public boolean isgpsavailable(){
        LocationManager manager = (LocationManager)mcontext.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}
