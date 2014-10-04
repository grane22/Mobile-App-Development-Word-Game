package edu.neu.madcourse.gauravrane.twoplayer;

import android.app.Activity;
import android.os.Bundle;
import edu.neu.madcourse.gauravrane.R;

public class TwoPlayerWordGameInstruction extends Activity{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twoplayer_instruction);

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
