package edu.neu.madcourse.gauravrane.twoplayer;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import edu.neu.madcourse.gauravrane.R;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class TwoPlayerBroadcastService extends IntentService{
	
	static final String TAG = "TwoPlayerService";
	public static final String APP_ACTIVE = "isAppActive";
	private static final String BUNDLE_DATA = "BundleData";
	public static final String NOTIFICATION_SET = "notificationSet";
	private static final String NOTIFICATION_BUNDLE = "notificationBundle";
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	public static final String DB_INTENT = "edu.neu.madcourse.gauravrane.broadcast.PLAYER_ONE";
	
	public TwoPlayerBroadcastService(){
	  super("TwoPlayerBroadcastService");
	}
	
	@Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) { 
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString(),"");
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " + extras.toString(),"");
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            	String value = extras.toString();
            	//Log.d(TAG, "Message reached inside receive whole value " + value);
            	SharedPreferences prefs = getGCMPreferences();
            	boolean isAppActive = prefs.getBoolean(APP_ACTIVE, false);
            	Log.d(TAG, "App Active status" + isAppActive);
            	if(isAppActive){
            		sendBroadcastToGCMMain(value);
            	}else{
            		sendNotification("Received new message from Opponent... ",value);
            	}
            }
            
        TwoPlayerBroadcastReceiver.completeWakefulIntent(intent);
      }  
    }
	
	private void sendBroadcastToGCMMain(String bundleMessage){
    	//Log.d(TAG,"inside sendBroadcastToGCMMAin: bundle " + bundle);
        Intent i = new Intent();
        i.setAction(DB_INTENT);
        i.putExtra(BUNDLE_DATA, bundleMessage);
        this.sendBroadcast(i);
    }
    
	// TODO: keep a flag to tell which activity to go to.
    private void sendNotification(String msg,String value) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent comMainActivityIntent = new Intent(this,TwoPlayerNewGameOption.class);
        comMainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        comMainActivityIntent.putExtra(BUNDLE_DATA, value);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,comMainActivityIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        
        
        SharedPreferences prefs = getGCMPreferences();
    	Editor editor = prefs.edit();
    	editor.putBoolean(NOTIFICATION_SET, true);
    	editor.putString(NOTIFICATION_BUNDLE, value);
    	editor.commit();
    	
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setContentTitle("GCM Notification")
        .setSmallIcon(R.drawable.display_image)
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setAutoCancel(true)
        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
	
	 private SharedPreferences getGCMPreferences() {
	        return getSharedPreferences(TwoPlayerNewGameOption.class.getSimpleName(),
	                Context.MODE_PRIVATE);
	    }
}
