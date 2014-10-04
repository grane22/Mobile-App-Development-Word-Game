package edu.neu.madcourse.gauravrane.wordgame;

import edu.neu.madcourse.gauravrane.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class WordGamePaused extends Activity{
	private Button resumeButton;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wordgame_paused_activity);
		
		resumeButton = (Button) findViewById(R.id.wordgame_paused_button_set);
		resumeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
	}

}
