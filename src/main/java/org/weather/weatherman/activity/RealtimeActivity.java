package org.weather.weatherman.activity;

import org.weather.weatherman.R;
import org.weather.weatherman.WeatherApplication;
import org.weather.weatherman.content.Weather;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class RealtimeActivity extends Activity {

	private WeatherApplication app;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.realtime);
		app = (WeatherApplication) getApplication();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.refresh();
	}

	void refresh() {
		String citycode = (app.getCity() != null ? app.getCity().getId() : null);
		if (citycode == null || citycode.length() == 0) {
			return;
		}
		Uri uri = Uri.withAppendedPath(Weather.RealtimeWeather.CONTENT_URI, citycode);
		Cursor cursor = getContentResolver().query(uri, null, null, null, null);
		if (cursor.moveToFirst()) {
			// temperature
			String text = cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.TEMPERATURE));
			TextView view = (TextView) findViewById(R.id.temperatue);
			view.setText(text);
			// wind
			text = cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.WINDDIRECTION))
					+ cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.WINDFORCE));
			view = (TextView) findViewById(R.id.wind);
			view.setText(text);
			// humidity
			text = cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.HUMIDITY));
			view = (TextView) findViewById(R.id.humidity);
			view.setText("湿度" + text);
			// updateTime
			text = cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.TIME));
			view = (TextView) findViewById(R.id.updateTime);
			view.setText(text + "更新");
			// comfort
			text = cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.COMFORT));
			view = (TextView) findViewById(R.id.comfort);
			view.setText(text);
			// dress
			text = cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.DRESS));
			view = (TextView) findViewById(R.id.dress);
			view.setText(text);
			// ultraviolet
			text = cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.ULTRAVIOLET));
			view = (TextView) findViewById(R.id.ultraviolet);
			view.setText(text);
			// cleancar
			text = cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.CLEANCAR));
			view = (TextView) findViewById(R.id.cleancar);
			view.setText(text);
			// travel
			text = cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.TRAVEL));
			view = (TextView) findViewById(R.id.travel);
			view.setText(text);
			// morningexercise
			text = cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.MORNINGEXERCISE));
			view = (TextView) findViewById(R.id.morningexercise);
			view.setText(text);
			// sundry
			text = cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.SUNDRY));
			view = (TextView) findViewById(R.id.sundry);
			view.setText(text);
			// irritability
			text = cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.IRRITABILITY));
			view = (TextView) findViewById(R.id.irritability);
			view.setText(text);
		} else {
			Log.e(RealtimeActivity.class.getName(), "can't get realtime weather");
		}
	}

}