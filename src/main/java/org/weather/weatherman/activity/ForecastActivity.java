package org.weather.weatherman.activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.weather.weatherman.R;
import org.weather.weatherman.WeatherApplication;
import org.weather.weatherman.content.Weather;

import com.baidu.mobstat.StatService;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @since 2012-5-18
 * @author gmz
 * 
 */
public class ForecastActivity extends Activity {

	private static final String tag = ForecastTask.class.getSimpleName();

	private WeatherApplication app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.forecast);
		app = (WeatherApplication) getApplication();
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
		new ForecastTask().execute(city);
		// stats
		StatService.onResume(this);
	}

	class ForecastTask extends AsyncTask<String, Integer, Cursor> {

		private DateFormat DF_1 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

		public ForecastTask() {
		}

		@Override
		protected Cursor doInBackground(String... params) {
			onProgressUpdate(0);
			String city = (params != null && params.length > 0 ? params[0] : null);
			if (city == null || city.length() == 0) {
				return null;
			}
			onProgressUpdate(20);
			Uri uri = Uri.withAppendedPath(Weather.ForecastWeather.CONTENT_URI, city);
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
				Log.i(tag, progress + "/" + progressBar.getMax());
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
			// clear old
			TextView uptimeView = (TextView) findViewById(R.id.updateTime);
			TableLayout layout = (TableLayout) uptimeView.getParent().getParent();
			layout.removeViews(1, layout.getChildCount() - 1);
			if (cursor != null && cursor.moveToFirst()) {
				// update time
				String text = cursor.getString(cursor.getColumnIndex(Weather.ForecastWeather.TIME));
				uptimeView.setText(text + "更新");
				// add row
				Calendar cal = Calendar.getInstance();
				try {
					cal.setTime(DF_1.parse(text));
				} catch (Exception e) {
					Log.e(tag, "parse update time failed", e);
				}
				cal.add(Calendar.HOUR_OF_DAY, -12);
				do {
					TableRow row = new TableRow(layout.getContext());
					// date
					cal.add(Calendar.HOUR_OF_DAY, 12);
					TextView view = new TextView(layout.getContext());
					view.setText(format(cal));
					row.addView(view);
					// image
					// view = new TextView(this);
					// view.setPadding(3, 3, 3, 3);
					// view.setTextSize(14);
					// view.setText(cursor.getString(cursor.getColumnIndex(Weather.ForecastWeather.IMAGE)));
					// row.addView(view);
					// weather
					view = new TextView(layout.getContext());
					view.setText(cursor.getString(cursor.getColumnIndex(Weather.ForecastWeather.WEATHER)));
					row.addView(view);
					// temperature
					view = new TextView(layout.getContext());
					view.setText(cursor.getString(cursor.getColumnIndex(Weather.ForecastWeather.TEMPERATURE)));
					row.addView(view);
					// wind
					view = new TextView(layout.getContext());
					view.setText(cursor.getString(cursor.getColumnIndex(Weather.ForecastWeather.WIND)));
					row.addView(view);
					layout.addView(row);
				} while (cursor.moveToNext());
			} else {
				uptimeView.setText("--");
				Toast.makeText(getApplicationContext(), getResources().getText(R.string.connect_failed),
						Toast.LENGTH_LONG).show();
				Log.e(tag, "can't get forecast weather");
			}
			cursor.close();
			onProgressUpdate(100);
		}

		String format(Calendar date) {
			int day = date.get(Calendar.DAY_OF_MONTH);
			return (date.get(Calendar.MONTH) + 1) + "." + (day < 10 ? "0" : "") + day
					+ (date.get(Calendar.HOUR_OF_DAY) < 12 ? "白天" : "夜晚");
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
