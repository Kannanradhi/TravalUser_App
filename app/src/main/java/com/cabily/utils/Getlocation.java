package com.cabily.utils;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DecimalFormat;


public class Getlocation extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    GoogleApiClient mGoogleApiClient;
    Location mLocation;
    LocationRequest mLocationRequest;
    Handler handler;

    Callbacks callbacks;

    SessionManager sessionManager;
    public static double current_lat = 0.0;
    public static double current_lon = 0.0;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private long UPDATE_INTERVAL = 5 * 1000;  /* 15 secs */
    private long FASTEST_INTERVAL = 500; /* 5 secs */

    Context Mcontext;
    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // Class used for the client Binder.
    public class LocalBinder extends Binder {
        public Getlocation getServiceInstance() {
            return Getlocation.this;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Mcontext = this;
        handler = new Handler();
        sessionManager = new SessionManager(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    //Here Activity register to the service as Callbacks client
    public void registerClient(Activity activity) {
        this.callbacks = (Callbacks) activity;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        if (!mGoogleApiClient.isConnected()) {
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Enable Permissions", Toast.LENGTH_LONG).show();
        }

        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        current_lat = location.getLatitude();
        current_lon = location.getLongitude();

        DecimalFormat dFormat = new DecimalFormat("#.######");
        current_lat= Double.valueOf(dFormat .format(current_lat));
        current_lon= Double.valueOf(dFormat .format(current_lon));

        sessionManager.setLatitude(String.valueOf(current_lat));
        sessionManager.setLongitude(String.valueOf(current_lon));

        Toast.makeText(getApplicationContext(), "onLocationChanged Lat:" + current_lat, Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(), "onLocationChanged Lon:" + current_lon, Toast.LENGTH_SHORT).show();
        callbacks.updateClient(location);
    }


    //callbacks interface for communication with service clients!
    public interface Callbacks {
        void updateClient(Location location);
    }


}
