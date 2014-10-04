package edu.neu.madcourse.gauravrane.wordgame;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import edu.neu.madcourse.gauravrane.R;
import edu.neu.madcourse.gauravrane.twoplayer.TwoPlayerWordGame;
import edu.neu.madcourse.gauravrane.wordgame.WordGame.Coordinate;
import android.R.integer;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class WordGameView extends View {

	private float width;
	private float height;
	private static final int ID = 42;
	private static final String TAG = "WordGame";
	private Paint dark;
	private Paint hilite;
	private Paint light;
	private Paint background;
	private Paint foreground;
	private Paint selected;
	private final WordGame wordGame;
	private int charCount = 0;
	private Rect selRect = new Rect();
	private static final String SELX = "selX";
	private static final String SELY = "selY";
	private final static int[] RANDOM_INDEX = {0,1,2,3,4};
	private int selX;
	private static final char blank = ' '; 
	private int selY;

	public WordGameView(Context context) {
		super(context);
		this.wordGame = (WordGame) context;
		setFocusable(true);
		setFocusableInTouchMode(true);
		setId(ID);
	}

	public WordGameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.wordGame = (WordGame) context;
	}

	public WordGameView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.wordGame = (WordGame) context;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		width = w / 8f;
		height = h / 8f;
		getRect(selX, selY, selRect);
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		background = new Paint();
		// TODO: Change the color here
		background.setColor(getResources().getColor(R.color.puzzle_background));
		canvas.drawRect(0, 0, getWidth(), getHeight(), background);
		// Log.i(TAG, "width: " + getWidth() + " height: " + getHeight());

		selected = new Paint();
		selected.setColor(getResources().getColor(R.color.puzzle_selected));
		dark = new Paint();
		dark.setColor(getResources().getColor(R.color.puzzle_dark));

		for (int i = 0; i < 8; i++) {
			canvas.drawLine(0, i * height, getWidth(), i * height, dark);
		}

		for (int i = 0; i < 8; i++) {
			canvas.drawLine(i * width, 0, i * width, getHeight(),
					dark);
		}

		foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
		foreground.setColor(getResources().getColor(R.color.puzzle_foreground));
		foreground.setStyle(Style.FILL);
		foreground.setTextSize(height * 0.75f);
		foreground.setTextScaleX(width / height);
		foreground.setTextAlign(Paint.Align.CENTER);

		FontMetrics fm = foreground.getFontMetrics();
		// Centering in X: use alignment (and X at midpoint)
		float x = width / 2;
		// Centering in Y: measure ascent/descent first
		float y = (height / 2 - (fm.ascent + fm.descent) / 2);

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (!isCoodInDictionary(i, j)) {
					canvas.drawText(Character.toString(wordGame.getRectLetter(i,j)),i * width + x, j * height + y, foreground);
				}
				if (isCoodSelected(i, j)) {
					Rect rect = new Rect();
					getRect(i, j, rect);
					canvas.drawRect(rect, selected);
				}
			}
		}

	}

	private boolean isCoodSelected(int i, int j) {
		return wordGame.isCoodInList(i, j);
	}

	private boolean isCoodInDictionary(int i, int j) {
		return wordGame.isCoodInMatch(i, j);
	}
	
	public void invalidateRects(ArrayList<Coordinate> list) {
		for (int i = 0; i < list.size(); i++) {
			Coordinate cod = list.get(i);
			int coodX = cod.x;
			int coodY = cod.y;
			Rect in = new Rect();
			getRect(coodX, coodY, in);
			invalidate(in);
		}
	}
	
	public void invalidateNewChars(ArrayList<Coordinate> list){
		for(int i=0;i<list.size();i++){
			Coordinate cod = list.get(i);
			int coodX = cod.x;
			int coodY = cod.y;
			Rect in = new Rect();
			getRect(coodX,coodY,in);
			wordGame.setRectLetter(coodX,coodY, wordGame.getCharForTile(new Random().nextInt(RANDOM_INDEX.length)));
			invalidate(in);
		}
	}
	
	
	public void invalidateClearSelection(){
		invalidate();
	}
	

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() != MotionEvent.ACTION_DOWN)
			return super.onTouchEvent(event);
		if(!wordGame.isPausedPressed){
			Music.playThird(wordGame,R.raw.dictionary_beep);
			int eventX = (int) (event.getX() / width);
			int eventY = (int) (event.getY() / height);
			if(!isCoodSelected(eventX, eventY) && !isCoodInDictionary(eventX, eventY)){
				wordGame.addCood(eventX, eventY);
				selectRect(eventX, eventY);
				wordGame.setButtonText(eventX, eventY);
			}
		}
		
		return true;
		
	}

	private void selectRect(int x, int y) {
		selX = Math.min(Math.max(x, 0), 7);
		selY = Math.min(Math.max(y, 0), 7);
		getRect(selX, selY, selRect);
		invalidate(selRect);
	}

	private void getRect(int x, int y, Rect rect) {
		rect.set((int) (x * width), (int) (y * height),
				(int) (x * width + width), (int) (y * height + height));
	}

}
