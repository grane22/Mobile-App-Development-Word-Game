package edu.neu.madcourse.gauravrane.twoplayer;

import edu.neu.madcourse.gauravrane.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.view.View;

public class TwoPlayerWordGameStart extends Activity implements OnClickListener {
	public static final String IS_NEW_GAME = "isNewGame";
	public static final String IS_GAME_OVER = "isGameOver";
	public static final String MYPREFS = "MyPrefs";
	
	private static final String TARGET_SCORE = "targetScore";
	private static final String MYTURN = "myturn";
	private static final String FIRST_MOVE = "firstMove";
	
	public boolean isGameOver = true;
	public boolean isNewGame = true;
	public boolean isMyTurn;
	public String targetScoreString;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twoplayer_main_activity);
		Bundle extrasBundle = getIntent().getExtras();
		isMyTurn = extrasBundle.getBoolean(MYTURN);
		targetScoreString = extrasBundle.getString(TARGET_SCORE);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		TwoPlayerMusic.play(this, R.raw.game_start);
		SharedPreferences sharedPreferences = getGCMPreferences();
		isGameOver = sharedPreferences.getBoolean(IS_GAME_OVER, true);
		Log.d("Inside Start:OnResume", isGameOver+"");
		if(!isGameOver){
			Log.d("Continue visible", "Continue can be seen");
			View continueButton = findViewById(R.id.twoplayer_continue_button);
			continueButton.setVisibility(View.VISIBLE);
			continueButton.setOnClickListener(this);
		}else{
			Log.d("Continue Invisible", "Continue cant be seen");
			View continueButton = findViewById(R.id.twoplayer_continue_button);
			continueButton.setVisibility(View.GONE);
			continueButton.setOnClickListener(this);
		}
		
		View newGameButton = findViewById(R.id.twoplayer_newgame_button);
		newGameButton.setOnClickListener(this);
		View acknowledgeButton = findViewById(R.id.twoplayer_acknowledge_button);
		acknowledgeButton.setOnClickListener(this);
		View instructionButton = findViewById(R.id.twoplayer_instruction_button);
		instructionButton.setOnClickListener(this);
		View highScoreButton = findViewById(R.id.twoplayer_highScores_button);
		highScoreButton.setOnClickListener(this);
		View settingButton = findViewById(R.id.twoplayer_setting_button);
		settingButton.setOnClickListener(this);
		View exitButton = findViewById(R.id.twoplayer_exit_button);
		exitButton.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.twoplayer_continue_button:
			startGame(false);
			break;
		case R.id.twoplayer_newgame_button:
			startGame(true);
			break;
		case R.id.twoplayer_acknowledge_button:
			Intent acknowledgeIntent = new Intent(this,
					TwoPlayerWordGameAcknowledge.class);
			startActivity(acknowledgeIntent);
			break;
		case R.id.twoplayer_instruction_button:
			Intent instructionIntent = new Intent(this,TwoPlayerWordGameInstruction.class);
			startActivity(instructionIntent);
			break;
		case R.id.twoplayer_highScores_button:
			Intent highScoreIntent = new Intent(this,TwoPlayerHighScore.class);
			startActivity(highScoreIntent);
			break;
		case R.id.twoplayer_setting_button:
			Intent settingsIntent = new Intent(this,TwoPlayerPrefs.class);
			startActivity(settingsIntent);
			break;
		case R.id.twoplayer_exit_button:
			finish();
			break;
		}
	}
	

	public void startGame(boolean isNewGame) {
		Intent wordGameIntent;
		if(isNewGame){
			wordGameIntent = new Intent(this, TwoPlayerNewGameOption.class);
			wordGameIntent.putExtra(IS_NEW_GAME, isNewGame);
		}else{
			
			wordGameIntent = new Intent(this, TwoPlayerWordGame.class);
			wordGameIntent.putExtra(MYTURN, isMyTurn);
			wordGameIntent.putExtra(TARGET_SCORE, targetScoreString);
			wordGameIntent.putExtra(IS_NEW_GAME, isNewGame);
			wordGameIntent.putExtra(FIRST_MOVE, false);
		}
		startActivity(wordGameIntent);
		
		SharedPreferences sharedPreferences = getGCMPreferences();
		Editor editor = sharedPreferences.edit();
		editor.putBoolean(IS_GAME_OVER, false);
		editor.commit();
	}
	
	@Override
	public void onPause(){
		super.onPause();
		TwoPlayerMusic.stop(this);
	}
	
	private SharedPreferences getGCMPreferences() {
        return getSharedPreferences(TwoPlayerNewGameOption.class.getSimpleName(),
                Context.MODE_MULTI_PROCESS);
    }
	
	

}
