package org.weather.weatherman;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class WeathermanActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		TextView rt = (TextView) findViewById(R.id.rt_text);
		rt.setText("realtime weather");
	}
}