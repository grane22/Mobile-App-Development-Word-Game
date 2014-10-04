package edu.neu.madcourse.gauravrane.wordgame;

import edu.neu.madcourse.gauravrane.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class WordGameFinish extends Activity implements OnClickListener{
	
	private static final String SCORE_KEY = "ScoreValue";
	private static final String TAG = "WordGame";
	public static final String MYPREFS = "MyPrefs";
	private static final String IS_NEW_GAME = "isNewGame";
	public static final String IS_GAME_OVER = "isGameOver";
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wordgame_timeout_activity);
		
		TextView scoreDisplayTextView = (TextView)findViewById(R.id.wordgame_score_name);
		Bundle extras = getIntent().getExtras();
		int scoreValue = extras.getInt(SCORE_KEY);
		scoreDisplayTextView.setText("Congrats!!! You scored " +scoreValue + " points.");
		Log.i(TAG, "Score in Finish Class: " + scoreValue);
		
		
		View mainMenuButton = findViewById(R.id.main_menu_button);
		mainMenuButton.setOnClickListener(this);
		View replayButton = findViewById(R.id.replay_button);
		replayButton.setOnClickListener(this);
		
	}
	
	@Override
	public void onPause(){
		super.onPause();
		Music.stop(this);
		SharedPreferences sharedPreferences = getSharedPreferences(MYPREFS, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putBoolean(IS_GAME_OVER, true);
		editor.commit();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		Music.play(this,R.raw.word_game_over);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.main_menu_button:
			getToMainMenu();
			break;
		case R.id.replay_button:
			startGame();
			break;
		}
	}
		
		public void startGame() {
			finish();
			boolean isNewGame = true;
			Intent newGame = new Intent(this, WordGame.class);
			//newGame.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			newGame.putExtra(IS_NEW_GAME, isNewGame);
			startActivity(newGame);
		}
		
		public void getToMainMenu(){
			/*finish();
			finish();
			finish();*/
			SharedPreferences sharedPreferences = getSharedPreferences(MYPREFS, Context.MODE_PRIVATE);
			Editor editor = sharedPreferences.edit();
			editor.putBoolean(IS_GAME_OVER, true);
			editor.commit();
			
			Intent mainMenu = new Intent(this,WordGameStart.class);
			mainMenu.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(mainMenu);
			
		}
		
		
	}
