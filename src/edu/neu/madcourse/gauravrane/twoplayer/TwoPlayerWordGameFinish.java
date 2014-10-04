package edu.neu.madcourse.gauravrane.twoplayer;

import edu.neu.madcourse.gauravrane.R;
import edu.neu.madcourse.gauravrane.sudoku.Prefs;
import edu.neu.mhealth.api.KeyValueAPI;
import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.LoginFilter.UsernameFilterGeneric;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class TwoPlayerWordGameFinish extends Activity implements OnClickListener{
	
	private static final String MYSCORE_KEY = "MyScoreValue";
	private static final String OPPSCORE_KEY = "OppScoreValue";
	private static final String FINAL_OUTPUT = "finalOutput";
	private static final String TAG = "WordGame";
	public static final String MYPREFS = "MyPrefs";
	private static final String OPPONENT_KEY = "OpponentKey";
	private static final String IS_NEW_GAME = "isNewGame";
	public static final String IS_GAME_OVER = "isGameOver";
	private static final String TARGET_SCORE = "targetScore";
	private static final String MYTURN = "myturn";
	public static final String USER_NAME = "userName";
	
	private static final String TEAM_NAME = "GAURAV";
	private static final String PASS = "GAURAV11";
	protected static final String HIGHSCORE_LIST = "highScore";
	
	private static final String GAME_USER="game_user";
	private static final String GAME_OPPONENT="game_opponent";
	
	private static final String ERROR = "Error";
	private static final String ERROR_NO_KEY = "Error: No Such Key";
	private static final String SUCCESS = "Successfully stored on Server";
	
	public static final String COMMA = ",";
	
	String highScoreListString;
	

	
	Context context;
	int userScore=0,oppScore=0;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twoplayer_timeout_activity);
		
		context = getApplicationContext();
		SharedPreferences sharedPreferences = getGCMPreferences();
		Editor editor = sharedPreferences.edit();
		editor.putString(OPPONENT_KEY, "");
		editor.commit();
	
		TextView scoreDisplayTextView = (TextView)findViewById(R.id.twoplayer_score_name);
		Bundle extras = getIntent().getExtras();
		int myscoreValue = extras.getInt(MYSCORE_KEY);
		userScore = myscoreValue;
		int oppScoreValue = extras.getInt(OPPSCORE_KEY);
		oppScore = oppScoreValue;
		String messageString = extras.getString(FINAL_OUTPUT);
		scoreDisplayTextView.setText(messageString + " Your Score: " + myscoreValue + " Opponent score: " + oppScoreValue);
		//Log.i(TAG, "Score in Finish Class: " + scoreValue);
		
		View mainMenuButton = findViewById(R.id.twoplayer_main_menu_button);
		mainMenuButton.setOnClickListener(this);
		
		getHighScoreList();
		
	}
	
	public void getHighScoreList(){
		if(isNetworkAvailable(context)){
			new AsyncTask<String,Integer, String>(){
				@Override
				protected String doInBackground(String... params) {
					String execute_value = null;
					if(KeyValueAPI.isServerAvailable()){
						String getString = KeyValueAPI.get(TEAM_NAME, PASS, HIGHSCORE_LIST);
						if(getString.equals(ERROR_NO_KEY)){
							execute_value = ERROR_NO_KEY;
							highScoreListString = COMMA;
						}else if(!getString.contains(ERROR)){
							execute_value = SUCCESS;
							highScoreListString = getString;								
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
						updateHighScoreList();
					}else if(result.equals(ERROR_NO_KEY)){
						createNewHighScoreList();
					}else{
						Toast.makeText(context, 
			                    "Unable to update high score list!", Toast.LENGTH_LONG).show();
					}
				}								
			}.execute();
		}
	}
	
	public void updateHighScoreList(){
		StringBuilder stringBuilder = new StringBuilder();
		SharedPreferences prefs = getGCMPreferences();
		String user = prefs.getString(GAME_USER, "user1");
		String opp = prefs.getString(GAME_OPPONENT, "user2");
		
		int currScore=0;
		String currString;
		
		if(userScore>oppScore){
			currScore = userScore;
			currString = user;
		}else{
			currScore = oppScore;
			currString = opp;
		}
		
		String[] arr = highScoreListString.split(" ");
		
		for(int i=0;i<5;i++){
			if(i<arr.length){
				String[] elemArr = arr[i].split(",");
				int elemValue = Integer.parseInt(elemArr[1]);
				if(currScore>=elemValue){
					stringBuilder.append(currString);
					stringBuilder.append(",");
					stringBuilder.append(currScore);
					
					currScore = elemValue;
					currString = elemArr[0];
				}else{
					stringBuilder.append(elemArr[0]);
					stringBuilder.append(",");
					stringBuilder.append(elemArr[1]);
				}
				
				if(i!=4){
					stringBuilder.append(" ");
				}
			}
		}
		
		submitListToServer(stringBuilder.toString());
	}
	
	public void createNewHighScoreList(){
		SharedPreferences prefs = getGCMPreferences();
		String user = prefs.getString(GAME_USER, "user1");
		String opp = prefs.getString(GAME_OPPONENT, "user2");
		
		StringBuilder sBuilder = new StringBuilder();
		if(userScore > oppScore){
			sBuilder.append(user);
			sBuilder.append(",");
			sBuilder.append(userScore);
		}else{
			sBuilder.append(opp);
			sBuilder.append(",");
			sBuilder.append(oppScore);
		}
		
		sBuilder.append(" ");
		sBuilder.append("rahul");
		sBuilder.append(",");
		sBuilder.append(5);
		
		sBuilder.append(" ");
		sBuilder.append("gaurav");
		sBuilder.append(",");
		sBuilder.append(4);
		
		sBuilder.append(" ");
		sBuilder.append("ramesh");
		sBuilder.append(",");
		sBuilder.append(3);
		
		sBuilder.append(" ");
		sBuilder.append("rahul");
		sBuilder.append(",");
		sBuilder.append(3);
		
		
		submitListToServer(sBuilder.toString());
	}
	
	public void submitListToServer(String list){
		if(isNetworkAvailable(context)){
			new AsyncTask<String,Integer, String>(){
				@Override
				protected String doInBackground(String... params) {
					String availableHighScoreList = params[0];
					String execute_value = null;
					if(KeyValueAPI.isServerAvailable()){
						String putString = KeyValueAPI.put(TEAM_NAME, PASS, HIGHSCORE_LIST,availableHighScoreList);
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
						Toast.makeText(context, 
			                    "Updated high score list!", Toast.LENGTH_LONG).show();
					}else{
						Toast.makeText(context, 
			                    "Unable to update high score list!", Toast.LENGTH_LONG).show();
					}
				}								
			}.execute(list);
		}
	}

	protected static boolean isNetworkAvailable(Context context) 
    {
        ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        android.net.NetworkInfo mobile = connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return wifi.isConnected() || mobile.isConnected();
    }
	
	@Override
	public void onPause(){
		super.onPause();
		TwoPlayerMusic.stop(this);
		SharedPreferences sharedPreferences = getGCMPreferences();
		Editor editor = sharedPreferences.edit();
		editor.putBoolean(IS_GAME_OVER, true);
		editor.commit();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		TwoPlayerMusic.play(this,R.raw.word_game_over);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.twoplayer_main_menu_button:
			getToMainMenu();
			break;
		}
	}
		
		
		public void getToMainMenu(){
			/*finish();
			finish();
			finish();*/
			SharedPreferences sharedPreferences = getGCMPreferences();
			Editor editor = sharedPreferences.edit();
			editor.putBoolean(IS_GAME_OVER, true);
			editor.commit();
			
			Intent mainMenu = new Intent(this,TwoPlayerWordGameStart.class);
			mainMenu.putExtra(MYTURN, false);
			mainMenu.putExtra(TARGET_SCORE, "100");
			mainMenu.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(mainMenu);
			
		}
		
		private SharedPreferences getGCMPreferences() {
	        return getSharedPreferences(TwoPlayerNewGameOption.class.getSimpleName(),
	                Context.MODE_MULTI_PROCESS);
	    }
		
		
	}
