package com.example.subhojitsom.maprendering;

import android.os.AsyncTask;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class LocationReceiver extends AsyncTask<String,Long,Integer>{
    public static interface LocationFeedListener{
        public void onLocationUpdated(Long longitude,Long latitude);
    }
    private  final String TAG =  "LocationReceiver";
    private String queryUrl;
    private LocationFeedListener listener;
    public void setListener(LocationFeedListener listener){this.listener=listener;}
    private Long[] location = new Long[]{Long.valueOf(0),Long.valueOf(0)};
    LocationReceiver(String url,LocationFeedListener listener){
        this.queryUrl = url;
        this.listener = listener;
    }
    @Override
    protected Integer doInBackground(String... urls) {
        /*e.g update url ====> http://172.27.196.24:8080/MongoServlet/MongoServlet?retreive&nameId=collins.sam@tpvision.com*/
        Log.d(TAG,"QUERY_URL=>"+this.queryUrl);
        if(this.queryUrl == null){
            Log.e(TAG,"QUERY_URL not set returning...");
            return null;
        }
        URL qUrl = null;
        try {
            /*qUrl= new URL(this.queryUrl);*/
            qUrl= new URL("http://172.27.196.24:8080/MongoServlet/MongoServlet?retreive&nameId=collins.sam@tpvision.com");
        } catch (MalformedURLException e) {
            Log.e(TAG,"QUERY_URL malformed exception returning...");
            e.printStackTrace();
            return null;
        }
        HttpURLConnection locServer = null;
        Long[] loc = new Long[2];
        int response = -1;
        String responseMsg ;
        try {
            locServer= (HttpURLConnection) qUrl.openConnection();
            locServer.setRequestMethod("GET");
            locServer.setUseCaches(false);
            locServer.setConnectTimeout(10000);
            locServer.setReadTimeout(10000);
            locServer.connect();
            response = locServer.getResponseCode();
            /*InputStream is = locServer.getInputStream();
            is.read();*/
            final BufferedReader br = new BufferedReader(new InputStreamReader(locServer.getInputStream()));
            int responseLength = Integer.parseInt(locServer.getHeaderField("Content-Length"));
            char[] buffer=new char[2*responseLength];
            Log.d(TAG,"Server query response :  " +response+" response length : "+responseLength );
            Log.d(TAG,"RESPONSE_MESSAGE=>"+ locServer.getResponseMessage());
            int bytesRead=0;
            bytesRead = br.read(buffer,0,responseLength);
            Log.d(TAG,"Bytes read = "+bytesRead);
            /*
            while (bytesRead < buffer.length){
                bytesRead += br.read(buffer, bytesRead, buffer.length - bytesRead + 1);
            }*/
            final JSONArray arr = new JSONArray(new String(buffer));
            final ArrayList<String> ret = new ArrayList<String>(arr.length());
            for (int i=0; i<arr.length(); i++) {
                ret.add((String) arr.get(i));
            }
            Log.d(TAG,"LocationRetrive Server update response = " +response);
            int i =0;
            for(String str:ret){
                Log.d(TAG,"Server result[" + i+"]=>"+str);
            }
        }catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        locServer.disconnect();
        return Integer.valueOf(response);
    }
    @Override
    protected void onPostExecute(Integer result) {
        this.listener.onLocationUpdated(this.location[0],this.location[1]);
    }

}
