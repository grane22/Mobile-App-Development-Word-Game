package edu.neu.madcourse.gauravrane.wordgame;

import android.app.Activity;
import android.os.Bundle;
import edu.neu.madcourse.gauravrane.R;

public class WordGameInstruction extends Activity{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wordgame_instruction);

	}
	
	@Override
	public void onPause(){
		super.onPause();
		Music.stop(this);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		Music.play(this, R.raw.game_start);
	}

}
