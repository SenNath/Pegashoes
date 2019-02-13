package com.hemanthkandula.hackwpi2019;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class newAct extends AppCompatActivity {
int ccc = 0;

    Runnable runnablenew;
    Handler handlernew;
    LocationManager locationManager;
    double longitudeBest, latitudeBest;
    double longitudeGPS, latitudeGPS;
    double longitudeNetwork, latitudeNetwork;
    TextView longitudeValueBest, latitudeValueBest,piezo,temper,magno,humi;
//    TextView longitudeValueGPS, latitudeValueGPS;
//    TextView longitudeValueNetwork, latitudeValueNetwork;
    FirebaseFirestore db;
List<String > piezolist = Arrays.asList("pizeo:1024","pizeo:1024","pizeo:700","pizeo:1024","pizeo:860","pizeo:966","pizeo:1024");



    List<String > maglist = Arrays.asList("magnetometer:71.15","magnetometer:69.85","magnetometer:68.59","magnetometer:76.62","magnetometer:69.18","magnetometer:73.59","magnetometer:74.12");



    List<String > humlist = Arrays.asList("moisture:49","moisture:40","moisture:50","moisture:43","moisture:44","moisture:44","moisture:47");

    List<String > templist = Arrays.asList("Temperature:35","Temperature:48","Temperature:39","Temperature:50","Temperature:45","Temperature:50","Temperature:46");

    int count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        db = FirebaseFirestore.getInstance();

        longitudeValueBest = (TextView) findViewById(R.id.longitudeValueBest);
        longitudeValueBest = (TextView) findViewById(R.id.longitudeValueBest);
        longitudeValueBest = (TextView) findViewById(R.id.longitudeValueBest);
        temper = findViewById(R.id.temp);
        humi = findViewById(R.id.hum);
        magno = findViewById(R.id.mag);
        piezo = findViewById(R.id.piezo);


        longitudeValueBest = (TextView) findViewById(R.id.longitudeValueBest);
        latitudeValueBest = (TextView) findViewById(R.id.latitudeValueBest);
//        longitudeValueGPS = (TextView) findViewById(R.id.longitudeValueGPS);
//        latitudeValueGPS = (TextView) findViewById(R.id.latitudeValueGPS);
//        longitudeValueNetwork = (TextView) findViewById(R.id.longitudeValueNetwork);
//        latitudeValueNetwork = (TextView) findViewById(R.id.latitudeValueNetwork);






        toggleGPSUpdates();
        toggleNetworkUpdates();


        toggleBestUpdates();


        handlernew = new Handler();

// Define the code block to be executed
         runnablenew = new Runnable() {
           @Override
           public void run() {

               piezo.setText(piezolist.get(ccc));
               magno.setText(maglist.get(ccc));
               humi.setText(humlist.get(ccc));
               temper.setText(templist.get(ccc));

               ccc= ccc+1;

               if(ccc==6){
                   ccc=0;

               }


               handlernew.postDelayed(runnablenew, 500);
           }
       };


        handlernew.post(runnablenew);
    }

    private boolean checkLocation() {
        if(!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void toggleGPSUpdates() {
        if(!checkLocation())
            return;

            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,  100, 10, locationListenerGPS);

    }

    public void toggleBestUpdates() {
        if(!checkLocation())
            return;

            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(true);
            criteria.setPowerRequirement(Criteria.POWER_HIGH);
            String provider = locationManager.getBestProvider(criteria, true);
            if(provider != null) {


                locationManager.requestLocationUpdates(provider,  100, 10, locationListenerBest);
                Toast.makeText(this, "Best Provider is " + provider, Toast.LENGTH_LONG).show();
            }

    }

    public void toggleNetworkUpdates() {
        if(!checkLocation())
            return;

            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 100, 10, locationListenerNetwork);
            Toast.makeText(this, "Network provider started running", Toast.LENGTH_LONG).show();

    }

    private final LocationListener locationListenerBest = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitudeBest = location.getLongitude();
            latitudeBest = location.getLatitude();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Map<String, Object> user = new HashMap<>();
                    user.put("lat",  latitudeBest);
                    user.put("long", longitudeBest);

// Add a new document with a generated ID

                    db.collection("gps").document("QvFfCM86QQJRAIk68MQv").update(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    String locs= latitudeGPS+","+longitudeGPS;

                                    Log.w("TAG", "onSuccess"+count);
                                    count = count+1;
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("TAG", "Error adding document", e);
                                }
                            });




                    longitudeValueBest.setText(longitudeBest + "");
                    latitudeValueBest.setText(latitudeBest + "");
                    Toast.makeText(newAct.this, "Best Provider update", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private final LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitudeNetwork = location.getLongitude();
            latitudeNetwork = location.getLatitude();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {



//
//                    longitudeValueNetwork.setText(longitudeNetwork + "");
//                    latitudeValueNetwork.setText(latitudeNetwork + "");
                    Toast.makeText(newAct.this, "Network Provider update", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private final LocationListener locationListenerGPS = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitudeGPS = location.getLongitude();
            latitudeGPS = location.getLatitude();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    longitudeValueGPS.setText(longitudeGPS + "");
//                    latitudeValueGPS.setText(latitudeGPS + "");





                    Toast.makeText(newAct.this, "GPS Provider update", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

}