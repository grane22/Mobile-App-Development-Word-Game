package edu.neu.madcourse.gauravrane.wordgame;

import edu.neu.madcourse.gauravrane.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.view.View;

public class WordGameStart extends Activity implements OnClickListener {
	public static final String IS_NEW_GAME = "isNewGame";
	public static final String IS_GAME_OVER = "isGameOver";
	public static final String MYPREFS = "MyPrefs";
	public boolean isGameOver = true;
	public boolean isNewGame = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wordgame_main_activity);
		
	}
	
	@Override
	public void onResume(){
		super.onResume();
		Music.play(this, R.raw.game_start);
		
		SharedPreferences sharedPreferences = getSharedPreferences(MYPREFS, Context.MODE_PRIVATE);
		isGameOver = sharedPreferences.getBoolean(IS_GAME_OVER, true);
		if(!isGameOver){
			View continueButton = findViewById(R.id.continue_button);
			continueButton.setVisibility(View.VISIBLE);
			continueButton.setOnClickListener(this);
		}else{
			View continueButton = findViewById(R.id.continue_button);
			continueButton.setVisibility(View.GONE);
			continueButton.setOnClickListener(this);
		}
		
		View newGameButton = findViewById(R.id.newgame_button);
		newGameButton.setOnClickListener(this);
		View acknowledgeButton = findViewById(R.id.acknowledge_button);
		acknowledgeButton.setOnClickListener(this);
		View instructionButton = findViewById(R.id.instruction_button);
		instructionButton.setOnClickListener(this);
		View settingButton = findViewById(R.id.setting_button);
		settingButton.setOnClickListener(this);
		View exitButton = findViewById(R.id.exit_button);
		exitButton.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.continue_button:
			startGame(false);
			break;
		case R.id.newgame_button:
			startGame(true);
			break;
		case R.id.acknowledge_button:
			Intent acknowledgeIntent = new Intent(this,
					WordGameAcknowledge.class);
			startActivity(acknowledgeIntent);
			break;
		case R.id.instruction_button:
			Intent instructionIntent = new Intent(this,WordGameInstruction.class);
			startActivity(instructionIntent);
			break;
		case R.id.setting_button:
			Intent settingsIntent = new Intent(this,Prefs.class);
			startActivity(settingsIntent);
			break;
		case R.id.exit_button:
			finish();
			break;
		}
	}

	public void startGame(boolean isNewGame) {
		Intent wordGameIntent = new Intent(this, WordGame.class);
		wordGameIntent.putExtra(IS_NEW_GAME, isNewGame);
		startActivity(wordGameIntent);
		SharedPreferences sharedPreferences = getSharedPreferences(MYPREFS, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putBoolean(IS_GAME_OVER, false);
		editor.commit();
	}
	
	@Override
	public void onPause(){
		super.onPause();
		Music.stop(this);
	}
	
	

}
