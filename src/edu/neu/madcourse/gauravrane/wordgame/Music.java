package edu.neu.madcourse.gauravrane.wordgame;

import android.content.Context;
import android.media.MediaPlayer;

public class Music {
	private static MediaPlayer mp = null;
	private static MediaPlayer secondmp = null;
	private static MediaPlayer thirdmp = null;
	
	   /** Stop old song and start new one */
	   
	   public static void play(Context context, int resource) {
	      stop(context);
	      // Start music only if not disabled in preferences
	      if (Prefs.getMusic(context)) {
	         mp = MediaPlayer.create(context, resource);
	         mp.setLooping(true);
	         mp.start();
	      }
	   }
	   
	   public static void playSecond(Context context, int resource) {
		      // Start music only if not disabled in preferences
		      if (Prefs.getMusic(context)) {
		         secondmp = MediaPlayer.create(context, resource);
		         secondmp.setLooping(false);
		         secondmp.start();
		      }
		   }
	   
	   public static void playThird(Context context, int resource) {
		      // Start music only if not disabled in preferences
		      if (Prefs.getMusic(context)) {
		         thirdmp = MediaPlayer.create(context, resource);
		         thirdmp.setLooping(false);
		         thirdmp.start();
		      }
		   }
	   

	   /** Stop the music */
	   public static void stop(Context context) { 
	      if (mp != null) {
	         mp.stop();
	         mp.release();
	         mp = null;
	      }
	   }
}
