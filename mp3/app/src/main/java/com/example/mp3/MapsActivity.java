package com.example.mp3;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayDeque;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        LocationListener,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    TextView temp;
    Button orient;
    SensorManager sensorManager;
    Sensor accelerometerSensor;
    SQLHelper helper;
    SQLiteDatabase database;
    final static double g = 9.8;
    int potholeCount = 0;
    double alpha=-1000,beta=-1000,sa,sb,ca,cb;
    static class Accleration {
        public Accleration(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        public double x,y,z;
    }
    ArrayDeque<Accleration> deque;
    private GoogleMap mMap;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100;
    Boolean mLocationPermissionGranted;
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.


                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        fun();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(latLng);
//        markerOptions.title("Current Position");
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
//        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        //stop location updates
//        if (mGoogleApiClient != null) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
//        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient != null &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    private void addPothole() {
        if(mLastLocation!=null) {
            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Pothole");
            mMap.addMarker(markerOptions);
        }
    }

    private void predict() {
        int count = 0;
        Accleration[] arr = new Accleration[20];

        for (int i=0;i<20;++i) {
            arr[i] = deque.removeFirst();
        }

        for (int i=0;i<20;++i)
            if(arr[i].z > 1.3*g){
                count++;
                break;
            }

        for (int i=0;i<19;++i)
            if(Math.abs(arr[i].z-arr[i+1].z)>0.2*g){
                count++;
                break;
            }

        for (int i=0;i<19;++i)
            if(arr[i].z<0.8*g) {
                count++;
                break;
            }

        double sum=0;

        for(int i=0;i<20;++i) {
            sum+=arr[i].z;
        }

        double mean = sum/20;
        sum=0;
        for(int i=0;i<20;++i) {
            double d = (arr[i].z-mean);
            sum+=d*d;
        }

        double stddev = Math.sqrt(sum/20);

        if(stddev>0.2*g) {
            count++;
        }

        if(count>=3) {
            potholeCount++;
            addPothole();
        }

        temp.setText("Pothole Count: "+potholeCount);
    }

    private void fun() {
        helper = new SQLHelper(getApplicationContext());
        database = helper.getWritableDatabase();
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        deque = new ArrayDeque<>();
        temp = findViewById(R.id.count);
        orient = findViewById(R.id.orient);

        orient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alpha = -1000;
            }
        });

        SensorEventListener sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                ContentValues values = new ContentValues();

                double x1=event.values[0],y1=event.values[1],z1=event.values[2];

                if(alpha<-999)
                {
                    alpha =  Math.atan(y1/z1);
                    beta =  Math.atan(-x1/Math.sqrt(y1*y1+z1*z1));
                    sa =  Math.sin(alpha);
                    ca =  Math.cos(alpha);
                    sb =  Math.sin(beta);
                    cb =  Math.cos(beta);
                }

                if(alpha>-999) {
                    //reorientation
                    double x, y, z;
                    x = (cb * x1 + sb * sa * y1 + ca * sb * z1);
                    y = (ca * y1 - sa * z1);
                    z = (-sb * x1 + cb * sa * y1 + cb * ca * z1);

                    deque.addFirst(new Accleration(x, y, z));
                    if (deque.size() >= 20) {
                        //deque.removeLast();
                        predict();
                    }

                    values.put("x", x);
                    values.put("y", y);
                    values.put("z", z);
                    values.put("t", String.valueOf(System.currentTimeMillis()));
                    database.insert("readings", null, values);
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        if(accelerometerSensor == null) {
            Log.i("sensorerror","accleroemter sensor not present");
            finish();
        }
        sensorManager.registerListener(sensorEventListener,accelerometerSensor,50000);
    }
}