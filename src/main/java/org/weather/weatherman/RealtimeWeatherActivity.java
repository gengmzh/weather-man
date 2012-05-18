package org.weather.weatherman;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class RealtimeWeatherActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.realtime);
		this.refresh();
	}

	void refresh() {
		WeatherApplication app = (WeatherApplication) getApplication();
		String citycode = app.getCitycode();
		if (citycode != null && citycode.length() > 0) {
			Uri uri = Uri.withAppendedPath(Weather.RealtimeWeather.CONTENT_URI, citycode);
			Cursor cursor = getContentResolver().query(uri, null, null, null, null);
			if (cursor.moveToFirst()) {
//				// city
//				String text = cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.NAME));
//				TextView view = (TextView) findViewById(R.id.city);
//				view.setText(text);
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
				view.setText("舒适度：" + text);
				// dress
				text = cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.DRESS));
				view = (TextView) findViewById(R.id.dress);
				view.setText("穿衣指数：" + text);
				// ultraviolet
				text = cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.ULTRAVIOLET));
				view = (TextView) findViewById(R.id.ultraviolet);
				view.setText("紫外线强度：" + text);
				// cleancar
				text = cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.CLEANCAR));
				view = (TextView) findViewById(R.id.cleancar);
				view.setText("洗车指数：" + text);
				// travel
				text = cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.TRAVEL));
				view = (TextView) findViewById(R.id.travel);
				view.setText("出游指数：" + text);
				// morningexercise
				text = cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.MORNINGEXERCISE));
				view = (TextView) findViewById(R.id.morningexercise);
				view.setText("晨练指数：" + text);
				// sundry
				text = cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.SUNDRY));
				view = (TextView) findViewById(R.id.sundry);
				view.setText("晾晒指数：" + text);
				// irritability
				text = cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.IRRITABILITY));
				view = (TextView) findViewById(R.id.irritability);
				view.setText("过敏指数：" + text);
			} else {
				Log.e(RealtimeWeatherActivity.class.getName(), "can't get realtime weather");
			}
		}
	}

}