package edu.neu.madcourse.gauravrane.wordgame;

import edu.neu.madcourse.gauravrane.R;
import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;

public class WordGameAcknowledge extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wordgame_acknowledge_activity);
		
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
