package com.example.subhojitsom.maprendering;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/*import com.google.android.gms.location.LocationRequest;*/

/**
 * Created by subhojitsom on 15/7/16.
 */
public class LocRequestThread /* extends Thread*/ extends Handler  {


    private LocationReceiver.LocationFeedListener listener;
    String qUrl;
    LocRequestThread(Looper looper, LocationReceiver.LocationFeedListener listener, String serverUrl){
        super(looper);
        this.qUrl=serverUrl;
        this.listener=listener;
    }
    @Override
    public void handleMessage(Message msg) {
        // Process messages here
        if(msg.what==MapsActivity.RECEIVE_LOCATION_UPDATE)
        {
            Log.d("LocationFetcher"," Message.RECEIVE_LOCATION_UPDATE received");
            LocationReceiver lr = new LocationReceiver(qUrl,listener);
            lr.execute();
            return;

        }
        else if(msg.what==MapsActivity.QUIT_RECEIVING)
        {
            Log.d("LocationFetcher"," Will resturn");
            return;
        }
    }

}
