package edu.neu.madcourse.gauravrane.communication;

import edu.neu.madcourse.gauravrane.R;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import edu.neu.mhealth.api.KeyValueAPI;

public class CommunicationActivity extends Activity{
	private static final String TEAM_NAME = "GAURAV";
	private static final String PASS = "GAURAV11";
	private static final String SEND = "Send";
	private static final String GET = "Get";
	private static final String CLEAR = "Clear";
	private static final String ERROR = "ERROR";
	private static final String SUCCESS = "Successfully stored on Server";
	private Button sendToServerButton;
	private Button getFromServerButton;
	private Button clearButton;
	private EditText comKeyEditText;
	private EditText comValueEditText;
	private TextView showValue;
	
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.communication_main_activity);
		
	}
	
	public void onStart(){
		super.onStart();
		sendToServerButton = (Button) findViewById(R.id.communication_send_server);
		getFromServerButton = (Button) findViewById(R.id.communication_get_from_server);
		clearButton = (Button) findViewById(R.id.communication_clear);
		
		comKeyEditText = (EditText) findViewById(R.id.commnunication_key_edit_text); 
		comValueEditText = (EditText) findViewById(R.id.commnunication_value_edit_text);
		
		showValue = (TextView) findViewById(R.id.communication_text_view);
		
		sendToServerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String keyString = comKeyEditText.getText().toString();
				String valueString = comValueEditText.getText().toString();
				if(keyString.length() > 0 && valueString.length() > 0){
					new CallServer().execute(SEND,keyString,valueString);
				}
			}
		});
		
		getFromServerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String keyString = comKeyEditText.getText().toString();
				if(keyString.length()>0){
					new CallServer().execute(GET,keyString);
				}	
			}
		});
		
		clearButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String keyString = comKeyEditText.getText().toString();
				new CallServer().execute(CLEAR,keyString);
			}
		});
		
	}
	
	private class CallServer extends AsyncTask<String, Integer, String>{

		@Override
		protected String doInBackground(String... params) {
			String execute_value = null;
			if(KeyValueAPI.isServerAvailable()){
				String firstParam = params[0];
				if(firstParam.equals(SEND)){
					String key = params[1];
					String value = params[2];
					String putString = KeyValueAPI.put(TEAM_NAME, PASS, key, value);
					if(putString.contains(ERROR)){
						execute_value = "Error in putting key pair value: " + putString;
					}else{
						execute_value = SUCCESS;
					}
				}else if(firstParam.equals(GET)){
					String key = params[1];
					String getString = KeyValueAPI.get(TEAM_NAME, PASS, key);
					if(getString.contains(ERROR)){
						execute_value = "Error in getting key pair value: " + getString;
					}else{
						execute_value = getString;
					}
				}else if(firstParam.equals(CLEAR)){
					String key = params[1];
					if(key.length() == 0){
						String clearString = KeyValueAPI.clear(TEAM_NAME, PASS);
						if(clearString.contains(ERROR)){
							execute_value = "Error in clearing all key pair values: " + clearString;
						}else{
							execute_value = "Cleared Entire List";
						}
					}else{
						String clearString = KeyValueAPI.clearKey(TEAM_NAME, PASS, key);
						if(clearString.contains(ERROR)){
							execute_value = "Error in clearing all key pair values: " + clearString;
						}else{
							execute_value = "Cleared specific key/value pair";
						}
					}
				}
			}else{
				execute_value = "Problem in server availability";
			}
			return execute_value;
		}
		
		@Override
		protected void onPostExecute(String result){
			super.onPostExecute(result);
			showValue.setText(result);
			comKeyEditText.setText("");
			comValueEditText.setText("");
		}
		
	}
	
}
