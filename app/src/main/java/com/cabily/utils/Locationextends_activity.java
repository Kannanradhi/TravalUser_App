package com.cabily.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.text.DecimalFormat;


public class Locationextends_activity extends AppCompatActivity {
    private static final int REQUEST_WRITE_STORAGE_REQUEST_CODE = 102;
    private static final int REQUEST_LOCATION = 103;
    Extras extras;
    Handler handler;
    public Getlocation loc_services;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    PendingResult<LocationSettingsResult> result;
    private int locationCount=0;

    public boolean isGPSEnabled = false;
    // Flag for network status
    boolean isNetworkEnabled = false;
    // Flag for GPS status
    public boolean canGetLocation = false;
    boolean checkGPS = false;
    boolean checkNetwork = false;
    Location loc;
    double latitude;
    double longitude;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
    protected LocationManager locationManager;

    Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        handler = new Handler();
        extras = new Extras(Locationextends_activity.this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= 21) {
                    if (!extras.checkAccessFineLocationPermission() || !extras.checkAccessCoarseLocationPermission() || !extras.checkWriteExternalStoragePermission()) {
                        requestAppPermissions();
                    } else {
//                        extras.startService(SplashPage.this,"Getlocation");

                        Intent intent = new Intent(Locationextends_activity.this, Getlocation.class);
                        startService(intent);
                        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
                        removerunnable();
                        handler.postDelayed(runnable, 2000);
                    }
                } else {

                    Intent intent = new Intent(Locationextends_activity.this, Getlocation.class);
                    startService(intent);
                    bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
                    removerunnable();
                    handler.postDelayed(runnable, 2000);
                }
            }
        }, 5000);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }


    /**
     * Callbacks for service binding, passed to bindService()
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MyService instance
            Getlocation.LocalBinder binder = (Getlocation.LocalBinder) service;
            loc_services = binder.getServiceInstance();
            loc_services.registerClient(Locationextends_activity.this);//Get instance of your service!
//            loc_services.setCallbacks_location(SplashPage.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }
    };

    private void requestAppPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        if (extras.hasReadPermissions() && extras.hasWritePermissions() && extras.hasLocatePermissions()) {
            return;
        }

        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION

                }, REQUEST_WRITE_STORAGE_REQUEST_CODE); // your request code
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Intent intent = new Intent(Locationextends_activity.this, Getlocation.class);
                    startService(intent);
                    bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
                    removerunnable();
                    handler.postDelayed(runnable, 2000);
                } else {
                    finish();
                }
                break;
        }
    }


    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!extras.isgpsavailable()) {
                enableGpsService();
            } else {
                if(locationCount<2)
                {
                    getLocationManual();
                    locationCount+=1;
                }

                removerunnable();
                handler.postDelayed(runnable, 2000);
            }
        }
    };


    //Enabling Gps Service
    private void enableGpsService() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5 * 1000);
        mLocationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                //final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:

                        Intent intent = new Intent(Locationextends_activity.this, Getlocation.class);
                        startService(intent);
                        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

                        removerunnable();
                        handler.postDelayed(runnable, 2000);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(Locationextends_activity.this, REQUEST_LOCATION);
                        } catch (IntentSender.SendIntentException e) {
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
        System.out.println("************ GC Memory cleared***********");
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Intent intent = new Intent(Locationextends_activity.this, Getlocation.class);
        stopService(intent);
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }

        removerunnable();
    }

    private Location getLocationManual2() {

        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

            // get GPS status
            checkGPS = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // get network provider status
            checkNetwork = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!checkGPS && !checkNetwork) {
                Toast.makeText(Locationextends_activity.this, "No Service Provider is available", Toast.LENGTH_SHORT).show();
            } else {
                this.canGetLocation = true;

                // if GPS Enabled get lat/long using GPS Services
                if (checkGPS) {

          /*          if (ActivityCompat.checkSelfPermission(Locationextends_activity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                    }*/
//                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (locationManager != null) {
                        if (!extras.checkAccessFineLocationPermission() || !extras.checkAccessCoarseLocationPermission() || !extras.checkWriteExternalStoragePermission()) {
                            loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                        if (loc != null) {
                            latitude = loc.getLatitude();
                            longitude = loc.getLongitude();

                            SessionManager sessionManager = new SessionManager(getApplicationContext());


                            DecimalFormat dFormat = new DecimalFormat("#.######");
                            latitude= Double.valueOf(dFormat .format(latitude));
                            longitude= Double.valueOf(dFormat .format(longitude));

                            sessionManager.setLatitude(String.valueOf(latitude));
                            sessionManager.setLongitude(String.valueOf(longitude));

                        /*    sessionManager.setLatitude(String.valueOf(latitude));
                            sessionManager.setLongitude(String.valueOf(longitude));*/
                            Toast.makeText(getApplicationContext(), "Manual Lat:" + latitude, Toast.LENGTH_SHORT).show();
                            Toast.makeText(getApplicationContext(), "Manual Lon:" + longitude, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Manual LatLon Null:", Toast.LENGTH_SHORT).show();

                        }
                    }
                }

            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return loc;
    }

    public void getLocationManual() {
        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            Toast.makeText(getApplicationContext(), "Manual isNetworkEnabled:" + isNetworkEnabled, Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), "Manual isGPSEnabled:" + isGPSEnabled, Toast.LENGTH_SHORT).show();

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                   /* locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);*/
                    if (locationManager != null) {
                        if (!extras.checkAccessFineLocationPermission() || !extras.checkAccessCoarseLocationPermission() || !extras.checkWriteExternalStoragePermission()) {
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        }
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            SessionManager sessionManager = new SessionManager(getApplicationContext());
                            DecimalFormat dFormat = new DecimalFormat("#.######");
                            latitude= Double.valueOf(dFormat .format(latitude));
                            longitude= Double.valueOf(dFormat .format(longitude));

                            sessionManager.setLatitude(String.valueOf(latitude));
                            sessionManager.setLongitude(String.valueOf(longitude));

                        /*    sessionManager.setLatitude(String.valueOf(latitude));
                            sessionManager.setLongitude(String.valueOf(longitude));*/
                            Toast.makeText(getApplicationContext(), "Manual Lat:" + latitude, Toast.LENGTH_SHORT).show();
                            Toast.makeText(getApplicationContext(), "Manual Lon:" + longitude, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Manual LatLon Null:", Toast.LENGTH_SHORT).show();

                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        /*locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);*/
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                SessionManager sessionManager = new SessionManager(getApplicationContext());
                                DecimalFormat dFormat = new DecimalFormat("#.######");
                                latitude= Double.valueOf(dFormat .format(latitude));
                                longitude= Double.valueOf(dFormat .format(longitude));

                                sessionManager.setLatitude(String.valueOf(latitude));
                                sessionManager.setLongitude(String.valueOf(longitude));

                        /*    sessionManager.setLatitude(String.valueOf(latitude));
                            sessionManager.setLongitude(String.valueOf(longitude));*/
                                Toast.makeText(getApplicationContext(), "Manual Lat:" + latitude, Toast.LENGTH_SHORT).show();
                                Toast.makeText(getApplicationContext(), "Manual Lon:" + longitude, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Manual LatLon Null:", Toast.LENGTH_SHORT).show();

                            }
                        }
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                switch (resultCode) {
                    case Activity.RESULT_OK: {
                        Intent intent = new Intent(Locationextends_activity.this, Getlocation.class);
                        startService(intent);
                        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
//                        Toast.makeText(FetchingDataActivity.this, "Location enabled!", Toast.LENGTH_LONG).show();
                        removerunnable();
                        handler.postDelayed(runnable, 2000);
                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        Intent intent = new Intent(Locationextends_activity.this, Getlocation.class);
                        startService(intent);
                        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
//                        Toast.makeText(FetchingDataActivity.this, "Location enabled!", Toast.LENGTH_LONG).show();
                        removerunnable();
                        handler.postDelayed(runnable, 2000);
//                        finish();
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
        }
    }


    public void removerunnable() {
        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

}
