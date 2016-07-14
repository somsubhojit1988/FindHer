package com.example.subhojitsom.maprendering;

import android.*;
import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.location.LocationListener;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Timer;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener,LocationReceiver.LocationFeedListener,LocationTransmitter.LocationTransmitterListener {
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String TAG="MapActivity";
    private GoogleMap mMap = null;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation = null;
    private boolean markerAdded = false;
    private Marker mMarker = null;
    private final String mServerUrl = "http://172.27.196.24:8080/MongoServlet/MongoServlet";
    /*private FetchLocationTask mFetchTask;*/
    private LocationReceiver mReceiver;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        if (mGoogleApiClient == null) {
            // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
            // See https://g.co/AppIndexing/AndroidStudio for more information.
            mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(AppIndex.API).build();
        }
     /*
        this.fetchTimer = new Timer("Fetch frien's location timer");
        this.fetchTimer.schedule(new FetchLocationTask(this,this.mServerUrl,"collins.sam@tpvision.com"),7000,10000);
     */
        /*this.mReceiver = new LocationReceiver(this.mServerUrl,this);*/

        this.mReceiver.execute(this.mServerUrl);
        /*this.mFetchTask = new FetchLocationTask(this,this.mServerUrl,"collins.sam@tpvision.com");*/
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onStart() {
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.subhojitsom.maprendering/http/host/path")
        );
        AppIndex.AppIndexApi.start(mGoogleApiClient, viewAction);
    }

    @Override
    protected void onDestroy() {
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
        super.onDestroy();

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        /*http://172.27.196.24:8080/MongoServlet/MongoServlet?retreive&nameId=collins.sam@tpvision.com*/

    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG,"Googgle ApiClient Connected...");
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, createLocationRequest(), this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG,"Location access permission granted");
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {
                    Log.d(TAG,"GoogleApiClient::onConnected->Will update map with location");
                   /*Update the map*/
                    updateMap();
                }else
                    Log.e(TAG,"Location is NULL");
            }
        }

        /*if(!this.isDestroyed())
            this.mHandler.post(this.mFetchTask);*/
    }

    private LocationRequest createLocationRequest() {
        LocationRequest  mLocReq = LocationRequest.create();
        mLocReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocReq.setInterval(10000);
        return  mLocReq;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mLastLocation != null&& mMap!=null) {
         /*   Update the map*/
            /*LatLng mPos = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mPos, 15));
           *//* mMap.addMarker(new MarkerOptions().position(mPos).title("This is my POSITION"));*/
            Log.d(TAG,"onLocationChanged->Will update map/LocationServer with location");
            updateMap();
            try {
                transmitSelfLocation();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void transmitSelfLocation() throws IOException {
        LocationTransmitter loc = new LocationTransmitter(mServerUrl,this);
        loc.execute(mLastLocation);

    }
    private void updateMap() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                !mMap.isMyLocationEnabled()) {
            Log.d(TAG, "Enabling Mylocation layer");
            mMap.setMyLocationEnabled(true);
        }
        LatLng mPos = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        Log.d(TAG, "Will update Map [ Latitude :  " + mLastLocation.getLatitude() + " Longitude : " + mLastLocation.getLongitude() + "]");
        if (!markerAdded) {
            Toast.makeText(this.getApplicationContext(), "Updating map with my position", Toast.LENGTH_LONG).show();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mPos, 15));
            mMarker = mMap.addMarker(new MarkerOptions().position(mPos).title("This is my POSITION"));
            Log.d(TAG, "MY POSTION marker added");
            markerAdded = true;
        } else {
            if (mMarker != null) {
                /*mMarker.remove();
                mMarker =  mMap.addMarker(new MarkerOptions().position(mPos).title("This is my POSITION"));}*/
                    animateMarker(mMarker, mPos, false);
            }

        }
    }
    private void animateMarker(final Marker marker, final LatLng toPosition,
                              final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;
        final Interpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }
    @Override
    public void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.subhojitsom.maprendering/http/host/path")
        );
        AppIndex.AppIndexApi.end(mGoogleApiClient, viewAction);
        mGoogleApiClient.disconnect();
    }
    private void  updateMap(Location location){
        Log.d(TAG,"Friend's location => long : " +location.getLongitude()+ " latitude : "+location.getLatitude());
     /*   this.mReceiver = new LocationReceiver(this.mServerUrl,this);
        this.mReceiver.execute(this.mServerUrl);*/
    }
    @Override
    public void onLocationUpdated(Long longitude, Long latitude) {
        Location newLocation = new Location("Friend's location");
        newLocation.setLongitude(longitude);
        newLocation.setLatitude(latitude);
        updateMap(newLocation);

    }
    @Override
    public void onLocationTransmitted(LatLng loc) {
        Log.d(TAG,"onLocationTransmitted_CALLBACK=> After transmitting location  "+loc.toString());
    }
}