package edu.neu.madcourse.gauravrane.twoplayer;

import edu.neu.madcourse.gauravrane.R;
import edu.neu.mhealth.api.KeyValueAPI;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class TwoPlayerHighScore extends Activity{
	
	private static final String TEAM_NAME = "GAURAV";
	private static final String PASS = "GAURAV11";
	protected static final String HIGHSCORE_LIST = "highScore";

	
	private static final String ERROR = "Error";
	private static final String ERROR_NO_KEY = "Error: No Such Key";
	private static final String SUCCESS = "Successfully stored on Server";
	
	ListView listView;
	TextView mTextView;
	Context context;
	String highScoreListString;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twoplayer_highscore_activity);
		context = getApplicationContext();
		
		listView = (ListView) findViewById(R.id.twoplayer_listView1);
		mTextView = (TextView) findViewById(R.id.twoplayer_emptyList_textview);
	}
	
	protected void onResume(){
		super.onResume();
        getHighScoreList();
	}
	
	public void getHighScoreList(){
		if(isNetworkAvailable(context)){
			new AsyncTask<String,Integer, String>(){
				@Override
				protected String doInBackground(String... params) {
					String execute_value = null;
					if(KeyValueAPI.isServerAvailable()){
						String getString = KeyValueAPI.get(TEAM_NAME, PASS, HIGHSCORE_LIST);
						if(getString.equals(ERROR_NO_KEY)){
							execute_value = ERROR_NO_KEY;
						}else if(!getString.contains(ERROR)){
							execute_value = SUCCESS;
							highScoreListString = getString;								
						}else{
							execute_value = getString;
						}
					}else{
						execute_value = "Problem in server availability";
					}
					return execute_value;
				}
				
				@Override
				protected void onPostExecute(String result){
					super.onPostExecute(result);
					if(result.equals(SUCCESS)){
						displayHighScoreList();
					}else if(result.equals(ERROR_NO_KEY)){
						displayEmptyTextViewMessage();
					}else{
						displayErrorTextViewMessage();
					}
				}								
			}.execute();
		}
	}
	
	public void displayHighScoreList(){
		String[] items = highScoreListString.split(" ");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, items);
        
        listView.setAdapter(adapter);
	}
	
	public void displayEmptyTextViewMessage(){
		mTextView.setText("High Score List is empty right now!!!");
	}
	
	public void displayErrorTextViewMessage(){
		mTextView.setText("Problem with the internet connection");
	}
	

	protected static boolean isNetworkAvailable(Context context) 
    {
        ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        android.net.NetworkInfo mobile = connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return wifi.isConnected() || mobile.isConnected();
    }
}
