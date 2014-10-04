package edu.neu.madcourse.gauravrane.gcm;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import edu.neu.madcourse.gauravrane.R;
import edu.neu.madcourse.gauravrane.communication.ComGSMMainActivity;
import edu.neu.madcourse.gauravrane.twoplayer.TwoPlayerNewGameOption;
import edu.neu.madcourse.gauravrane.twoplayer.TwoPlayerWordGame;
import edu.neu.madcourse.gauravrane.twoplayer.TwoPlayerWordGameStart;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
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

public class BroadcastService extends IntentService{
	private static final String TAG = "Broadcast Service";
	public static final String DB_INTENT = "edu.neu.madcourse.gauravrane.broadcast.PLAYER_ONE";
	public static final String BDGAMEREQ_INTENT = "TWOPLAYER.GAMEREQ";
	public static final String BDGAME_INTENT = "TWOPLAYER.GAME";
	public static final int NOTIFICATION_ID = 1;
	private static final String BUNDLE_DATA = "BundleData";
	public static final String APP_ACTIVE = "isAppActive";
	private static final String MYTURN = "myturn";
	private NotificationManager mNotificationManager;
    public static final String NOTIFICATION_SET = "notificationSet";
	private static final String NOTIFICATION_BUNDLE = "notificationBundle";
	private static final String FIRST_MOVE = "firstMove";
	private static final String TARGET_SCORE = "targetScore";
	
	public static final String INTENT_SET = "intentSet";
	private static final String INTENT_BUNDLE = "intentBundle";
	
	
	public static final String IS_NEW_GAME = "isNewGame";

	public BroadcastService(){
		super("BroadcastService");
	}
	
	@Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) { 
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString(),"");
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString(),"");
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            	String value = extras.toString();
            	//Log.d(TAG, "Message reached inside receive whole value " + value);
            	checkIfCommOrTwoPlayer(value);
            	
            }
        BroadcastReceiver.completeWakefulIntent(intent);
      }  
    }
	
	private void checkIfCommOrTwoPlayer(String dataString) {
		HashMap<String, String> mapBundle = getMapFromBundle(dataString);
		if((mapBundle.get("program")).equals("TWOP")){
			SharedPreferences prefs = getTwoPlayerPreferences();
        	boolean isAppActive = prefs.getBoolean(APP_ACTIVE, false);
        	Log.d(TAG, "App Active status" + isAppActive);
        	String msgTypeString = mapBundle.get("msgtype");
        	if(isAppActive){
        		if(msgTypeString.equals("GAME")){
        	    	Editor editor = prefs.edit();
        	    	editor.putBoolean(MYTURN, true);
        	    	editor.commit();
        			sendBroadcastToGCMMain(dataString,true,true);	
        		}else if(msgTypeString.equals("GAMEQUIT")){
        			sendBroadcastToGCMMain(dataString, true, true);
        		}else{
        			Log.d(TAG, "Sending right broadcast");
        			sendBroadcastToGCMMain(dataString, true,false);
        		}
        	}else{
    			if(msgTypeString.equals("GAME")){
        			Editor editor = prefs.edit();
        	    	editor.putBoolean(MYTURN, true);
        	    	editor.commit();
        			sendNotificationTwoPlayer("Received new message from Opponent... ",dataString,true);
        		}else if(msgTypeString.equals("GAMEQUIT")){
        			sendNotificationTwoPlayer("Received new message from Opponent..", dataString, true);
        		}else{
        			sendNotificationTwoPlayer("Received new message from Opponent... ",dataString,false);
        		}
        	}
		}else if((mapBundle.get("program")).equals("COMM")){
			SharedPreferences prefs = getGCMPreferences();
        	boolean isAppActive = prefs.getBoolean(APP_ACTIVE, false);
        	Log.d(TAG, "App Active status" + isAppActive);
        	if(isAppActive){
        		sendBroadcastToGCMMain(dataString,false,false);
        	}else{
        		sendNotification("Received new message from Opponent... ",dataString);
        	}
		}
	}
    
    protected HashMap<String, String> getMapFromBundle(String bundleMsg){
		HashMap<String,String> mapBundle = new HashMap<String, String>();
		String subBundleMsg = bundleMsg.substring(8,bundleMsg.length()-2);
		Log.d(TAG, "Inside getMapFromBundle: subbundle message " + subBundleMsg);
		String[] commaSeperatedValue = subBundleMsg.split(", ");
		for(String cString:commaSeperatedValue){
			String[] equalSeperatedValue = cString.split("=");
			String key = equalSeperatedValue[0];
			String value = equalSeperatedValue[1];
			Log.d(TAG, "Inside getMapFromBundle: key value pair " + key + " " + value);
			mapBundle.put(key, value);
		}
		return mapBundle;
	}

    private void sendBroadcastToGCMMain(String bundle,boolean isTwoPlayer, boolean isGameMessage){
    	//Log.d(TAG,"inside sendBroadcastToGCMMAin: bundle " + bundle);
        Intent i = new Intent();
        if(isTwoPlayer){
        	if(isGameMessage){
        		i.setAction(BDGAME_INTENT);
        	}else{
        		Log.d(TAG, "Game request broadcast");
        		i.setAction(BDGAMEREQ_INTENT);
        	}
        }else{
        	i.setAction(DB_INTENT);
        }
        i.putExtra(BUNDLE_DATA, bundle);
        this.sendBroadcast(i);
    }
    
    private void sendNotification(String msg,String value) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent comMainActivityIntent = new Intent(this,ComGSMMainActivity.class);
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
    
    private void sendNotificationTwoPlayer(String msg, String value,boolean isGameNotification){
    	Log.d("Inside two player notification", "notification");
    	mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
    	Intent comMainActivityIntent = null;
    	if(isGameNotification){
    		comMainActivityIntent = new Intent(this,TwoPlayerWordGame.class);
    	}else{
    		comMainActivityIntent = new Intent(this,TwoPlayerNewGameOption.class);
    	}
        
        comMainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        comMainActivityIntent.putExtra(BUNDLE_DATA, value);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,comMainActivityIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        
        
        SharedPreferences prefs = getTwoPlayerPreferences();
    	Editor editor = prefs.edit();
    	editor.putBoolean(NOTIFICATION_SET, true);
    	editor.putString(NOTIFICATION_BUNDLE, value);
    	editor.commit();
    	
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setContentTitle("Two Player Game Notification")
        .setSmallIcon(R.drawable.display_image)
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setAutoCancel(true)
        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
 
    
    
    public boolean isAnyActivityInApp(){
    	ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> services = activityManager
                .getRunningTasks(Integer.MAX_VALUE);
        boolean isActivityFound = false;

        if (services.get(0).topActivity.getPackageName().toString()
                .equalsIgnoreCase(this.getPackageName().toString())) {
            isActivityFound = true;
        }
        
        return isActivityFound;
    }
    private SharedPreferences getGCMPreferences() {
        return getSharedPreferences(ComGSMMainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
    
    private SharedPreferences getTwoPlayerPreferences() {
        return getSharedPreferences(TwoPlayerNewGameOption.class.getSimpleName(),
                Context.MODE_MULTI_PROCESS);
    }
}
