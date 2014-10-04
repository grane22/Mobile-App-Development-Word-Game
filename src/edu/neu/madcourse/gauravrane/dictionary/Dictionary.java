package edu.neu.madcourse.gauravrane.dictionary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import edu.neu.madcourse.gauravrane.R;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class Dictionary extends Activity {

	private EditText word;
	private TextView outputView;
	private HashMap<Character, String> charMap = null;
	private LinkedHashSet<String> listOfWords = new LinkedHashSet<String>();
	private ArrayList<String> loadList = new ArrayList<String>();;
	private InputStream is = null;
	private BufferedReader bufferedReader = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dictionary_activity);
		int ascii_start = 97;
		charMap = new HashMap<Character, String>();
		word = (EditText) findViewById(R.id.dictionaryEditText);
		outputView = (TextView) findViewById(R.id.dictionary_TextView);
		for (int i = 1; i <= 26; i++) {
			String value = Character.toString((char) (ascii_start));
			charMap.put((char) (ascii_start), (value + "_list.txt"));
			ascii_start++;
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		startValidatingUserInput();
		onRenderScreen();
	}

	@Override
	protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		state.putString("TextViewValue", (String) outputView.getText());
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		outputView.setText(state.getString("TextViewValue"));
	}

	private void startValidatingUserInput() {
		final MediaPlayer mediaPlayer = MediaPlayer.create(this,
				R.raw.dictionary_beep);
		word.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable enteredChar) {
				AssetManager assetManager = getAssets();
				String wordToFind = word.getText().toString().toLowerCase();
				if (enteredChar.length() == 0) {
					is = null;
					loadList.clear();
				} else if (enteredChar.length() == 2 && is == null) {
					try {
						String filePath = "dictionary_text_files/"
								+ charMap.get(wordToFind.charAt(0));
						is = assetManager.open(filePath);
						bufferedReader = new BufferedReader(
								new InputStreamReader(is));
						String line;
						while ((line = bufferedReader.readLine()) != null) {
							loadList.add(line);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else if (enteredChar.length() >= 3) {
					String toCompare = enteredChar.toString().toLowerCase();
					if (loadList.contains(toCompare)) {
						listOfWords.add(wordToFind);
						onRenderScreen();
						mediaPlayer.start();
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub

			}
		});

	}

	public void onRenderScreen() {
		StringBuilder stringBuilder = new StringBuilder();
		for (String earlierWord : listOfWords) {
			if (earlierWord != null) {
				stringBuilder.append(earlierWord);
				stringBuilder.append(" ");
			}
		}
		outputView.setText(stringBuilder.toString());
	}

	public void clear(View view) {
		word.setText("");
		outputView.setText("");
		listOfWords.clear();
	}

	public void returnToMenu(View view) {
		finish();
	}

	public void acknowledge(View view) {
		Intent i = new Intent(this, DictionaryAcknowledge.class);
		startActivity(i);
	}

}
