package edu.neu.madcourse.gauravrane.communication;

import java.util.logging.Logger;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class GCMBroadcastReceiver extends WakefulBroadcastReceiver{
	
	static final String TAG = "GCMMain";
	@Override
    public void onReceive(Context context, Intent intent) {
        // Explicitly specify that GcmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(),
                GCMBroadcastService.class.getName());
        
        Log.d(TAG, "inside onReceive " + context.getPackageName());
        Log.d(TAG, "inside onReceive " + GCMBroadcastReceiver.class.getName());
        
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }

}
