package edu.neu.madcourse.gauravrane.communication;

import edu.neu.madcourse.gauravrane.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.widget.Button;


public class CommunicationStartPage extends Activity implements OnClickListener{
	
	public Button twoPlayerExperimentButton;
	public Button mhealthExperimentButton;
	public Button comAcknowledgementButton;
	public Button backToMainButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.communication_start_page_activity);
		
		twoPlayerExperimentButton = (Button)findViewById(R.id.show_two_player_communication);
		twoPlayerExperimentButton.setOnClickListener(this);
		mhealthExperimentButton = (Button)findViewById(R.id.fetch_data_from_mhealth_server);
		mhealthExperimentButton.setOnClickListener(this);
		comAcknowledgementButton = (Button)findViewById(R.id.communication_acknowledge_button);
		comAcknowledgementButton.setOnClickListener(this);
		backToMainButton = (Button)findViewById(R.id.communication_back_main_activity_button);
		backToMainButton.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.show_two_player_communication:
			Intent showTwoPlayerIntent = new Intent(this,ComGSMMainActivity.class);
			startActivity(showTwoPlayerIntent);
			break;
		case R.id.fetch_data_from_mhealth_server:
			Intent fetchFromMHealth = new Intent(this,CommunicationActivity.class);
			startActivity(fetchFromMHealth);
			break;
		case R.id.communication_acknowledge_button:
			Intent comAcknowledgeIntent = new Intent(this,CommunicationAcknowledge.class);
			startActivity(comAcknowledgeIntent);
			break;
		case R.id.communication_back_main_activity_button:
			finish();
			break;
		}
		
		
	}	

}
