package org.weather.weatherman.activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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

		private DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
		private Cursor index;

		@Override
		protected Cursor doInBackground(String... params) {
			onProgressUpdate(0);
			String city = (params != null && params.length > 0 ? params[0] : null);
			if (city == null || city.length() == 0) {
				return null;
			}
			onProgressUpdate(20);
			Uri uri = Uri.withAppendedPath(Weather.RealtimeWeather.CONTENT_URI, city);
			Cursor realtime = getContentResolver().query(uri, null, null, null, null);
			onProgressUpdate(40);
			uri = Uri.withAppendedPath(Weather.LivingIndex.CONTENT_URI, city);
			index = getContentResolver().query(uri, null, null, null, null);
			onProgressUpdate(60);
			return realtime;
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
		protected void onPostExecute(Cursor realtime) {
			super.onPostExecute(realtime);
			onProgressUpdate(80);
			TextView msg = (TextView) getParent().findViewById(R.id.msg);
			msg.setText("");
			if (realtime != null && realtime.moveToFirst()) {
				// temperature
				String text = realtime.getString(realtime.getColumnIndex(Weather.RealtimeWeather.TEMPERATURE));
				TextView view = (TextView) findViewById(R.id.temperatue);
				view.setText(text);
				// wind
				view = (TextView) findViewById(R.id.wind);
				text = realtime.getString(realtime.getColumnIndex(Weather.RealtimeWeather.WINDDIRECTION));
				view.setText(text);
				text = realtime.getString(realtime.getColumnIndex(Weather.RealtimeWeather.WINDFORCE));
				if (text != null && !text.equals(view.getText())) {
					view.setText(view.getText() + text);
				}
				// humidity
				text = realtime.getString(realtime.getColumnIndex(Weather.RealtimeWeather.HUMIDITY));
				view = (TextView) findViewById(R.id.humidity);
				view.setText("湿度" + text);
				// updateTime
				text = realtime.getString(realtime.getColumnIndex(Weather.RealtimeWeather.TIME));
				view = (TextView) findViewById(R.id.updateTime);
				view.setText(DATE_FORMAT.format(new Date()) + " " + text + "更新");
			} else {
				msg.setText("网络连接失败");
				Log.e(RealtimeActivity.class.getName(), "can't get realtime weather");
			}
			if (index != null && index.moveToFirst()) {
				// comfort
				String text = index.getString(index.getColumnIndex(Weather.LivingIndex.COMFORT));
				TextView view = (TextView) findViewById(R.id.comfort);
				view.setText(text);
				// dress
				text = index.getString(index.getColumnIndex(Weather.LivingIndex.DRESS));
				view = (TextView) findViewById(R.id.dress);
				view.setText(text);
				// ultraviolet
				text = index.getString(index.getColumnIndex(Weather.LivingIndex.ULTRAVIOLET));
				view = (TextView) findViewById(R.id.ultraviolet);
				view.setText(text);
				// cleancar
				text = index.getString(index.getColumnIndex(Weather.LivingIndex.CLEANCAR));
				view = (TextView) findViewById(R.id.cleancar);
				view.setText(text);
				// travel
				text = index.getString(index.getColumnIndex(Weather.LivingIndex.TRAVEL));
				view = (TextView) findViewById(R.id.travel);
				view.setText(text);
				// morningexercise
				text = index.getString(index.getColumnIndex(Weather.LivingIndex.MORNINGEXERCISE));
				view = (TextView) findViewById(R.id.morningexercise);
				view.setText(text);
				// sundry
				text = index.getString(index.getColumnIndex(Weather.LivingIndex.SUNDRY));
				view = (TextView) findViewById(R.id.sundry);
				view.setText(text);
				// irritability
				text = index.getString(index.getColumnIndex(Weather.LivingIndex.IRRITABILITY));
				view = (TextView) findViewById(R.id.irritability);
				view.setText(text);
			} else {
				msg.setText("网络连接失败");
				Log.e(RealtimeActivity.class.getName(), "can't get weather index");
			}
			onProgressUpdate(100);
		}
	}

}