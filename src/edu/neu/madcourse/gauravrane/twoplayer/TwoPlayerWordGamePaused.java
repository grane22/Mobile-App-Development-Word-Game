package edu.neu.madcourse.gauravrane.twoplayer;

import edu.neu.madcourse.gauravrane.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TwoPlayerWordGamePaused extends Activity{
	private Button resumeButton;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twoplayer_paused_activity);
		
		resumeButton = (Button) findViewById(R.id.twoplayer_paused_button_set);
		resumeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
	}

}
