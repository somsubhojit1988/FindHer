package com.example.subhojitsom.maprendering;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LocationReceiver extends AsyncTask<String,Long,Integer>   {
    public static interface LocationFeedListener{
        public void onLocationUpdated(Double longitude,Double latitude);
    }
    private  final String TAG =  "LocationReceiver";
    private String queryUrl;
    private LocationFeedListener listener;
    private Double[] location = new Double[]{Double.valueOf(0),Double.valueOf(0)};
    public void setListener(LocationFeedListener listener){
        this.listener=listener;
    }
    LocationReceiver(String url,LocationFeedListener listener){
        this.queryUrl = url;
        this.listener = listener;
    }
    @Override
    protected Integer doInBackground(String... urls) {
        Log.d(TAG,"QUERY_URL=>" + this.queryUrl);
        if(this.queryUrl == null){
            Log.e(TAG,"QUERY_URL not set returning...");
            return null;
        }
        URL qUrl = null;
        try {
            qUrl= new URL(this.queryUrl);
        } catch (MalformedURLException e) {
            Log.e(TAG,"QUERY_URL malformed exception returning...");
            e.printStackTrace();
            return null;
        }
        HttpURLConnection locServer = null;
        Long[] loc = new Long[2];
        int response = -1;
        String responseMsg ;
        String json;
        List<JSONObject> jsonObjectList = new ArrayList<JSONObject>();
        try {
            locServer= (HttpURLConnection) qUrl.openConnection();
            locServer.setRequestMethod("GET");
            locServer.setUseCaches(false);
            locServer.setConnectTimeout(10000);
            locServer.setReadTimeout(10000);
            locServer.connect();
            response = locServer.getResponseCode();
            final BufferedReader br = new BufferedReader(new InputStreamReader(locServer.getInputStream()));
            StringBuilder sb = new StringBuilder();String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            json = sb.toString();
            JSONArray jsonArray = new JSONArray(json);
            for(int i = 0 ;i<jsonArray.length();i++){
                JSONObject obj = null;
                obj= (JSONObject) jsonArray.get(i);
                if(obj!=null) jsonObjectList.add(obj);
            }
        }catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        locServer.disconnect();

        if(jsonObjectList.size()>0)
            this.parseJsonResponse(jsonObjectList);
        return Integer.valueOf(response);
    }
    @Override
    protected void onPostExecute(Integer result) {
        this.listener.onLocationUpdated(this.location[0],this.location[1]);
    }
    private void parseJsonResponse(List<JSONObject> jsonObjects){
        JSONObject fObj = jsonObjects.get(0);
        assert  fObj!=null;
        JSONObject loc = null;
        String name = "";
        String locType = "";
        String cod="";
        JSONArray cood = null;
        try {
            name = fObj.getString("name_Id");
            loc = fObj.getJSONObject("loc");
            locType = loc.getString("type");
            cood = loc.getJSONArray("coordinates");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG,"FETCHED name_id =>"+name);
        Log.d(TAG,"FETCHED location type =>"+locType);
        Log.d(TAG,"FETCHED coordinates =>"+cood.toString());
        try {
            this.location[0] = cood.getDouble(0);
            this.location[1] = cood.getDouble(1);
            Log.d(TAG,"Longitude=> "+cood.getDouble(0)+" Latitude=>"+cood.getDouble(1));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
