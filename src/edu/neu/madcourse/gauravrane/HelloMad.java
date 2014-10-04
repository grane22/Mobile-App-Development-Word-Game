package edu.neu.madcourse.gauravrane;


import edu.neu.madcourse.gauravrane.communication.ComGSMMainActivity;
import edu.neu.madcourse.gauravrane.communication.CommunicationActivity;
import edu.neu.madcourse.gauravrane.communication.CommunicationStartPage;
import edu.neu.madcourse.gauravrane.dictionary.Dictionary;
import edu.neu.madcourse.gauravrane.sudoku.Sudoku;
import edu.neu.madcourse.gauravrane.twoplayer.TwoPlayerWordGameStart;
import edu.neu.madcourse.gauravrane.wordgame.WordGameStart;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.View.OnClickListener;

public class HelloMad extends Activity implements OnClickListener {	
	
	private static final String TARGET_SCORE = "targetScore";
	private static final String MYTURN = "myturn";
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.about_button:
			Intent about_intent = new Intent(this, HelloMadAbout.class);
			startActivity(about_intent);
			break;
		case R.id.generate_error_button:
			int calc = 5 / 0;
			break;
		case R.id.sudoku_button:
			Intent sudoku_intent = new Intent(this, Sudoku.class);
			startActivity(sudoku_intent);
			break;
		case R.id.dictionary_button:
			Intent dict_intent = new Intent(this, Dictionary.class);
			startActivity(dict_intent);
			break;
		case R.id.wordgame_button:		
			Intent wordgame_intent = new Intent(this, WordGameStart.class);
			startActivity(wordgame_intent);
			break;
		case R.id.twoPlayerWordGameButton:
			Intent twoPlayerwordgame_Intent = new Intent(this,TwoPlayerWordGameStart.class);
			twoPlayerwordgame_Intent.putExtra(MYTURN, true);
			twoPlayerwordgame_Intent.putExtra(TARGET_SCORE, "100");
			startActivity(twoPlayerwordgame_Intent);
			break;
		case R.id.communication_button:
			Intent communication_intent = new Intent(this, CommunicationStartPage.class);
			startActivity(communication_intent);
			break;
		case R.id.trickiestPartButton:
			Intent trickiestIntent = new Intent("edu.neu.madcourse.dushyantdeshmukh.TRICKIEST_PART");
			startActivity(trickiestIntent);
			break;
		case R.id.quit_button:
			finish();
			break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hello_mad);

		// set up listeners for clicking on buttons
		View aboutButton = findViewById(R.id.about_button);
		aboutButton.setOnClickListener(this);
		View generateErrorButton = findViewById(R.id.generate_error_button);
		generateErrorButton.setOnClickListener(this);
		View sudokuButton = findViewById(R.id.sudoku_button);
		sudokuButton.setOnClickListener(this);
		View dictionaryButton = findViewById(R.id.dictionary_button);
		dictionaryButton.setOnClickListener(this);
		View wordGameButton = findViewById(R.id.wordgame_button);
		wordGameButton.setOnClickListener(this);
		View communicationButton = findViewById(R.id.communication_button);
		communicationButton.setOnClickListener(this);
		View twoPlayerGameButton = findViewById(R.id.twoPlayerWordGameButton);
		twoPlayerGameButton.setOnClickListener(this);
		View trickiestPartButton = findViewById(R.id.trickiestPartButton);
		trickiestPartButton.setOnClickListener(this);
		View quitButton = findViewById(R.id.quit_button);
		quitButton.setOnClickListener(this);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.hello_mad, menu);
		return true;
	}

}
