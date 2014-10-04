package edu.neu.madcourse.gauravrane.twoplayer;

import edu.neu.madcourse.gauravrane.R;
import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;

public class TwoPlayerWordGameAcknowledge extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twoplayer_acknowledge_activity);
		
	}
	
	@Override
	public void onPause(){
		super.onPause();
		TwoPlayerMusic.stop(this);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		TwoPlayerMusic.play(this, R.raw.game_start);
	}

}
