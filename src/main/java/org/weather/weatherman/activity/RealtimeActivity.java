package org.weather.weatherman.activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
import android.widget.TextView;

import com.baidu.mobstat.StatService;

public class RealtimeActivity extends Activity {

	private static final String tag = RealtimeTask.class.getSimpleName();

	private WeatherApplication app;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.realtime);
		app = (WeatherApplication) getApplication();
		// domob
		// RelativeLayout adContainer = (RelativeLayout)
		// findViewById(R.id.adContainver);
		// DomobAdView adView = new DomobAdView(this,
		// WeatherApplication.DOMOB_PUBLISHER_ID,
		// DomobAdView.INLINE_SIZE_320X50);
		// adContainer.addView(adView);
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
		this.refreshData();
		// stats
		StatService.onResume(this);
	}

	/**
	 * @author gengmaozhang01
	 * @since 2014-1-25 下午6:04:26
	 */
	public void refreshData() {
		String city = (app.getCity() != null ? app.getCity().getId() : null);
		new RealtimeTask().execute(city);
	}

	class RealtimeTask extends AsyncTask<String, Integer, Boolean> {

		private DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		private Cursor realtimeCursor, indexCursor, aqiCursor;

		@Override
		protected Boolean doInBackground(String... params) {
			onProgressUpdate(0);
			String city = (params != null && params.length > 0 ? params[0] : null);
			if (city == null || city.length() == 0) {
				return false;
			}
			onProgressUpdate(20);
			Uri uri = Uri.withAppendedPath(Weather.RealtimeWeather.CONTENT_URI, city);
			realtimeCursor = getContentResolver().query(uri, null, null, null, null);
			onProgressUpdate(40);
			// if (realtimeCursor != null && realtimeCursor.moveToFirst()) {
			uri = Uri.withAppendedPath(Weather.LivingIndex.CONTENT_URI, city);
			indexCursor = getContentResolver().query(uri, null, null, null, null);
			// }
			uri = Uri.withAppendedPath(Weather.AirQualityIndex.CONTENT_URI, city);
			aqiCursor = getContentResolver().query(uri, null, null, null, null);
			onProgressUpdate(60);
			return true;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			int progress = (values != null && values.length > 0 ? values[0] : 0);
			ProgressBar progressBar = (ProgressBar) getParent().findViewById(R.id.progressBar);
			if (progressBar != null) {
				Log.i(tag, progress + "/" + progressBar.getMax());
				progressBar.setProgress(progress);
				if (progress >= progressBar.getMax()) {
					progressBar.setVisibility(View.GONE);
				}
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			onProgressUpdate(80);
			boolean isOk = true;
			if (realtimeCursor != null && realtimeCursor.moveToFirst()) {
				// temperature
				String text = realtimeCursor.getString(realtimeCursor
						.getColumnIndex(Weather.RealtimeWeather.TEMPERATURE));
				TextView view = (TextView) findViewById(R.id.temperatue);
				view.setText(text);
				// wind
				view = (TextView) findViewById(R.id.wind);
				text = realtimeCursor.getString(realtimeCursor.getColumnIndex(Weather.RealtimeWeather.WINDDIRECTION));
				view.setText(text);
				text = realtimeCursor.getString(realtimeCursor.getColumnIndex(Weather.RealtimeWeather.WINDFORCE));
				if (text != null && !text.equals(view.getText())) {
					view.setText(view.getText() + text);
				}
				// humidity
				text = realtimeCursor.getString(realtimeCursor.getColumnIndex(Weather.RealtimeWeather.HUMIDITY));
				view = (TextView) findViewById(R.id.humidity);
				view.setText("湿度" + text);
				// updateTime
				text = realtimeCursor.getString(realtimeCursor.getColumnIndex(Weather.RealtimeWeather.TIME));
				view = (TextView) findViewById(R.id.updateTime);
				view.setText(DATE_FORMAT.format(new Date()) + " " + text + "更新");
			} else {
				isOk = false;
				// temperature
				TextView view = (TextView) findViewById(R.id.temperatue);
				view.setText("--");
				// wind
				view = (TextView) findViewById(R.id.wind);
				view.setText("--");
				// humidity
				view = (TextView) findViewById(R.id.humidity);
				view.setText("--");
				// updateTime
				view = (TextView) findViewById(R.id.updateTime);
				view.setText("--");
				Log.e(tag, "can't get realtime weather");
			}
			if (indexCursor != null && indexCursor.moveToFirst()) {
				// comfort
				String text = indexCursor.getString(indexCursor.getColumnIndex(Weather.LivingIndex.COMFORT));
				TextView view = (TextView) findViewById(R.id.comfort);
				view.setText(text);
				// dress
				text = indexCursor.getString(indexCursor.getColumnIndex(Weather.LivingIndex.DRESS));
				view = (TextView) findViewById(R.id.dress);
				view.setText(text);
				// ultraviolet
				text = indexCursor.getString(indexCursor.getColumnIndex(Weather.LivingIndex.ULTRAVIOLET));
				view = (TextView) findViewById(R.id.ultraviolet);
				view.setText(text);
				// cleancar
				text = indexCursor.getString(indexCursor.getColumnIndex(Weather.LivingIndex.CLEANCAR));
				view = (TextView) findViewById(R.id.cleancar);
				view.setText(text);
				// travel
				text = indexCursor.getString(indexCursor.getColumnIndex(Weather.LivingIndex.TRAVEL));
				view = (TextView) findViewById(R.id.travel);
				view.setText(text);
				// morningexercise
				text = indexCursor.getString(indexCursor.getColumnIndex(Weather.LivingIndex.MORNINGEXERCISE));
				view = (TextView) findViewById(R.id.morningexercise);
				view.setText(text);
				// sundry
				text = indexCursor.getString(indexCursor.getColumnIndex(Weather.LivingIndex.SUNDRY));
				view = (TextView) findViewById(R.id.sundry);
				view.setText(text);
				// irritability
				text = indexCursor.getString(indexCursor.getColumnIndex(Weather.LivingIndex.IRRITABILITY));
				view = (TextView) findViewById(R.id.irritability);
				view.setText(text);
			} else {
				isOk = false;
				// comfort
				TextView view = (TextView) findViewById(R.id.comfort);
				view.setText("--");
				// dress
				view = (TextView) findViewById(R.id.dress);
				view.setText("--");
				// ultraviolet
				view = (TextView) findViewById(R.id.ultraviolet);
				view.setText("--");
				// cleancar
				view = (TextView) findViewById(R.id.cleancar);
				view.setText("--");
				// travel
				view = (TextView) findViewById(R.id.travel);
				view.setText("--");
				// morningexercise
				view = (TextView) findViewById(R.id.morningexercise);
				view.setText("--");
				// sundry
				view = (TextView) findViewById(R.id.sundry);
				view.setText("--");
				// irritability
				view = (TextView) findViewById(R.id.irritability);
				view.setText("--");
				Log.e(tag, "can't get weather index");
			}
			if (aqiCursor != null && aqiCursor.moveToFirst()) {
				do { // AQI
					String tag = aqiCursor.getString(aqiCursor.getColumnIndex(Weather.AirQualityIndex.TAG));
					if ("current".equalsIgnoreCase(tag)) {
						int aqi = aqiCursor.getInt(aqiCursor.getColumnIndex(Weather.AirQualityIndex.AQI));
						TextView view = (TextView) findViewById(R.id.AQI);
						if (aqi >= 0) {
							String text = String.valueOf(aqi) + "，";
							int color = 0;
							if (aqi <= 50) {
								text += "优";
								color = R.color.AQI_perfect;
							} else if (aqi <= 100) {
								text += "良";
								color = R.color.AQI_fine;
							} else if (aqi <= 150) {
								text += "轻度污染";
								color = R.color.AQI_smell_little;
							} else if (aqi <= 200) {
								text += "中度污染";
								color = R.color.AQI_smell_middle;
							} else if (aqi <= 300) {
								text += "重度污染";
								color = R.color.AQI_smell_heavy;
							} else {
								text += "严重污染";
								color = R.color.AQI_smell_fatal;
							}
							view.setText(text);
							view.setTextColor(getResources().getColor(color));
						} else {
							view.setText("--");
						}
						break;
					}
				} while (aqiCursor.moveToNext());
			} else {
				isOk = false;
				TextView view = (TextView) findViewById(R.id.AQI);
				view.setText("--");
				Log.e(tag, "can't get AQI");
			}
			if (realtimeCursor != null) {
				realtimeCursor.close();
			}
			if (indexCursor != null) {
				indexCursor.close();
			}
			if (aqiCursor != null) {
				aqiCursor.close();
			}
			if (!isOk) {
				ToastService.toastLong(getApplicationContext(), getResources().getString(R.string.connect_failed));
			}
			onProgressUpdate(100);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// stats
		StatService.onPause(this);
	}

	@Override
	public void onBackPressed() {
		Activity parent = getParent();
		if (parent != null) {
			parent.onBackPressed();
		} else {
			super.onBackPressed();
		}
	}

}
