package edu.neu.madcourse.gauravrane.communication;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.internal.em;
import com.google.android.gms.internal.is;

import edu.neu.madcourse.gauravrane.R;
import edu.neu.mhealth.api.KeyValueAPI;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ComGSMMainActivity extends Activity{
	private static final String TEAM_NAME = "GAURAV";
	private static final String PASS = "GAURAV11";
	private static final String ERROR = "Error";
	private static final String OWN_KEY = "OwnKey";
	private static final String OPPONENT_KEY = "OpponentKey";
	private static final String SELF_KEY = "selfkey";
	private static final String SUCCESS = "Successfully stored on Server";
	private static final String NETSUCCESS = "NetSuccess";
	private static final String NETNOTLOGGED = "NetNotLogged";
	private static final String NETFAILED = "NetFailed";
	public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String AVAILABLE_USER = "available";
    public static final String API_KEY = "AIzaSyAOUeOoOx5haA6Dzw-wc8yoao718axP63w";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String GSM_URL = "https://android.googleapis.com/gcm/send";
    public static final String DB_INTENT = "edu.neu.madcourse.gauravrane.broadcast.PLAYER_ONE";
    public static final String APP_ACTIVE = "isAppActive";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String BUNDLE_DATA = "BundleData";
    private static final String FULL_CONNECTION = "FullConnection";
    public static final String NOTIFICATION_SET = "notificationSet";
    private static final String NOTIFICATION_BUNDLE = "notificationBundle";
    
    
    private TextView mTextView;
    private EditText enteredNameText;
    private EditText sendMsgToOpponent;
	private Button startCommunicationButton;
	private Button sendTextToOtherDevice;
	private Button backButton;

	
	public BroadcastReceiver receiver;
	public boolean isNetFailing = false;
	public boolean noNetAvailable = false;
	public boolean isAllSet = false;

    /*
     * Project Id Google API Console
     */ 
    String SENDER_ID = "386713966593";

    static final String TAG = "GCMMain";

    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;

    String regid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.communication_start_activity);
        
        mTextView = (TextView) findViewById(R.id.communication_start_text_view);
        enteredNameText = (EditText) findViewById(R.id.commnunication_enter_name_edit_text);
        startCommunicationButton = (Button) findViewById(R.id.communication_send_name_button);
        
        sendTextToOtherDevice = (Button) findViewById(R.id.communication_start_button);
        backButton = (Button) findViewById(R.id.communication_back_button);
        sendMsgToOpponent = (EditText)findViewById(R.id.commnunication_ping_edit_text);
        
        context = getApplicationContext();
        
        receiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
                if (action.equals(DB_INTENT)) {
                	  Log.d(TAG, "Inside Register method of DB_INTENT");
                	  updateOpponentKeyOnRegistration(intent);
                } 
				
			}
    	};
        
    }
    
    @Override
    public void onStart(){
    	super.onStart();
		new AsyncTask<String, Integer,String>(){
			
			@Override
			protected String doInBackground(String... params) {			
				String result = null;
				if(isNetworkAvailable(context)){						
					try {
		                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
		                urlc.setRequestProperty("User-Agent", "Test");
		                urlc.setRequestProperty("Connection", "close");
		                urlc.setConnectTimeout(1500); 
		                urlc.connect();
		                if(urlc.getResponseCode() == 200){
		                	result = NETSUCCESS;
		                }else{
		                	result = NETNOTLOGGED;
		                }
		            } catch (IOException e) {		    
		                result = NETNOTLOGGED;
		            }
				}else{
					result = NETFAILED;
				}
				return result;
			}
			@Override
			protected void onPostExecute(String result) {
				if (result.equals(NETNOTLOGGED)) {
					mTextView.setText("Network connected but not logged in...");
				}else if(result.equals(NETFAILED)){
					mTextView.setText("Failed to Connect the user...");
				}
			}   
		}.execute("");	
    }
    
    protected void updateOpponentKeyOnRegistration(Intent intent) {
    	Bundle intentBundle = intent.getExtras();
    	Log.d("UpdateOpponentKeyOnRegistration", "Bundle data from notification intent " + intentBundle.getString(BUNDLE_DATA));
        String dataString = (String) intentBundle.get(BUNDLE_DATA);
        updateOpponentKeyStringOnRegister(dataString);
	}

	private void updateOpponentKeyStringOnRegister(String dataString) {
		HashMap<String, String> mapBundle = getMapFromBundle(dataString);
		if((mapBundle.get("msgtype")).equals("REGKEY")){
	        SharedPreferences prefs = getSharedPreferences(ComGSMMainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
	    	Editor editor = prefs.edit();
	    	editor.putString(OPPONENT_KEY, mapBundle.get(SELF_KEY));
	    	editor.putBoolean(FULL_CONNECTION, true);
	    	editor.commit();
	    	mTextView.setText("Opponent connected succesfully...");
		}else if((mapBundle.get("msgtype")).equals("SENDMSG")){
			mTextView.setText(mapBundle.get("message"));
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


	public void onResume(){
    	super.onResume();
    	checkPlayServices();
    	
    	if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);
            Log.d(TAG, "registration key already stored: " + regid);

            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.d(TAG, "No valid Google Play Services APK found.");
        }
    	
    	registerReceiver(receiver, new IntentFilter(DB_INTENT));
    	
    	SharedPreferences prefs = getSharedPreferences(ComGSMMainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    	Editor editor = prefs.edit();
    	editor.putBoolean(APP_ACTIVE, true);
    	editor.commit();
    	
    	boolean isNotificationSet = prefs.getBoolean(NOTIFICATION_SET, false);
    	Log.d("Notification", "isNotificationFlagSet " + isNotificationSet);
    	if(isNotificationSet){
    		Log.d("Notification", "After notification is send to Player one...");
    		Intent currentIntent = getIntent();
    		Log.d("Notification", "currentIntent name: "+currentIntent.getStringExtra(BUNDLE_DATA));
    		String bundleString = prefs.getString(NOTIFICATION_BUNDLE, "");
    		updateOpponentKeyStringOnRegister(bundleString);
    		//updateOpponentKeyOnRegistration(currentIntent);
    		editor.putBoolean(NOTIFICATION_SET, false);
    		editor.commit();
    	}
    	
    	backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
    	
        startCommunicationButton.setOnClickListener(new OnClickListener(){	
			@Override
			public void onClick(View v) {
				SharedPreferences prefs = getSharedPreferences(ComGSMMainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
				boolean isConnectionFullyDone = prefs.getBoolean(FULL_CONNECTION, false);
				if(!isConnectionFullyDone){
					String userId = enteredNameText.getText().toString();
					if(!userId.isEmpty()){
						if(isNetworkAvailable(context)){
							new AsyncTask<String,Integer, String>(){
								
								@Override
								protected String doInBackground(String... params) {
									String execute_value = null;
									if(KeyValueAPI.isServerAvailable()){
										String putString = KeyValueAPI.put(TEAM_NAME, PASS, enteredNameText.getText().toString(), regid);
										if(putString.contains(ERROR)){
											execute_value = "Error in putting key pair value: " + putString;
										}else{
											execute_value = SUCCESS;
										}
									}else{
										execute_value = "Problem in server availability";
									}
									return execute_value;
								}
								
								@Override
								protected void onPostExecute(String result){
									super.onPostExecute(result);
									if(result.equals(SUCCESS)){
										//Log.d(TAG, "Stored name with reg key");
										checkWaitingListForUser();
									}else{
										mTextView.setText("No Network Available...");
									}
								}								
							}.execute("");
					    }else{
					    	mTextView.setText("No network available!");				
					    }
					}else{
						mTextView.setText("Enter a non empty string...");
					}
				}else{
					mTextView.setText("You are already connected to an opponent");
				}
			}
		});
        
        sendTextToOtherDevice.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(isNetworkAvailable(context)){
					String inmessageString = sendMsgToOpponent.getText().toString();
					if(!inmessageString.isEmpty()){
						SharedPreferences prefs = getSharedPreferences(ComGSMMainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
						String oppmsgkey = prefs.getString(OPPONENT_KEY, "empty");
						if(oppmsgkey.equals("empty")){
							mTextView.setText("Registration id not proper to send message...");
						}else{
							String inputDataString = "data.msgtype="+"SENDMSG"+"&data.message="+inmessageString+"&data.program="+"COMM";
							sendRegIdOrMessageToOpponent(oppmsgkey, inputDataString);
							mTextView.setText("Message sent to opponent...");
						}	
					}else{
						mTextView.setText("Enter a non-empty string...");
					}
				}else{
					mTextView.setText("Failure to connect to internet...");
				}
			}
		});
        
        
    }
    

	public void onPause(){
    	super.onPause();
    	SharedPreferences prefs = getSharedPreferences(ComGSMMainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    	Editor editor = prefs.edit();
    	editor.putBoolean(APP_ACTIVE, false);
    	editor.commit();
    	unregisterReceiver(receiver);
    }
    
    public void checkWaitingListForUser(){
    	if(isNetworkAvailable(context)){
	    	new AsyncTask<String,Integer, String>(){
	    		
	    		boolean isAvailable = false;
	    		
				@Override
				protected String doInBackground(String... params) {
					String execute_value = null;
					if(KeyValueAPI.isServerAvailable()){
						String putString = KeyValueAPI.get(TEAM_NAME, PASS, AVAILABLE_USER);
						if(putString.contains(ERROR)){
							execute_value = "Error in getting key pair value: " + putString;
						}else{
							execute_value = putString;
							Log.d(TAG, "waiting user " + putString);
							isAvailable = true;
						}
					}else{
						execute_value = "Error in server availability";
					}
					Log.d(TAG, "Available user status " + execute_value);
					return execute_value;
				}
				
				@Override
				protected void onPostExecute(String result){
					super.onPostExecute(result);
					if(isAvailable){
						if(!result.equals(regid)){
							Log.d(TAG, "Waiting user exists");
							storeOtherRegistrationId(context, false, result);
							storeOtherRegistrationId(context, true, regid);
							acceptRequestForCom(result);
						}else{
							mTextView.setText("Already waiting for someone...");
						}
					}else{
						Log.d(TAG, "Waiting user doesnt exists");
						storeOtherRegistrationId(context, true, regid);
						keepWaitingForNewUser();
					}
				}
			}.execute("");
    	}else{
    		mTextView.setText("No network available!");
    	}
    }
    
    
    public void acceptRequestForCom(String msg){
    	if(isNetworkAvailable(context)){
	    	new AsyncTask<String,Integer, String>(){
	    		
	    		private String param = null;
	   
				@Override
				protected String doInBackground(String... params) {
					param = params[0];
					String execute_value = null;
					if(KeyValueAPI.isServerAvailable()){
						String putString = KeyValueAPI.clearKey(TEAM_NAME, PASS, AVAILABLE_USER);
						if(putString.contains(ERROR)){
							execute_value = "Error in getting key pair value: " + putString;
						}else{
							execute_value = SUCCESS;
						}
					}else{
						execute_value = "Error in server availability";
					}
					//Log.d(TAG, "Accepted request for game " + execute_value);
					return execute_value;
				}
				
				@Override
				protected void onPostExecute(String result){
					super.onPostExecute(result);
					if(result.equals(SUCCESS)){
						mTextView.setText("Successfully connected with opponent");
						SharedPreferences prefs = getSharedPreferences(ComGSMMainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
				    	Editor editor = prefs.edit();
				    	editor.putBoolean(FULL_CONNECTION, true);
				    	editor.commit();
						//Log.d(TAG, "Opponent key id: " + param);
				    	String inputDataString = "data.selfkey="+regid+"&data.msgtype="+"REGKEY"+"&data.program="+"COMM";
						sendRegIdOrMessageToOpponent(param,inputDataString);
					}else{
						mTextView.setText("Something went haywire!!!");
					}
				}
				
			}.execute(msg);
    	}else{
    		mTextView.setText("No network available!");
    	}
    }
    
    public void keepWaitingForNewUser(){
    	if(isNetworkAvailable(context)){
	    	new AsyncTask<String,Integer, String>(){
				@Override
				protected String doInBackground(String... params) {
					String execute_value = null;
					if(KeyValueAPI.isServerAvailable()){
						String putString = KeyValueAPI.put(TEAM_NAME, PASS, AVAILABLE_USER,regid);
						if(putString.contains(ERROR)){
							execute_value = "Error in getting key pair value: " + putString;
						}else{
							execute_value = SUCCESS;
						}
					}else{
						execute_value = "Error in server availability";
					}
					//Log.d(TAG, "Keep waiting for user " + execute_value);
					return execute_value;
				}
				
				@Override
				protected void onPostExecute(String result){
					super.onPostExecute(result);
					if(result.equals(SUCCESS)){
						mTextView.setText("No one present... waiting for someone to join");
					}else{
						mTextView.setText("Something went haywire...");
					}
				}
				
			}.execute("");
    	}else{
    		mTextView.setText("No network available!");
    	}
    }
    
    private void sendRegIdOrMessageToOpponent(String msg,String inputDataString){
    	if(!msg.equals(null)){
    		if(isNetworkAvailable(context)){
	    		new AsyncTask<String, Integer, String>(){
	    			
	    			protected String doInBackground(String... params){
	    				String toMessage = params[0]; 
	    				String inputData = params[1];
	    				//Log.d(TAG, "Opponent key id: " + message);
				    	DataOutputStream out = null;
				    	URL url = null;
						try {
							url = new URL(GSM_URL);
						} catch (MalformedURLException e1) {
							e1.printStackTrace();
						}
				    	HttpURLConnection connection = null;
						try {
							connection = (HttpURLConnection) url.openConnection();
							connection.setRequestMethod("POST");
							connection.setRequestProperty("Authorization", "key="+API_KEY);
							connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
							
							String urlParams = inputData+"&registration_id="+toMessage;
							//Log.d(TAG, "URL Params: " + urlParams);
							connection.setDoOutput(true);
							out = new DataOutputStream(connection.getOutputStream());
							out.writeBytes(urlParams);
							
							//Log.d(TAG,"connection response message " + connection.getResponseMessage());
							//Log.d(TAG,"connection response code " + Integer.toString(connection.getResponseCode()));
							
							BufferedReader in = new BufferedReader(
							        new InputStreamReader(connection.getInputStream()));
							String inputLine;
							StringBuffer response = new StringBuffer();
					 
							while ((inputLine = in.readLine()) != null) {
								response.append(inputLine);
							}
							
							in.close();
							
							//Log.d(TAG, "Actual Respose data: " + response.toString());
							
							out.flush();
							out.close();
							
							if (connection != null) {
				                connection.disconnect();
				            }
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						return null;
	    			}
	    		}.execute(msg,inputDataString);
    		}else{    			
	    		mTextView.setText("No network available!");		    
    		}
    	}else{
    		mTextView.setText("Not valid message");
    	}
    	
    }
    
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.d(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
    
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.d(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.d(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }
    
    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(ComGSMMainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
    
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
    
    
    private void registerInBackground() {
    	if(isNetworkAvailable(context)){
    		Log.d("NOINTERNET", "hey inside registerInBackground");
	        new AsyncTask<String,Integer,String>() {
	            @Override
	            protected String doInBackground(String... params) {
	                String msg = "";
	                try {
	                    if (gcm == null) {
	                        gcm = GoogleCloudMessaging.getInstance(context);
	                    }
	                    regid = gcm.register(SENDER_ID);
	                    //msg = "Device registered, registration ID=" + regid;
	                    msg="Device registered for the first time...";
	                    
	                    storeRegistrationId(context, regid);
	                } catch (IOException ex) {
	                    msg = "Error :" + ex.getMessage();
	                    // If there is an error, don't just keep trying to register.
	                    // Require the user to click a button again, or perform
	                    // exponential back-off.
	                }
	                return msg;
	            }
	
	            @Override
	            protected void onPostExecute(String msg) {
	                mTextView.append(msg + "\n");
	            }
	            
	        }.execute(null, null, null);
    	}else{
    		mTextView.setText("No network available!");
    	}
    }
    
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.d(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
    
    private void storeOtherRegistrationId(Context context, Boolean isDeviceOne, String regID){
    	final SharedPreferences prefs = getGCMPreferences(context);
    	 SharedPreferences.Editor editor = prefs.edit();
    	 if(isDeviceOne){
    		 Log.d(TAG, "Self Registration " + regID);
    		 editor.putString(OWN_KEY, regID);
    	 }else{
    		 Log.d(TAG, "Opponent Registration " + regID);
    		 editor.putString(OPPONENT_KEY, regID);
    	 }
         editor.commit();
    }	
    
    protected static boolean isNetworkAvailable(Context context) 
    {
        ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        android.net.NetworkInfo mobile = connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return wifi.isConnected() || mobile.isConnected();
    }
    
}

