package com.example.subhojitsom.maprendering;


import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by subhojitsom on 13/7/16.
 */
public class LocationTransmitter extends AsyncTask<Location,Integer,String>{
    public static interface LocationTransmitterListener{
        public void onLocationTransmitted(LatLng loc);
    }
    private final String TAG= "LocationTransmitter";
    private String  mServerUrl;
    private LatLng location = null;
    private LocationTransmitterListener listener;

    LocationTransmitter(String serverUrl,LocationTransmitterListener listener){
        this.mServerUrl =serverUrl;
        this.listener = listener;
    }
    @Override
    protected String doInBackground(Location... locations) {
        URL locServerUrl = null;
        int response = - 1;
        String newLocationPost = mServerUrl+"?"+"update"+"&nameId=som.subhojit@tpvision.com"+"&longitude="+
                locations[0].getLongitude()+"&latitude="+locations[0].getLatitude();
        if(this.location == null)
            this.location = new LatLng(locations[0].getLatitude(),locations[0].getLongitude());
        else{
            if(this.location.equals(new LatLng(locations[0].getLatitude(),locations[0].getLongitude()))){
                String ret = "Self location has not changed will not update server";
                Log.d(TAG,ret);
                return ret;
            }
        }
        Log.d(TAG,"Posting to LocationServer=====>"+newLocationPost);
        try {
            locServerUrl = new URL(newLocationPost);
        }catch (MalformedURLException e) {
            Log.e(TAG,"Could not send latest location");
            e.printStackTrace();
        }
        if(locServerUrl==null){
            Log.e(TAG,"Null server url will not post location to server");
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) locServerUrl.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        urlConnection.setDoOutput(true);
        try {
            urlConnection.setRequestMethod("GET");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        urlConnection.setUseCaches(false);
        urlConnection.setConnectTimeout(10000);
        urlConnection.setReadTimeout(10000);
        try {
            urlConnection.connect();
            response = urlConnection.getResponseCode();
            InputStream is = urlConnection.getInputStream();
            is.read();
            Log.d(TAG,"Server response = " +response);
        }catch (IOException e) {
            e.printStackTrace();
        }
        urlConnection.disconnect();
        return new String("Location server responded : "+response);
    }

    @Override
    protected void onPostExecute(String response) {
        this.listener.onLocationTransmitted(this.location);
    }
}
