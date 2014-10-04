package edu.neu.madcourse.gauravrane.twoplayer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import edu.neu.madcourse.gauravrane.R;

import android.R.integer;
import android.R.string;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TwoPlayerWordGame extends Activity {

	private TwoPlayerWordGameView twoPlayerWordGameView;
	private static final String TAG = "WordGame";
	private final char[] vowelsArray = { 'A', 'E', 'I', 'O', 'U' };
	private final char[] hardLetterArray = { 'V', 'K', 'J', 'X', 'Q', 'Z' };
	private final char[] simpleLetterArray = { 'T', 'N', 'S', 'H', 'R', 'D',
			'L', 'C', 'M', 'W', 'F', 'G', 'Y', 'P', 'B' };
	private final static String HARDCODED_STRING = "TCUKWROQWHAVDCIQPDIXCCUKFLEKWMOVYLITCUKWROQWHAVDCIQPDIXCCUKFLEKWM";
	private static final String GSM_URL = "https://android.googleapis.com/gcm/send";
	public static final String API_KEY = "AIzaSyAOUeOoOx5haA6Dzw-wc8yoao718axP63w";
	private ArrayList<Coordinate> selectedCoods = new ArrayList<Coordinate>();
	private static final String FINAL_OUTPUT = "finalOutput";
	protected ArrayList<Character> currentLettersArrayList = new ArrayList<Character>();
	protected ArrayList<Coordinate> matchLettersArrayList = new ArrayList<Coordinate>();
	protected StringBuffer selectedWordString = new StringBuffer();
	private BufferedReader bufferedReader = null;
	private int turn;
	private int myscore = 0;
	private int oppscore = 0;
	private InputStream is = null;
	private HashMap<Character, String> charMap = null;
	private ArrayList<String> loadList;
	private Button selectedWordButton;
	private Button hintButton;
	private Button clearWordButton;
	private Button myScoreButton;
	private Button oppScoreButton;
	private Button pauseButton;
	private Button targetButton;
	private Button quitWordGameButton;
	private TextView gameStatusTextView;
	private Activity context;
	private static final String BUNDLE_DATA = "BundleData";
	private static final String TARGET_SCORE = "targetScore";
	public static final String APP_ACTIVE = "isAppActive";
	public static final String MYPREFS = "MyPrefs";
	private static final String MYSCORE_KEY = "MyScoreValue";
	private static final String OPPONENT_KEY = "OpponentKey";
	private static final String MYTURN = "myturn";
	private static final String OPPSCORE_KEY = "OppScoreValue";
	public static final String TIMER_KEY = "TimerValue";
	public static final String LETTERS_KEY = "Letters";
	public static final String IS_NEW_GAME = "isNewGame";
	public static final String IS_GAME_OVER = "isGameOver";
	public static final String HINT_WORD = "hintWord";
	public static final String BDGAME_INTENT = "TWOPLAYER.GAME";
	private static final String FIRST_MOVE = "firstMove";
	
	public static final String INTENT_SET = "intentSet";
	private static final String INTENT_BUNDLE = "intentBundle";
	
	public boolean isNewGame;
	public boolean isMyTurn;
	public boolean isFirstMove = false;
	public String targetScoreString;
	public ArrayList<String> hintList = new ArrayList<String>();
	public boolean onPauseEvent;
	public String hintWord;
	public Long timerTime = 0L;
	public boolean isPausedPressed;
	
	public BroadcastReceiver receiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		twoPlayerWordGameView = new TwoPlayerWordGameView(this);
		setContentView(R.layout.twoplayer_custom_layout);
		twoPlayerWordGameView.requestFocus();
		this.context = this;
		
		Bundle extrasBundle = getIntent().getExtras();
		isNewGame = extrasBundle.getBoolean(IS_NEW_GAME);
		isMyTurn = extrasBundle.getBoolean(MYTURN);
		isFirstMove = extrasBundle.getBoolean(FIRST_MOVE);
		targetScoreString = extrasBundle.getString(TARGET_SCORE);
		setGameBoard();
		String gammeLettString = getGameLetterInString();
		
		SharedPreferences sharedPreferences = getGCMPreferences(context);
		Editor editor = sharedPreferences.edit();
		editor.putInt(MYSCORE_KEY, 0);
		editor.putInt(OPPSCORE_KEY, 0);
		editor.putBoolean(IS_NEW_GAME, false);
		editor.putBoolean(IS_GAME_OVER, false);
		editor.putString(TARGET_SCORE, targetScoreString);
		editor.putString(LETTERS_KEY, gammeLettString);
		editor.putBoolean(MYTURN, isMyTurn);
		editor.commit();
		
		charMap = new HashMap<Character, String>();
		int ascii_start = 97;
		
		for (int i = 1; i <= 26; i++) {
			String value = Character.toString((char) (ascii_start));
			charMap.put((char) (ascii_start), (value + "_list.txt"));
			ascii_start++;
		}
		gameStatusTextView = (TextView) findViewById(R.id.twoplayer_gameplaying_text_view);
		
		if(isMyTurn && isFirstMove){
			Log.d(TAG, "Inside First Move...send to check");
			String oppRegId = sharedPreferences.getString(OPPONENT_KEY, null);
			String boardLetterString = getGameLetterInString();
			Log.d("First Move", "My turn : " + isMyTurn + " isFirstMove : " + isFirstMove);
			Log.d("FIRST MOVE", "Initiated player will send: boardLetters: " + boardLetterString);
			Log.d("FIRST MOVE", "Initiated player will send: regId: " + oppRegId);
			sendWordGameMessage(oppRegId, 0, false, boardLetterString,targetScoreString);
			isFirstMove = false;
		}
		receiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
                if (action.equals(BDGAME_INTENT)) {
                	  Log.d(TAG, "Inside Register method of BDGAME_INTENT");
                	  getDataFromReceivingIntent(intent);
                } 
			}
    	};
	}
	
	protected void getDataFromReceivingIntent(Intent intent){
		Bundle intentBundle = intent.getExtras();
    	Log.d("UpdateOpponentKeyOnRegistration", "Bundle data from notification intent " + intentBundle.getString(BUNDLE_DATA));
        String dataString = (String) intentBundle.get(BUNDLE_DATA);
        updateGameBasedOnMessage(dataString);
	}
	
	public void updateGameBasedOnMessage(String message){		
		SharedPreferences sharedPreferences = getGCMPreferences(context);
		Editor editor = sharedPreferences.edit();
		HashMap<String, String> mapBundle = getMapFromBundle(message);
		Log.d("Inside receiver", "Hii");
		if((mapBundle.get("msgtype")).equals("GAME")){
			String oppScore = mapBundle.get("oppscore");
			Log.d("Loose situation ", "Game over oppscore: " + oppScore);
			String isGameOver = mapBundle.get("isGameOver");
			Log.d("Loose situation", "Game over flag: " + isGameOver);
			if(Boolean.parseBoolean(isGameOver)){
				myscore = 0;
				oppscore = 0;
				Log.d("Inside receiver", "Game over situation ");
				int myscr = sharedPreferences.getInt(MYSCORE_KEY, 0);
				showWinOrLooseScreen(myscr,Integer.parseInt(oppScore),"Oops..You lost!!!");
			}else{
				String tarString = mapBundle.get("target");
				Log.d("Inside receiver", "target score set: "+ tarString);
				targetButton.setText("Target Score: " +tarString);
				String boardLetters = mapBundle.get("boardletters");
				setOppScore(Integer.parseInt(oppScore));
				oppscore = Integer.parseInt(oppScore);
				setGameLetterFromString(boardLetters);
				Log.d("Inside receiver", "board letters set: "+ boardLetters);
				
				TwoPlayerWordGameView wordGameLatestView = (TwoPlayerWordGameView) findViewById(R.id.twoPlayerCustomWordGameView);
				if(isFirstMove){
					Log.d(TAG, "Inside my turn");
					wordGameLatestView.setVisibility(View.GONE);
					gameStatusTextView.setText("Opponent turn");
					isFirstMove = false;
					isMyTurn = false;
				}else{
					wordGameLatestView.setVisibility(View.VISIBLE);
					gameStatusTextView.setText("Your turn");
					isMyTurn = true;
				}
				wordGameLatestView.invalidateClearSelection();
				editor.putInt(OPPSCORE_KEY,Integer.parseInt(oppScore));
				editor.putString(TARGET_SCORE, tarString);
				editor.putString(LETTERS_KEY,boardLetters);
				editor.putBoolean(IS_GAME_OVER, false);
				editor.putBoolean(MYTURN, isMyTurn);
				editor.commit();
			}
		}else if((mapBundle.get("msgtype")).equals("GAMEQUIT")){
			Log.d("Inside quit mode", "quitting...");
			editor.putInt(MYSCORE_KEY, 0);
			editor.putInt(OPPSCORE_KEY,0);
			editor.putString(TARGET_SCORE, "0");
			editor.putBoolean(IS_GAME_OVER, true);
			editor.commit();
			myscore = 0;
			oppscore = 0;
			Toast.makeText(context, 
                    "Opponent quit..You won", Toast.LENGTH_LONG).show();
			Log.d("Inside quit button receiver: ", sharedPreferences.getBoolean(IS_GAME_OVER, false)+"");
			Intent quitIntent = new Intent(context,TwoPlayerWordGameStart.class);
			quitIntent.putExtra(MYTURN,false);
			quitIntent.putExtra(TARGET_SCORE, "100");
			quitIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(quitIntent);
		}
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
	
	@Override
	public void onBackPressed(){
		super.onBackPressed();
		SharedPreferences prefs = getGCMPreferences(context);
		boolean myTurn = prefs.getBoolean(MYTURN, false);
		Intent mainMenu = new Intent(this,TwoPlayerWordGameStart.class);
		mainMenu.putExtra(MYTURN, myTurn);
		mainMenu.putExtra(TARGET_SCORE, targetScoreString);
		mainMenu.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(mainMenu);
		
	}
	
	@Override
	public void onPause(){
		super.onPause();
		onPauseEvent = true;
		TwoPlayerMusic.stop(this);
		SharedPreferences prefs = getGCMPreferences(context);
		Boolean turBoolean = prefs.getBoolean(MYTURN, true);
		Editor editor = prefs.edit();
		editor.putInt(MYSCORE_KEY, myscore);
		editor.putInt(OPPSCORE_KEY,oppscore);
		editor.putBoolean(MYTURN, turBoolean);
		editor.putString(TARGET_SCORE, targetScoreString);
		editor.putString(LETTERS_KEY,getGameLetterInString());
		editor.commit();
		Log.d(TAG, "myscore: "+ myscore);
		Log.d(TAG, "oppscore: "+ oppscore);
		Log.d(TAG, "targetscore: "+ targetScoreString);
		Log.d(TAG, "letters key: "+ getGameLetterInString());
		Log.d(TAG, "MY turn value: "+ turBoolean);
		unregisterReceiver(receiver);
		
	}
	
	@Override
	public void onResume(){
		super.onResume();
		registerReceiver(receiver, new IntentFilter(BDGAME_INTENT));
    	
		myScoreButton = (Button) findViewById(R.id.twoPlayerYourScoreButton);
		oppScoreButton = (Button) findViewById(R.id.twoPlayerOppScoreButton);
		
		targetButton = (Button) findViewById(R.id.twoPlayerTargetScoreButton);
		
		selectedWordButton = (Button) findViewById(R.id.twoPlayerShowSelectedWord);
		selectedWordButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(!isPausedPressed){
					AssetManager assetManager = getAssets();
					String wordToFind = selectedWordButton.getText().toString().toLowerCase();
					if (wordToFind != "") {
						is = null;
						try {
							String filePath = "dictionary_text_files/"
									+ charMap.get(wordToFind.charAt(0));
							is = assetManager.open(filePath);
							bufferedReader = new BufferedReader(
									new InputStreamReader(is));
							loadList = new ArrayList<String>();
							String line;
							while ((line = bufferedReader.readLine()) != null) {
								loadList.add(line);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
	
						if (loadList.contains(wordToFind)) {
							SharedPreferences sharedPreferences = getGCMPreferences(context);
							TwoPlayerMusic.playSecond(context, R.raw.word_found);
							myscore += wordToFind.length();
							Log.i(TAG, "score in find: " +myscore);
							setMyScore(myscore);
							clearButtonText();
							
							if(myscore>=Integer.parseInt(targetScoreString)){
								String oppRegId = sharedPreferences.getString(OPPONENT_KEY, null);
								boolean isGameOver = true;
								String gameletters = sharedPreferences.getString(LETTERS_KEY, HARDCODED_STRING); 
								String targettString = sharedPreferences.getString(TARGET_SCORE, "26");
								int recentscore = myscore;
								Log.d("Inside win sits", "apna score jo bhejenge " + recentscore);
								sendWordGameMessage(oppRegId,recentscore,isGameOver,gameletters,targettString);
								
								showWinOrLooseScreen(myscore,oppscore,"Congrats, You won!!!");
								
								//Log.d("Win situation", "Msg Contents : "+ myscre +" "+ isGameOver);
								Editor editor = sharedPreferences.edit();
								editor.putInt(MYSCORE_KEY, 0);
								editor.putInt(OPPSCORE_KEY,0);
								editor.putString(TARGET_SCORE, "0");
								editor.putBoolean(IS_GAME_OVER, true);
								editor.commit();
								myscore = 0;
								oppscore = 0;
							}else{
								// Pass turn to other other player and block the turn for the current player
								// Also pass message to the other player via GCM
								
								for(int h = 0; h < selectedCoods.size(); h++) {
									matchLettersArrayList.add(selectedCoods.get(h));
								}
								clearSelectedCoodList();
								
								TwoPlayerWordGameView wordGameLatestView = (TwoPlayerWordGameView) findViewById(R.id.twoPlayerCustomWordGameView);
								wordGameLatestView.invalidateNewChars(matchLettersArrayList);
								matchLettersArrayList.clear();
								
								Editor editor = sharedPreferences.edit();
								editor.putInt(MYSCORE_KEY, myscore);
								editor.putInt(OPPSCORE_KEY,oppscore);
								editor.putString(TARGET_SCORE, targetScoreString);
								editor.putBoolean(MYTURN, false);
								editor.putString(LETTERS_KEY,getGameLetterInString());
								editor.putBoolean(IS_GAME_OVER, false);
								editor.commit();
								isMyTurn = false;
								
								String oppRegId = sharedPreferences.getString(OPPONENT_KEY, null);
								int myscre = sharedPreferences.getInt(MYSCORE_KEY, 0);
								boolean isGameOver = sharedPreferences.getBoolean(IS_GAME_OVER, false);
								String gameletters = sharedPreferences.getString(LETTERS_KEY, HARDCODED_STRING); 
								String targettString = sharedPreferences.getString(TARGET_SCORE, "26");
								sendWordGameMessage(oppRegId,myscre,isGameOver,gameletters,targettString);
								Log.d("Normal Game Msg", "Msg Contents : "+ myscre +" "+ isGameOver
										+ " " + gameletters +" "+ targettString);
								
								if(!isMyTurn){
									Log.d(TAG, "Inside my turn");
									wordGameLatestView.setVisibility(View.GONE);
									gameStatusTextView.setText("Opponent turn");
								}else{
									wordGameLatestView.setVisibility(View.VISIBLE);
									gameStatusTextView.setText("Play your turn");
								}
							}
						}
						loadList.clear();
					}
				}
			}
		});
		
		quitWordGameButton = (Button) findViewById(R.id.quitTwoPlayerWordGame);
		quitWordGameButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TwoPlayerWordGame.this);
				 
				// set title
				alertDialogBuilder.setTitle("Are you sure you want to quit?");
	 
				// set dialog message
				alertDialogBuilder
					.setMessage("Click Yes to forfeit or No to play!!!")
					.setCancelable(false)
					.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							SharedPreferences sharedPreferences = getGCMPreferences(context);
							Editor editor = sharedPreferences.edit();
							editor.putInt(MYSCORE_KEY, 0);
							editor.putInt(OPPSCORE_KEY,0);
							editor.putString(TARGET_SCORE, "0");
							editor.putBoolean(IS_GAME_OVER, true);
							editor.commit();
							myscore = 0;
							oppscore = 0;
							
							String oppRegId = sharedPreferences.getString(OPPONENT_KEY, null);
							Log.d("Quit game", "Opp reg id: " + oppRegId);
							sendQuitWordGameMessage(oppRegId,0,true,"ABCD","122");
							
							Log.d("Inside quit button: yes", sharedPreferences.getBoolean(IS_GAME_OVER, false)+"");
							Intent quitIntent = new Intent(context,TwoPlayerWordGameStart.class);
							quitIntent.putExtra(MYTURN,false);
							quitIntent.putExtra(TARGET_SCORE, "100");
							quitIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
							startActivity(quitIntent);
						}
					  })
					.setNegativeButton("No",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							dialog.cancel();
						}
					});
	 
					// create alert dialog
					AlertDialog alertDialog = alertDialogBuilder.create();
	 
					// show it
					alertDialog.show();
				
			}
		});
		
		clearWordButton = (Button) findViewById(R.id.twoPlayerClearSelectedWord);
		clearWordButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				selectedCoods.clear();
				TwoPlayerWordGameView wordGameLatestView = (TwoPlayerWordGameView) findViewById(R.id.twoPlayerCustomWordGameView);
				wordGameLatestView.invalidateClearSelection();
				clearButtonText();
			}
		});
		
		pauseButton = (Button)findViewById(R.id.pauseTwoPlayerWordGame);
		pauseButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent pauseIntent = new Intent(context,TwoPlayerWordGamePaused.class);
				startActivity(pauseIntent);
			}
		});
		SharedPreferences prefs = getGCMPreferences(context);
		Editor editor = prefs.edit();
    	editor.putBoolean(APP_ACTIVE, true);
    	editor.commit();
		TwoPlayerMusic.play(this, R.raw.game_music);
		
		// update own score from shared preference
		int myResumedScore = prefs.getInt(MYSCORE_KEY,0);
		Log.i(TAG,"score on resume: " +myResumedScore);
		int oppResumedScore = prefs.getInt(OPPSCORE_KEY,0);
		Log.i(TAG,"score on resume: " +oppResumedScore);
		myscore = myResumedScore;
		oppscore = oppResumedScore; 
		setMyScore(myscore);
		setOppScore(oppscore);
		String targettScore = prefs.getString(TARGET_SCORE, "18");
		targetScoreString = targettScore;
		targetButton.setText("Target Score: "+ targetScoreString);
		String savedChars = prefs.getString(LETTERS_KEY, HARDCODED_STRING);
		setGameLetterFromString(savedChars);
		
		boolean isMyTurn = prefs.getBoolean(MYTURN, false);
		Log.d(TAG, "Bhai mai turn kya hai " + isMyTurn);
		TwoPlayerWordGameView wordGameLatestView = (TwoPlayerWordGameView) findViewById(R.id.twoPlayerCustomWordGameView);
		if(!isMyTurn){
			Log.d(TAG, "Inside my turn");
			wordGameLatestView.setVisibility(View.GONE);
			gameStatusTextView.setText("Opponent turn");
		}
		else{
			wordGameLatestView.setVisibility(View.VISIBLE);
			gameStatusTextView.setText("Your turn");
		}
		
		
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		SharedPreferences prefs = getGCMPreferences(context);
		Editor editor = prefs.edit();
		editor.putBoolean(IS_GAME_OVER, true);
		editor.commit();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	      super.onCreateOptionsMenu(menu);
	      MenuInflater inflater = getMenuInflater();
	      inflater.inflate(R.menu.word_game_menu, menu);
	      return true;
	 }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	      switch (item.getItemId()) {
	      case R.id.word_game_setting:
	         startActivity(new Intent(this, TwoPlayerPrefs.class));
	         return true;
	      }
	   return false;
	}

	
	public char getCharForTile(int turn) {
		int randomLetterInt;
		char randomletter = 0;
		if (turn == 0 || turn == 1 || turn == 2) {
			randomLetterInt = new Random().nextInt(simpleLetterArray.length);
			randomletter = simpleLetterArray[randomLetterInt];
		} else if (turn == 3) {
			randomLetterInt = new Random().nextInt(vowelsArray.length);
			randomletter = vowelsArray[randomLetterInt];
		} else if (turn == 4) {
			randomLetterInt = new Random().nextInt(hardLetterArray.length);
			randomletter = hardLetterArray[randomLetterInt];
		}
		return randomletter;
	}
	
	public void shuffleGameBoard(){
		Collections.shuffle(currentLettersArrayList);
	}
	
	public void clearSelectedCoodList(){
		selectedCoods.clear();
	}
	

	public void setMyScore(int num){
		myScoreButton.setText("My Score: " + num);
		Log.i(TAG,"my score: " + num);
	}
	
	public void setOppScore(int num){
		oppScoreButton.setText("Opp Score: " + num);
		Log.i(TAG,"Opp score: " + num);
	}
	
	public boolean isPauseButtonPressed(){
		return isPausedPressed;
	}
	
	public void setGameBoard() {
		if(isNewGame){
			char[] letters;
			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					if (turn >= 5) {
						turn = 0;
					}
					char currLetter = getCharForTile(turn);
					currentLettersArrayList.add(new Character(currLetter));
					turn++;
				}
			}
		}else{
			SharedPreferences sharedPreferences = getGCMPreferences(context);
			String savedChars = sharedPreferences.getString(LETTERS_KEY, HARDCODED_STRING);
			setGameLetterFromString(savedChars);
		}
	}

	public void addCood(int x, int y) {
		selectedCoods.add(new Coordinate(x, y));
	}

	public Coordinate getCood(int k) {
		return selectedCoods.get(k);
	}

	public ArrayList<Coordinate> getCoodsArrayList() {
		return selectedCoods;
	}

	public ArrayList<Coordinate> getMatchArrayList() {
		return matchLettersArrayList;
	}

	public boolean isMatchArrayEmpty() {
		if (matchLettersArrayList.size() == 0) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isCoodInMatch(int i, int j) {
		Coordinate checkCood = new Coordinate(i, j);
		for (int p = 0; p < matchLettersArrayList.size(); p++) {
			Coordinate getCood = matchLettersArrayList.get(p);
			if (getCood.equals(checkCood)) {
				return true;

			}
		}
		return false;
	}

	public boolean isCoodInList(int i, int j) {
		Coordinate checkCood = new Coordinate(i, j);
		for (int p = 0; p < selectedCoods.size(); p++) {
			Coordinate getCood = selectedCoods.get(p);
			if (getCood.equals(checkCood)) {
				return true;
			}
		}
		return false;
	}

	public int getCoodListLength() {
		return selectedCoods.size();
	}

	public void setButtonText(int selX, int selY) {
		int pos = selY * 8 + selX;
		selectedWordString.append(getGameBoardChars(pos));
		selectedWordButton.setText(selectedWordString);
	}

	public void clearButtonText() {
		selectedWordButton.setText(selectedWordString.replace(0,
				selectedWordString.length(), ""));
	}

	public ArrayList<Character> getGameBoardLetters() {
		return currentLettersArrayList;
	}
	
	public String getGameLetterInString(){
		StringBuffer currentLettersBuffer = new StringBuffer();
		for(Character ch :currentLettersArrayList){
			currentLettersBuffer.append(ch);
		}
		return currentLettersBuffer.toString();
	}
	
	public void setGameLetterFromString(String savedChars){
		int letterLength = savedChars.length();
		currentLettersArrayList.clear();
		for(int i=0;i<letterLength;i++){
			currentLettersArrayList.add(savedChars.charAt(i));
		}
	}

	public char getRectLetter(int x, int y) {
		return getGameBoardLetters().get(y * 8 + x);
	}

	public void setRectLetter(int x, int y, char ch) {
		getGameBoardLetters().set((y * 8 + x), ch);
	}

	public String getGameBoardChars(int pos) {
		char letter = currentLettersArrayList.get(pos);
		return Character.toString(letter);
	}
	
	public void setGameStatusTextButton(String text){
		gameStatusTextView.setText(text);
	}

	public class Coordinate {
		public int x;
		public int y;

		public Coordinate(int newX, int newY) {
			x = newX;
			y = newY;
		}

		public boolean equals(Coordinate other) {
			if (x == other.x && y == other.y) {
				return true;
			}
			return false;
		}

		@Override
		public String toString() {
			return "Coordinate: [" + x + "," + y + "]";
		}
	}
	
	public void sendWordGameMessage(String oppRegId,int myscre, boolean isGameOver, String letters,String target){
		if(isNetworkAvailable(context)){
			if(oppRegId.equals("empty")){
				gameStatusTextView.setText("Bad signal");
			}else{
				
				String inputDataString = "data.msgtype="+"GAME"+"&data.oppscore="+myscre
						+"&data.isGameOver="+isGameOver+"&data.boardletters="+letters+"&data.target="+target+"&data.program="+"TWOP";
				
				sendGameRequestOrMessageToOpponent(oppRegId, inputDataString);
				gameStatusTextView.setText("Opponent turn");
			}
		}else{
			gameStatusTextView.setText("Internet failure");
		}
	}
	public void sendQuitWordGameMessage(String oppRegId,int myscre, boolean isGameOver, String letters,String target){
		if(isNetworkAvailable(context)){
			if(oppRegId.equals("empty")){
				gameStatusTextView.setText("Bad signal");
			}else{
				
				String inputDataString = "data.msgtype="+"GAMEQUIT"+"&data.oppscore="+myscre
						+"&data.isGameOver="+isGameOver+"&data.boardletters="+letters+"&data.target="+target+"&data.program="+"TWOP";
				
				sendGameRequestOrMessageToOpponent(oppRegId, inputDataString);
				gameStatusTextView.setText("You loose");
			}
		}else{
			gameStatusTextView.setText("Internet failure");
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
	    		gameStatusTextView.setText("No network");		    
    		}
    	}else{
    		gameStatusTextView.setText("No valid user");
    	}
    	
    }
	
	protected static boolean isNetworkAvailable(Context context) 
    {
        ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        android.net.NetworkInfo mobile = connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return wifi.isConnected() || mobile.isConnected();
    }
	
	public void showWinOrLooseScreen(int myscore,int oppscore,String msg){
		Intent gameFinishIntent = new Intent(context,TwoPlayerWordGameFinish.class);
		gameFinishIntent.putExtra(MYSCORE_KEY, myscore);
		gameFinishIntent.putExtra(OPPSCORE_KEY, oppscore);
		gameFinishIntent.putExtra(FINAL_OUTPUT, msg);
		startActivity(gameFinishIntent);
		finish();	
	}
	
	
	private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(TwoPlayerNewGameOption.class.getSimpleName(),
                Context.MODE_MULTI_PROCESS);
    }
	
	

}
