package com.cabily.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.cabily.adapter.MyRideCancelTripAdapter;
import com.cabily.iconstant.Iconstant;
import com.cabily.pojo.CancelTripPojo;
import com.cabily.utils.ConnectionDetector;
import com.cabily.utils.SessionManager;
import com.casperon.app.cabily.R;
import com.github.paolorotolo.expandableheightlistview.ExpandableHeightListView;
import com.mylibrary.dialog.PkDialog;
import com.mylibrary.sqliteDb.ChatDatabaseHelper;
import com.mylibrary.volley.ServiceRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by Prem Kumar and Anitha on 11/2/2015.
 */
public class MyRideCancelTrip extends Activity {
    private RelativeLayout back;
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private static Context context;
    private SessionManager session;
    private String UserID = "";

    private ServiceRequest mRequest;
    Dialog dialog;
    ArrayList<CancelTripPojo> itemlist;
    MyRideCancelTripAdapter adapter;
    private ExpandableHeightListView listview;
    private String SrideId_intent = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myride_cancel_trip);
        context = getApplicationContext();
        initialize();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cd = new ConnectionDetector(MyRideCancelTrip.this);
                isInternetPresent = cd.isConnectingToInternet();

                if (isInternetPresent) {
                    cancel_MyRide(Iconstant.cancel_myride_url, itemlist.get(position).getReasonId());
                } else {
                    Alert(getResources().getString(R.string.alert_label_title), getResources().getString(R.string.alert_nointernet));
                }
            }
        });

    }

    private void initialize() {
        session = new SessionManager(MyRideCancelTrip.this);
        cd = new ConnectionDetector(MyRideCancelTrip.this);
        isInternetPresent = cd.isConnectingToInternet();

        back = (RelativeLayout) findViewById(R.id.my_rides_cancel_trip_header_back_layout);
        listview = (ExpandableHeightListView) findViewById(R.id.my_rides_cancel_trip_listView);

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        UserID = user.get(SessionManager.KEY_USERID);

        Intent intent = getIntent();
        SrideId_intent = intent.getStringExtra("RideID");
        try {
            Bundle bundleObject = getIntent().getExtras();
            itemlist = (ArrayList<CancelTripPojo>) bundleObject.getSerializable("Reason");
            adapter = new MyRideCancelTripAdapter(MyRideCancelTrip.this, itemlist);
            listview.setAdapter(adapter);
            listview.setExpanded(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //--------------Alert Method-----------
    private void Alert(String title, String alert) {

        final PkDialog mDialog = new PkDialog(MyRideCancelTrip.this);
        mDialog.setDialogTitle(title);
        mDialog.setDialogMessage(alert);
        mDialog.setPositiveButton(getResources().getString(R.string.action_ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        mDialog.show();
    }
    private void AlertCancel(String title, String alert) {

        final PkDialog mDialog = new PkDialog(MyRideCancelTrip.this);
        mDialog.setDialogTitle(title);
        mDialog.setDialogMessage(alert);
        mDialog.setPositiveButton(getResources().getString(R.string.action_ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                mDialog.dismiss();
                Intent intent = new Intent(MyRideCancelTrip.this, MyRidesDetail.class);
                intent.putExtra("RideID", SrideId_intent);
                startActivity(intent);
                overridePendingTransition(R.anim.enter, R.anim.exit);
                /*onBackPressed();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();*/

            }
        });
        mDialog.show();
    }
    //-----------------------Cancel MyRide Post Request-----------------
    private void cancel_MyRide(String Url, final String reasonId) {
        dialog = new Dialog(MyRideCancelTrip.this);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView dialog_title = (TextView) dialog.findViewById(R.id.custom_loading_textview);
        dialog_title.setText(getResources().getString(R.string.my_rides_cancel_trip_action_cancel));


        System.out.println("-------------Cancel MyRide Url----------------" + Url);

        HashMap<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("id", UserID);
        jsonParams.put("ride_id", SrideId_intent);
        jsonParams.put("reason", reasonId);
        jsonParams.put("user_type", "user");
        mRequest = new ServiceRequest(MyRideCancelTrip.this);
        mRequest.makeServiceRequest(Url, Request.Method.POST, jsonParams, new ServiceRequest.ServiceListener() {
            @Override
            public void onCompleteListener(String response) {

                System.out.println("-------------Cancel MyRide Response----------------" + response);

                String Sstatus = "";

                try {
                    JSONObject object = new JSONObject(response);
                    Sstatus = object.getString("status");
                    if (Sstatus.equalsIgnoreCase("1")) {
                        JSONObject response_object = object.getJSONObject("response");
                        if (response_object.length() > 0) {
                            String message = response_object.getString("message");

                            final PkDialog mDialog = new PkDialog(MyRideCancelTrip.this);
                            mDialog.setDialogTitle(getResources().getString(R.string.action_success));
                            mDialog.setDialogMessage(message);
                            mDialog.setPositiveButton(getResources().getString(R.string.action_ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mDialog.dismiss();
                                    finish();

                                    ChatDatabaseHelper db = new ChatDatabaseHelper(getApplicationContext());
                                    db.clearTable();

                                    session.setrideStatus("0");
                                    session.setchatNotify("0");


                                    Intent broadcastIntent = new Intent();
                                    broadcastIntent.setAction("com.pushnotification.updateBottom_view");
                                    sendBroadcast(broadcastIntent);

                                    Intent broadcastIntent1 = new Intent();
                                    broadcastIntent1.setAction("com.package.MYRIDES_FINISH");
                                    sendBroadcast(broadcastIntent1);

                                    Intent broadcastIntent2 = new Intent();
                                    broadcastIntent2.setAction("com.MyRidesDetail.MYRIDES_FINISH");
                                    sendBroadcast(broadcastIntent2);

                                  //  MyRidesDetail.myrideDetail_class.finish();
                                  //  MyRides.myride_class.finish();


                                    onBackPressed();
                                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                }
                            });
                            mDialog.show();
                        }
                    } else {
                        String Sresponse = object.getString("response");
                        AlertCancel(getResources().getString(R.string.alert_label_title), Sresponse);
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                dialog.dismiss();
            }

            @Override
            public void onErrorListener() {
                dialog.dismiss();
            }
        });
    }

    //-----------------Move Back on pressed phone back button------------------
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)) {
            onBackPressed();
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            return true;
        }
        return false;
    }
}
