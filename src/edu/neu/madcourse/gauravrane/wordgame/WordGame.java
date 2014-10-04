package edu.neu.madcourse.gauravrane.wordgame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import edu.neu.madcourse.gauravrane.R;

import android.R.integer;
import android.R.string;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class WordGame extends Activity {

	private WordGameView wordGameView;
	private static final String TAG = "WordGame";
	private final char[] vowelsArray = { 'A', 'E', 'I', 'O', 'U' };
	private final char[] hardLetterArray = { 'V', 'K', 'J', 'X', 'Q', 'Z' };
	private final char[] simpleLetterArray = { 'T', 'N', 'S', 'H', 'R', 'D',
			'L', 'C', 'M', 'W', 'F', 'G', 'Y', 'P', 'B' };
	private final static String HARDCODED_STRING = "TCUKWROQWHAVDCIQPDIXCCUKFLEKWMOVYLITCUKWROQWHAVDCIQPDIXCCUKFLEKWM";
	private ArrayList<Coordinate> selectedCoods = new ArrayList<Coordinate>();
	protected ArrayList<Character> currentLettersArrayList = new ArrayList<Character>();
	protected ArrayList<Coordinate> matchLettersArrayList = new ArrayList<Coordinate>();
	protected StringBuffer selectedWordString = new StringBuffer();
	private BufferedReader bufferedReader = null;
	private int turn;
	private int score = 0;
	private InputStream is = null;
	private HashMap<Character, String> charMap = null;
	private ArrayList<String> loadList;
	private Button selectedWordButton;
	private Button hintButton;
	private Button clearWordButton;
	private Button scoreButton;
	private Button timerButton;
	private Button pauseButton;
	private WordGameCounter timer;
	private Activity context;
	public static final String MYPREFS = "MyPrefs";
	private static final String SCORE_KEY = "ScoreValue";
	public static final String TIMER_KEY = "TimerValue";
	public static final String LETTERS_KEY = "Letters";
	public static final String IS_NEW_GAME = "isNewGame";
	public static final String IS_GAME_OVER = "isGameOver";
	public static final String HINT_WORD = "hintWord";
	public boolean isNewGame;
	public ArrayList<String> hintList = new ArrayList<String>();
	public boolean onPauseEvent;
	public String hintWord;
	public Long timerTime = 0L;
	public boolean isPausedPressed;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		wordGameView = new WordGameView(this);
		setContentView(R.layout.wordgame_custom_layout);
		wordGameView.requestFocus();
		this.context = this;
		
		Bundle extrasBundle = getIntent().getExtras();
		isNewGame = extrasBundle.getBoolean(IS_NEW_GAME);
		setGameBoard();
		
		charMap = new HashMap<Character, String>();
		int ascii_start = 97;
		
		for (int i = 1; i <= 26; i++) {
			String value = Character.toString((char) (ascii_start));
			charMap.put((char) (ascii_start), (value + "_list.txt"));
			ascii_start++;
		}
		
		scoreButton = (Button) findViewById(R.id.totalScore);
		timerButton = (Button) findViewById(R.id.timerCount);
		
		selectedWordButton = (Button) findViewById(R.id.showSelectedWord);
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
							Music.playSecond(context, R.raw.word_found);
							score += wordToFind.length();
							Log.i(TAG, "score in find: " +score);
							setScore(score);
							clearButtonText();
							for(int h = 0; h < selectedCoods.size(); h++) {
								matchLettersArrayList.add(selectedCoods.get(h));
							}
							clearSelectedCoodList();
							
							WordGameView wordGameLatestView = (WordGameView) findViewById(R.id.customWordGameView);
							wordGameLatestView.invalidateNewChars(matchLettersArrayList);
							matchLettersArrayList.clear();
						}
						loadList.clear();
					}
				}
			}
		});
		
		hintButton = (Button) findViewById(R.id.hintButton);
		hintButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				WordGameView wordGameLatestView = (WordGameView) findViewById(R.id.customWordGameView);
				shuffleGameBoard();
				clearButtonText();
				clearSelectedCoodList();
				wordGameLatestView.invalidateClearSelection();
			}
		});
		
		clearWordButton = (Button) findViewById(R.id.clearSelectedWord);
		clearWordButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				selectedCoods.clear();
				WordGameView wordGameLatestView = (WordGameView) findViewById(R.id.customWordGameView);
				wordGameLatestView.invalidateClearSelection();
				clearButtonText();
			}
		});
		
		pauseButton = (Button)findViewById(R.id.pauseWordGame);
		pauseButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent pauseIntent = new Intent(context,WordGamePaused.class);
				startActivity(pauseIntent);
			}
		});
	}
	
	
	@Override
	public void onPause(){
		onPauseEvent = true;
		Music.stop(this);
		super.onPause();
		SharedPreferences sharedPreferences = getSharedPreferences(MYPREFS, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putInt(SCORE_KEY, score);
		editor.putLong(TIMER_KEY, timerTime);
		editor.putString(LETTERS_KEY,getGameLetterInString());
		editor.putBoolean(IS_GAME_OVER, false);
		editor.commit();
		timer.cancel();
		
	}
	
	@Override
	public void onResume(){
		super.onResume();
		Music.play(this, R.raw.game_music);
		if(isNewGame && !onPauseEvent){
			timer = new WordGameCounter(20000, 1000);
			timer.start();
			Log.i(TAG, " Inside resume new game time ");
		}else{
			SharedPreferences sharedPreferences = getSharedPreferences(MYPREFS, Context.MODE_PRIVATE);
			int sharedScore = sharedPreferences.getInt(SCORE_KEY,0);
			Log.i(TAG,"score on resume: " +score);
			score = sharedScore;
			setScore(score);
			long resumeTime = sharedPreferences.getLong(TIMER_KEY, 20) * 1000;
			Log.i(TAG, "time: continue " + resumeTime);
			timer = new WordGameCounter(resumeTime, 1000);
			String savedChars = sharedPreferences.getString(LETTERS_KEY, HARDCODED_STRING);
			setGameLetterFromString(savedChars);
			timer.start();
		}
		
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
	         startActivity(new Intent(this, Prefs.class));
	         return true;
	      }
	   return false;
	}

	public void setHintWord(String text){
		hintButton.setText(text);
	}
	
	public String getHintWordFromList(){
		String hintString = "";
		boolean gotHint = false;
		while(!gotHint){
			char[] hintArray = new char[3];
			for(int i=0;i<3;i++){
				int randomLetterInt = new Random().nextInt(63);
				hintArray[i] = currentLettersArrayList.get(randomLetterInt);
			}
			String permString = new String(hintArray);
			permutation("", permString);
			
			for(String hint:hintList){
				String wordToFind = hint.toLowerCase();
				AssetManager assetManager = getAssets();
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
						hintString = wordToFind;
						break;
					}
				}
				loadList.clear();
			}
			gotHint = true;
			
		}
		return hintString;
	}
	
	public void permutation(String prefix,String str){
		int n = str.length();
		if(n == 0){
			hintList.add(prefix);
		}else{
			for(int i=0;i<n;i++){
				permutation(prefix+str.charAt(i), str.substring(0,i)+str.substring(i+1,n));
			}
		}
	}
	
	protected char getCharForTile(int turn) {
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
	

	public void setScore(int num){
		scoreButton.setText("Score: " + num);
		Log.i(TAG,"score in isNewGame " + num);
	}
	
	public void changePauseToResume(){
		SharedPreferences sharedPreferences = getSharedPreferences(MYPREFS, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putLong(TIMER_KEY, timerTime);
		editor.putString(LETTERS_KEY, getGameLetterInString());
		editor.commit();
		timer.cancel();
		//pauseButton.setText("Resume");
	}
	
	public void changeResumeToPause(){
		SharedPreferences sharedPreferences = getSharedPreferences(MYPREFS, Context.MODE_PRIVATE);
		long resumeTime = sharedPreferences.getLong(TIMER_KEY, 20) * 1000;
		timer = new WordGameCounter(resumeTime, 1000);
		String savedChars = sharedPreferences.getString(LETTERS_KEY, HARDCODED_STRING);
		setGameLetterFromString(savedChars);
		timer.start();
		//pauseButton.setText("Pause");
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
			SharedPreferences sharedPreferences = getSharedPreferences(MYPREFS, Context.MODE_PRIVATE);
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
	
	public void setRedColorToTextButton(){
		timerButton.setTextColor(getResources().getColor(R.color.red_color));
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
	
	public class WordGameCounter extends CountDownTimer{
		public WordGameCounter(long millisInFuture,long countDownInterval){
			super(millisInFuture, countDownInterval);
			
		}

		@Override
		public void onFinish() {
			Intent gameFinishIntent = new Intent(context,WordGameFinish.class);
			gameFinishIntent.putExtra(SCORE_KEY, score);
			Log.i(TAG, "on finish score " + score);
			startActivity(gameFinishIntent);
			finish();	
		}

		@Override
		public void onTick(long millisUntilFinished) {
			timerTime = millisUntilFinished/1000;
			if(timerTime < 10){
				setRedColorToTextButton();
			}
			timerButton.setText("Time: " + timerTime);
		}
	}

}
