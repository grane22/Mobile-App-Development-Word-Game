package edu.neu.madcourse.gauravrane.twoplayer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.drive.internal.e;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import edu.neu.madcourse.gauravrane.R;
import edu.neu.madcourse.gauravrane.twoplayer.ShakeDetector.OnShakeListener;
import edu.neu.madcourse.gauravrane.wordgame.WordGame;
import edu.neu.mhealth.api.KeyValueAPI;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class TwoPlayerNewGameOption extends Activity implements OnClickListener{
	
	private static final String TEAM_NAME = "GAURAV";
	private static final String PASS = "GAURAV11";
	
	private static final String NETSUCCESS = "NetSuccess";
	private static final String NETNOTLOGGED = "NetNotLogged";
	private static final String NETFAILED = "NetFailed";
	
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private static final String FULL_CONNECTION = "FullConnection";
	private static final String GSM_URL = "https://android.googleapis.com/gcm/send";
	public static final String API_KEY = "AIzaSyAOUeOoOx5haA6Dzw-wc8yoao718axP63w";
	public static final String BDGAMEREQ_INTENT = "TWOPLAYER.GAMEREQ";
	
	public static final String APP_ACTIVE = "isAppActive";
	private static final String IS_GAME_OVER = "isGameOver";
	public static final String USER_NAME = "userName";
	public static final String IS_NEW_GAME = "isNewGame";
	private static final String OPPONENT_KEY = "OpponentKey";
	private static final String OPPONENT_NAME = "OpponentName";
	private static final String FIRST_MOVE = "firstMove";
	private static final String SELF_KEY = "fromUserRegId";
	private static final String SELF_NAME = "fromUserName";
	private static final String BUNDLE_DATA = "BundleData";
	private static final String TARGET_SCORE = "targetScore";
	public static final String NOTIFICATION_SET = "notificationSet";
	private static final String NOTIFICATION_BUNDLE = "notificationBundle";
	private static final String MYTURN = "myturn";
	private static final String GAME_USER="game_user";
	private static final String GAME_OPPONENT="game_opponent";
	
	public static final String AVAILABLE_USER = "availUsers";
	public static final String COMMA = ",";
	
	private static final String ERROR = "Error";
	private static final String ERROR_NO_KEY = "Error: No Such Key";
	private static final String ONLY_ONE_VALUE = "OnlyOneValue";
	private static final String SUCCESS = "Successfully stored on Server";
	
	/*
     * Project Id Google API Console
     */ 
    String SENDER_ID = "386713966593";
	
	static final String TAG = "GCMMain";
	
	GoogleCloudMessaging gcm;
	String oppontRegId;
	String targett;
	String regid;
	boolean isNewGame;
	public BroadcastReceiver receiver;
	
	 // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
	
	private TextView mTextView,targetScoreTextView,opponentNameTextView,userNameTextView,welcomeUserNameTextView,randomPlayerSearch;
	private EditText userNameEditText,opponentNameEditText,targetScoreEditText;
	private Button submitUserNameButton,opponentUserNameButton,newGameExitButton;
	Context context;

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twoplayer_newgameoption);
		context = getApplicationContext();
		
		Bundle extrasBundle = getIntent().getExtras();
		isNewGame = extrasBundle.getBoolean(IS_NEW_GAME);
		
		welcomeUserNameTextView = (TextView) findViewById(R.id.twoplayer_welcomeusername_textview);
		userNameTextView = (TextView) findViewById(R.id.twoplayer_enterusername_textview);
		userNameEditText = (EditText) findViewById(R.id.twoplayer_entername_edit_text);
		submitUserNameButton = (Button) findViewById(R.id.twoplayer_submitusername_button);
		submitUserNameButton.setOnClickListener(this);
		
		targetScoreTextView = (TextView) findViewById(R.id.twoplayer_targetscore_textview);
		targetScoreEditText = (EditText) findViewById(R.id.twoplayer_entertargetscore_edit_text);
		
		String targetsScore = targetScoreEditText.getText().toString();
		targett = targetsScore;
		
		opponentNameTextView = (TextView) findViewById(R.id.twoplayer_opponentname_textview);
		opponentNameEditText = (EditText) findViewById(R.id.twoplayer_enteropponentname_edit_text);
		opponentUserNameButton = (Button) findViewById(R.id.twoplayer_submitopponentname_button);
		opponentUserNameButton.setOnClickListener(this);
	
		randomPlayerSearch = (TextView)findViewById(R.id.twoplayer_randomPlayerSearchTextLabel);
		newGameExitButton = (Button) findViewById(R.id.twoplayer_newgameexit_button);
		newGameExitButton.setOnClickListener(this);
		
		mTextView = (TextView) findViewById(R.id.twoplayer_gameconnect_text_view);
		
		receiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
                if (action.equals(BDGAMEREQ_INTENT)) {
                	  Log.d(TAG, "Inside Register method of DB_INTENT");
                	  getDataFromReceivingIntent(intent);
                } 
			}
    	};
    	
    	// ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new OnShakeListener() {
 
            @Override
            public void onShake(int count) {
               selectRandomPlayer();
            }
        });
	}
	
	protected void getDataFromReceivingIntent(Intent intent){
		Bundle intentBundle = intent.getExtras();
    	Log.d("UpdateOpponentKeyOnRegistration", "Bundle data from notification intent " + intentBundle.getString(BUNDLE_DATA));
        String dataString = (String) intentBundle.get(BUNDLE_DATA);
        updateOpponentKeyStringOnRegister(dataString);
	}
	
	private void updateOpponentKeyStringOnRegister(String dataString) {
		Log.d(TAG, "Inside updateOpponentKeyStringOnRegister");
		HashMap<String, String> mapBundle = getMapFromBundle(dataString);
		final String oppKey = mapBundle.get(SELF_KEY);
		final String oppName = mapBundle.get(SELF_NAME);
		final String targetScore = mapBundle.get(TARGET_SCORE);
		Log.d(TAG, "Inside updateOpponentKeyStringOnRegister: OppName = " + oppName);
		if((mapBundle.get("msgtype")).equals("GAMEREQ")){
	    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
	 
				// set title
				alertDialogBuilder.setTitle("New Game Request from " + oppName + " for " + targetScore + " target score");
	 
				// set dialog message
				alertDialogBuilder
					.setMessage("Click Yes to accept or No to reject!!!")
					.setCancelable(false)
					.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							SharedPreferences prefs = getGCMPreferences(context);
					    	Editor editor = prefs.edit();
					    	editor.putString(OPPONENT_NAME, oppName);
					    	editor.putString(GAME_OPPONENT, oppName);
					    	editor.putString(OPPONENT_KEY, oppKey);
					    	editor.putBoolean(FULL_CONNECTION, true);
					    	editor.commit();
					    	sendRequestApprovalOrRejectBackToOpponent(oppKey,true);
					    	mTextView.setText("Accepted Opponent's request");
					    	startTwoPlayerWordGame(targetScore,false);
						}
					  })
					.setNegativeButton("No",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							dialog.cancel();
							sendRequestApprovalOrRejectBackToOpponent(oppKey,false);
						}
					});
	 
					// create alert dialog
					AlertDialog alertDialog = alertDialogBuilder.create();
	 
					// show it
					alertDialog.show();
					
		}else if((mapBundle.get("msgtype")).equals("GAMEREQACC")){
			mTextView.setText("Game request accepted");
			SharedPreferences prefs = getGCMPreferences(context);
	    	Editor editor = prefs.edit();
	    	editor.putString(OPPONENT_NAME, oppName);
	    	editor.putString(GAME_OPPONENT, oppName);
	    	editor.putString(OPPONENT_KEY, oppontRegId);
	    	editor.putBoolean(FULL_CONNECTION, true);
	    	editor.commit();
			startTwoPlayerWordGame(targett,true);
			
		}else if((mapBundle.get("msgtype")).equals("GAMEREQREJ")){
			SharedPreferences prefs = getGCMPreferences(context);
			Editor editor = prefs.edit();
			editor.remove(TARGET_SCORE);
	    	editor.commit();
			mTextView.setText("Game request rejected");
		}
	}
	
	public void startTwoPlayerWordGame(String target,boolean initiatingPlayer){
		Intent wordGameIntent = new Intent(this, TwoPlayerWordGame.class);
		wordGameIntent.putExtra(IS_NEW_GAME, isNewGame);
		wordGameIntent.putExtra(TARGET_SCORE, target);
		wordGameIntent.putExtra(FIRST_MOVE, true);
		if(initiatingPlayer){
			wordGameIntent.putExtra(MYTURN,true);
		}else{
			wordGameIntent.putExtra(MYTURN, false);
		}
		startActivity(wordGameIntent);
		SharedPreferences sharedPreferences = getGCMPreferences(context);
		Editor editor = sharedPreferences.edit();
		editor.putBoolean(IS_GAME_OVER, false);
		editor.commit();
	}
	
	public void onBackPressed(){
		super.onBackPressed();
		SharedPreferences prefs = getGCMPreferences(context);
		Editor editor = prefs.edit();
		editor.putBoolean(IS_GAME_OVER, true);
		editor.commit();
		Intent mainMenu = new Intent(this,TwoPlayerWordGameStart.class);
		mainMenu.putExtra(MYTURN, false);
		mainMenu.putExtra(TARGET_SCORE, "100");
		mainMenu.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(mainMenu);
		
	}
	
	public void sendRequestApprovalOrRejectBackToOpponent(String key, boolean isRequestAccept){
		SharedPreferences prefs = getGCMPreferences(context);
		String user = prefs.getString(USER_NAME, ERROR);
		String inmessageString = regid;
		String inputDataString = null;
		if(isRequestAccept){
			inputDataString = "data.msgtype="+"GAMEREQACC"+"&data.fromUserName="+user+"&data.fromUserRegId="+inmessageString+"&data.program="+"TWOP";
		}else{
			inputDataString = "data.msgtype="+"GAMEREQREJ"+"&data.fromUserName="+user+"&data.fromUserRegId="+inmessageString+"&data.program="+"TWOP";
		}
		sendGameRequestOrMessageToOpponent(key, inputDataString);
	}
	
	protected HashMap<String, String> getMapFromBundle(String bundleMsg){
		HashMap<String,String> mapBundle = new HashMap<String, String>();
		String subBundleMsg = bundleMsg.substring(8,bundleMsg.length()-2);
		//Log.d(TAG, "Inside getMapFromBundle: subbundle message " + subBundleMsg);
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
	
	protected void onStart(){
		super.onStart();
		// add user name to available user list
		SharedPreferences prefs = getGCMPreferences(context);
		if(!ERROR.equals(prefs.getString(USER_NAME, ERROR))){
			makeOtherUIElementsAvailable(prefs.getString(USER_NAME, ERROR));
		}
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
				}else if(result.equals(NETSUCCESS)){
					addUserNameToAvailableListOnServer();
				}
			}   
		}.execute("");	
	}
	
	// add user name to Available list on Server 
	public void addUserNameToAvailableListOnServer(){
		SharedPreferences prefs = getGCMPreferences(context);
		String userName = prefs.getString(USER_NAME, ERROR);
		if(!ERROR.contains(userName)){
			if(isNetworkAvailable(context)){
				new AsyncTask<String,Integer, String>(){
					String existingUsers = null,userNameString = null;
					@Override
					protected String doInBackground(String... params) {
						userNameString = params[0];
						String execute_value = null;
						if(KeyValueAPI.isServerAvailable()){
							String getString = KeyValueAPI.get(TEAM_NAME, PASS, AVAILABLE_USER);
							if(getString.equals(ERROR_NO_KEY)){
								execute_value = ERROR_NO_KEY;
								existingUsers = COMMA;
							}else if(!getString.contains(ERROR)){
								execute_value = SUCCESS;
								existingUsers = getString;
							}else{
								execute_value = getString;
							}
						}else{
							execute_value = "Problem in server availability";
						}
						return execute_value;
					}
					
					@Override
					protected void onPostExecute(String result){
						super.onPostExecute(result);
						if(result.equals(SUCCESS) || result.equals(ERROR_NO_KEY)){
							//Log.d(TAG, "Stored name with reg key");
							addUserNameToList(existingUsers,userNameString);
						}else{
							mTextView.setText("No Network Available...");
						}
					}								
				}.execute(userName);
			}
		}else{
			mTextView.setText("User name is not entered yet");
		}
	}
	
	public void addUserNameToList(String existingList, String userName){
		StringBuilder sBuilder = new StringBuilder();
		if(existingList.equals(COMMA)){
			sBuilder.append(userName);
		}else{
			sBuilder.append(existingList);
			sBuilder.append(",");
			sBuilder.append(userName);
		}
		addListToServer(sBuilder);
		

	}
	
	public void addListToServer(StringBuilder sBuilder){
		if(isNetworkAvailable(context)){
			new AsyncTask<String,Integer, String>(){
				@Override
				protected String doInBackground(String... params) {
					String availableUserList = params[0];
					String execute_value = null;
					if(KeyValueAPI.isServerAvailable()){
						String putString = KeyValueAPI.put(TEAM_NAME, PASS, AVAILABLE_USER,availableUserList);
						if(putString.contains(ERROR)){
							execute_value = putString;
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
						mTextView.setText("Added in available users list");
					}else{
						mTextView.setText("No Network Available...");
					}
				}								
			}.execute(sBuilder.toString());
		}
	}
	
	
	protected void onResume(){
		super.onResume();
		TwoPlayerMusic.play(this, R.raw.game_start);
		
		checkPlayServices();
		
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,SensorManager.SENSOR_DELAY_UI);
    	
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
    	
    	//TODO: change this to add the register 
    	registerReceiver(receiver, new IntentFilter(BDGAMEREQ_INTENT));
    	
    	SharedPreferences prefs = getGCMPreferences(context);
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
		
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.twoplayer_submitusername_button:
			updateSelfUserNameToServer();
			break;
		case R.id.twoplayer_submitopponentname_button:
			searchOpponentName();
			break;
		case R.id.twoplayer_newgameexit_button:
			finish();
			break;
		}
	}
	
	protected void onPause(){
		super.onPause(); 	
		mSensorManager.unregisterListener(mShakeDetector);
		TwoPlayerMusic.stop(this);
		SharedPreferences prefs = getGCMPreferences(context);
    	Editor editor = prefs.edit();
    	editor.putBoolean(APP_ACTIVE, false);
    	editor.commit();
    	unregisterReceiver(receiver);
	}
	
	protected void onDestroy(){
		super.onDestroy();
		//Remove username from available user list
		getAvailListToRemoveUserFromAvailList();
	}
	
	public void getAvailListToRemoveUserFromAvailList(){
		SharedPreferences prefs = getGCMPreferences(context);
		String userName = prefs.getString(USER_NAME, ERROR);
		if(!ERROR.contains(userName)){
			if(isNetworkAvailable(context)){
				new AsyncTask<String,Integer, String>(){
					String existingUsers = null,userNameString = null;
					@Override
					protected String doInBackground(String... params) {
						userNameString = params[0];
						String execute_value = null;
						if(KeyValueAPI.isServerAvailable()){
							String getString = KeyValueAPI.get(TEAM_NAME, PASS, AVAILABLE_USER);
							if(getString.equals(userNameString)){
								execute_value = ONLY_ONE_VALUE;
								existingUsers = getString;
							}else if(!getString.contains(ERROR)){
								execute_value = SUCCESS;
								existingUsers = getString;								
							}else{
								execute_value = getString;
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
							removeUserNameFromList(existingUsers,userNameString);
						}else if(result.equals(ONLY_ONE_VALUE)){
							removeOnlyUserNameFromList();
						}else{
							mTextView.setText("No Network Available...");
						}
					}								
				}.execute(userName);
			}
		}
	}
	
	public void removeOnlyUserNameFromList(){
		if(isNetworkAvailable(context)){
			new AsyncTask<String,Integer, String>(){
				@Override
				protected String doInBackground(String... params) {
					String execute_value = null;
					if(KeyValueAPI.isServerAvailable()){
						String removeKey = KeyValueAPI.clearKey(TEAM_NAME, PASS, AVAILABLE_USER);
						if(removeKey.contains(ERROR)){
							execute_value = removeKey;
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
						mTextView.setText("Removed user name from the available users list");
					}else{
						mTextView.setText("No Network Available...");
					}
				}								
			}.execute();
		}
	}
	
	public void removeUserNameFromList(String existingList, String userName){
		StringBuilder sb = new StringBuilder();
		String[] arr = existingList.split(",");
		for(int j=0;j<arr.length;j++){
			if(!arr[j].equals(userName)){
				sb.append(arr[j]);
				if(j != (arr.length-1)){
					sb.append(",");
				}
			}
		}
		
		if(isNetworkAvailable(context)){
			new AsyncTask<String,Integer, String>(){
				@Override
				protected String doInBackground(String... params) {
					String availableUserList = params[0];
					String execute_value = null;
					if(KeyValueAPI.isServerAvailable()){
						String putString = KeyValueAPI.put(TEAM_NAME, PASS, AVAILABLE_USER,availableUserList);
						if(putString.contains(ERROR)){
							execute_value = putString;
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
						mTextView.setText("Removed user name from the available users list");
					}else{
						mTextView.setText("No Network Available...");
					}
				}								
			}.execute(sb.toString());
		}
		
		
	}
	
	public void updateSelfUserNameToServer(){
		/* 
		 * TODO: Add the user name to mhealth server 		 
		 *       and add the user to available user list  
		 *       add user name in shared preference
		 * */
		
		SharedPreferences prefs = getGCMPreferences(context);
		boolean isConnectionFullyDone = prefs.getBoolean(FULL_CONNECTION, false);
		if(!isConnectionFullyDone){
			String userId = userNameEditText.getText().toString();
			if(!userId.isEmpty()){
				if(isNetworkAvailable(context)){
					new AsyncTask<String,Integer, String>(){
						@Override
						protected String doInBackground(String... params) {
							String execute_value = null;
							if(KeyValueAPI.isServerAvailable()){
								String getString = KeyValueAPI.get(TEAM_NAME, PASS, userNameEditText.getText().toString());
								if(ERROR_NO_KEY.equals(getString)){
									String putString = KeyValueAPI.put(TEAM_NAME, PASS, userNameEditText.getText().toString(), regid);
									if(putString.contains(ERROR)){
										execute_value = "Error in putting key pair value: " + putString;
									}else{
										execute_value = SUCCESS;
									}
								}else{
									execute_value = ERROR_NO_KEY;
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
								addUserNameToSharedPrefs(userNameEditText.getText().toString());
								makeOtherUIElementsAvailable(userNameEditText.getText().toString());
								addUserNameToAvailableListOnServer();
								mTextView.setText("UserName added succesfully");
							}else if(result.equals(ERROR_NO_KEY)){
								mTextView.setText("User Name already exits..Please enter new userName");
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
	
	public void addUserNameToSharedPrefs(String userName){
		SharedPreferences prefs = getGCMPreferences(context);
		Editor editor = prefs.edit();
    	editor.putString(USER_NAME, userName);
    	editor.putString(GAME_USER, userName);
    	editor.commit();
	}
	
	public void makeOtherUIElementsAvailable(String userName){
		
		welcomeUserNameTextView.setText("Welcome "+ userName);
		
		targetScoreTextView.setVisibility(View.VISIBLE);
		targetScoreEditText.setVisibility(View.VISIBLE);
		
		randomPlayerSearch.setVisibility(View.VISIBLE);
		opponentNameTextView.setVisibility(View.VISIBLE);
		opponentNameEditText.setVisibility(View.VISIBLE);
		opponentUserNameButton.setVisibility(View.VISIBLE);
		
		userNameTextView.setVisibility(View.GONE);
		userNameEditText.setVisibility(View.GONE);
		submitUserNameButton.setVisibility(View.GONE);
	}
	
	public void selectRandomPlayer(){
		/*
		 * TODO: Select the player from the available user
		 * 		 and gets its registration id
		 * 		 (and add in the playing users -- not here)
		 * 		 Send game request to that player
		 * */
		
		String targetScore = targetScoreEditText.getText().toString();
		Log.d(TAG, "Inside select random player: targetScore : " + targetScore);
		if(!targetScore.isEmpty()){
			targett = targetScore;
			SharedPreferences prefs = getGCMPreferences(context);
			Editor editor = prefs.edit();
	    	editor.putString(TARGET_SCORE, targetScore);
	    	editor.commit();
			String userName = prefs.getString(USER_NAME, ERROR);
			Log.d(TAG, "USER name inside find random player: " + userName);
			if(isNetworkAvailable(context)){
				new AsyncTask<String,Integer, String>(){
					String userNameString,availList;
					@Override
					protected String doInBackground(String... params) {
						userNameString = params[0];
						String execute_value = null;
						if(KeyValueAPI.isServerAvailable()){
							String getString = KeyValueAPI.get(TEAM_NAME, PASS, AVAILABLE_USER);
							if(getString.equals(ERROR_NO_KEY)){
								execute_value = ERROR_NO_KEY;
							}else if(getString.contains(ERROR)){
								execute_value = getString;
							}else{
								execute_value = SUCCESS;
								availList = getString;
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
							if(!availList.equals(userNameString)){
								//Log.d(TAG, "selectRandomPlayer method:own user name " + userNameString);
								getRegIDOfRandomPlayer(userNameString,availList);
							}else{
								mTextView.setText("No other user available");
							}
						}else{
							mTextView.setText("No Network Available...");
						}
					}								
				}.execute(userName);
			}
		}else{
			mTextView.setText("Please enter the target score...");
		}
	}
	
	public void getRegIDOfRandomPlayer(String userName, String availList){
		Log.d(TAG, "Inside getRegIdDOfRandomPlayer method");
		Log.d(TAG, "The availableList so far " + availList);
		StringBuilder sb = new StringBuilder();
		String[] arr = availList.split(",");
		String randomUser = userName;
		Log.d(TAG, "Inital assignment of random player " +randomUser);
		while(randomUser.equals(userName)){
			randomUser = arr[new Random().nextInt(arr.length)];
		}
		
		Log.d(TAG, "After assigning the random player " + randomUser);
		
		if(isNetworkAvailable(context)){
			new AsyncTask<String,Integer, String>(){
				String randomUserNameString;
				String randomRegIdString;
				@Override
				protected String doInBackground(String... params) {
					randomUserNameString = params[0];
					String execute_value = null;
					if(KeyValueAPI.isServerAvailable()){
						String getRegIDString = KeyValueAPI.get(TEAM_NAME, PASS, randomUserNameString);
						if(getRegIDString.contains(ERROR)){
							execute_value = getRegIDString;
						}else{
							execute_value = SUCCESS;
							randomRegIdString = getRegIDString;
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
						Log.d(TAG, "regId Of opponent" + randomRegIdString);
						connectToPlayer(randomRegIdString);
					}else{
						mTextView.setText("No Network Available...");
					}
				}								
			}.execute(randomUser);
		}
	}
	
	public void connectToPlayer(String oppRegId){
		if(isNetworkAvailable(context)){
			if(oppRegId.equals("empty")){
				mTextView.setText("Registration id not proper to send message...");
			}else{
				SharedPreferences prefs = getGCMPreferences(context);
				String user = prefs.getString(USER_NAME, ERROR);
				String inmessageString = regid;
				String tarScore = prefs.getString(TARGET_SCORE, "98");
				oppontRegId = oppRegId;
    	    	
    	    	Log.d("INSIDE CONNECTPLAYER", "OPP REGID " + oppontRegId);
				String inputDataString = "data.msgtype="+"GAMEREQ"+"&data.fromUserName="+user
						+"&data.fromUserRegId="+inmessageString+"&data.isnewgame="+isNewGame+
						"&data.targetScore="+tarScore+"&data.program="+"TWOP";
				Log.d(TAG, "From username: " + user);
				Log.d(TAG, "From userRegId: " + inmessageString);
				Log.d(TAG, "TargetScore: " + tarScore);
				
				sendGameRequestOrMessageToOpponent(oppRegId, inputDataString);
				mTextView.setText("Game Request sent to opponent...Waiting for its response");
			}
		}else{
			mTextView.setText("Failure to connect to internet...");
		}
	}
	
	private void sendGameRequestOrMessageToOpponent(String regId,String inputDataString){
    	if(!regId.equals(null)){
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
	    		}.execute(regId,inputDataString);
    		}else{    			
	    		mTextView.setText("No network available!");		    
    		}
    	}else{
    		mTextView.setText("Not valid user name to send request");
    	}
    	
    }
	
	public void searchOpponentName(){
		/*
		 * TODO: get the registration id from the mhealth server
		 * 		 and send game request to other player
		 * 		
		 * */
		String targetScore = targetScoreEditText.getText().toString();
		Log.d(TAG, "Inside select random player: targetScore : " + targetScore);
		String findUserName = opponentNameEditText.getText().toString();
		if(!targetScore.isEmpty()){
			if(!findUserName.isEmpty()){
				targett = targetScore;
				SharedPreferences prefs = getGCMPreferences(context);
				Editor editor = prefs.edit();
		    	editor.putString(TARGET_SCORE, targetScore);
		    	editor.commit();
				String userName = prefs.getString(USER_NAME, ERROR);
				Log.d(TAG, "USER name inside find random player: " + userName);
				if(isNetworkAvailable(context)){
					new AsyncTask<String,Integer, String>(){
						String userNameString,availList,findUserNameString;
						@Override
						protected String doInBackground(String... params) {
							userNameString = params[0];
							findUserNameString = params[1];
							String execute_value = null;
							if(KeyValueAPI.isServerAvailable()){
								String getString = KeyValueAPI.get(TEAM_NAME, PASS, AVAILABLE_USER);
								if(getString.equals(ERROR_NO_KEY)){
									execute_value = ERROR_NO_KEY;
								}else if(getString.contains(ERROR)){
									execute_value = getString;
								}else{
									execute_value = SUCCESS;
									availList = getString;
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
								if(!availList.equals(userNameString)){
									//Log.d(TAG, "selectRandomPlayer method:own user name " + userNameString);
									getRegIDOfFindPlayer(userNameString,findUserNameString,availList);
								}else{
									mTextView.setText("No other user available");
								}
							}else{
								mTextView.setText("No Network Available...");
							}
						}								
					}.execute(userName,findUserName);
				}
			}else{
				mTextView.setText("Please enter the user name to find...");
			}
		}else{
			mTextView.setText("Please enter the target score...");
		}
	}
	
	public void getRegIDOfFindPlayer(String userName,String findUserName,String availList){
		Log.d(TAG, "Inside getRegIdDOfFindPlayer method");
		Log.d(TAG, "The availableList so far " + availList);
		StringBuilder sb = new StringBuilder();
		String[] arr = availList.split(",");
		
		String matchedUser = null;
		boolean foundAMatch = false;
		for(int k=0;k<arr.length;k++){
			if(arr[k].equals(findUserName)){
				if(!arr[k].equals(userName)){
					matchedUser = arr[k];
					foundAMatch = true;
				}
			}
		}
		
		if(foundAMatch){
			if(isNetworkAvailable(context)){
				new AsyncTask<String,Integer, String>(){
					String randomUserNameString;
					String randomRegIdString;
					@Override
					protected String doInBackground(String... params) {
						randomUserNameString = params[0];
						String execute_value = null;
						if(KeyValueAPI.isServerAvailable()){
							String getRegIDString = KeyValueAPI.get(TEAM_NAME, PASS, randomUserNameString);
							if(getRegIDString.contains(ERROR)){
								execute_value = getRegIDString;
							}else{
								execute_value = SUCCESS;
								randomRegIdString = getRegIDString;
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
							Log.d(TAG, "regId Of opponent" + randomRegIdString);
							connectToPlayer(randomRegIdString);
						}else{
							mTextView.setText("No Network Available...");
						}
					}								
				}.execute(matchedUser);
			}
		}else{
			mTextView.setText("Entered user not in available list");
		}
		
		Log.d(TAG, "After finding the user name  " + matchedUser);
		
		
	}
	
	protected static boolean isNetworkAvailable(Context context) 
    {
        ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        android.net.NetworkInfo mobile = connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return wifi.isConnected() || mobile.isConnected();
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
        return getSharedPreferences(TwoPlayerNewGameOption.class.getSimpleName(),
                Context.MODE_MULTI_PROCESS);
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
}

