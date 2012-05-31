package org.weather.weatherman.activity;

import org.weather.weatherman.R;
import org.weather.weatherman.WeatherApplication;
import org.weather.weatherman.content.Weather;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.domob.android.ads.DomobAdView;

public class RealtimeActivity extends Activity {

	private WeatherApplication app;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.realtime);
		app = (WeatherApplication) getApplication();
		// domob
		RelativeLayout adContainer = (RelativeLayout) findViewById(R.id.adContainver);
		DomobAdView adView = new DomobAdView(this, WeatherApplication.DOMOB_PUBLISHER_ID,
				DomobAdView.INLINE_SIZE_320X50);
		adContainer.addView(adView);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		ProgressBar progressBar = (ProgressBar) getParent().findViewById(R.id.progressBar);
		progressBar.setVisibility(View.VISIBLE);
		String city = (app.getCity() != null ? app.getCity().getId() : null);
		new RealtimeTask().execute(city);
	}

	class RealtimeTask extends AsyncTask<String, Integer, Cursor> {

		public RealtimeTask() {
		}

		@Override
		protected Cursor doInBackground(String... params) {
			onProgressUpdate(0);
			String city = (params != null && params.length > 0 ? params[0] : null);
			if (city == null || city.length() == 0) {
				return null;
			}
			onProgressUpdate(20);
			Uri uri = Uri.withAppendedPath(Weather.RealtimeWeather.CONTENT_URI, city);
			onProgressUpdate(40);
			Cursor cursor = getContentResolver().query(uri, null, null, null, null);
			onProgressUpdate(60);
			return cursor;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			int progress = (values != null && values.length > 0 ? values[0] : 0);
			ProgressBar progressBar = (ProgressBar) getParent().findViewById(R.id.progressBar);
			if (progressBar != null) {
				Log.i(RealtimeTask.class.getSimpleName(), progress + "/" + progressBar.getMax());
				progressBar.setProgress(progress);
				if (progress >= progressBar.getMax()) {
					progressBar.setVisibility(View.GONE);
				}
			}
		}

		@Override
		protected void onPostExecute(Cursor cursor) {
			super.onPostExecute(cursor);
			onProgressUpdate(80);
			if (cursor != null && cursor.moveToFirst()) {
				// temperature
				String text = cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.TEMPERATURE));
				TextView view = (TextView) findViewById(R.id.temperatue);
				view.setText(text);
				// wind
				view = (TextView) findViewById(R.id.wind);
				text = cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.WINDDIRECTION));
				view.setText(text);
				text = cursor.getString(cursor.getColumnIndex(Weather.RealtimeWeather.WINDFORCE));
				if (text != null && !text.equals(view.getText())) {
					view.setText(view.getText() + text);
				}
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
			onProgressUpdate(100);
		}
	}

}