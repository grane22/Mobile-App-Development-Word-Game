package edu.neu.madcourse.gauravrane;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.widget.ImageView;
import android.widget.TextView;

public class HelloMadAbout extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hello_mad_about);

		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String imei_id = telephonyManager.getDeviceId();

		TextView i = (TextView) findViewById(R.id.my_phone_imei_id);// Getting
																	// the
																	// TextView
																	// to
																	// display
																	// IMEI
																	// number
		i.setText("IMEI number - " + imei_id);// Displaying IMEI number

		ImageView image = (ImageView) findViewById(R.id.my_image_id);
		image.setImageResource(R.drawable.display_image);
	}
}
