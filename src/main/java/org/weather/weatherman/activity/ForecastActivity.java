package org.weather.weatherman.activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.baidu.mobstat.StatService;

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
		this.refreshData();
		// stats
		StatService.onResume(this);
	}

	/**
	 * @author gengmaozhang01
	 * @since 2014-1-25 下午6:06:02
	 */
	public void refreshData() {
		String city = (app.getCity() != null ? app.getCity().getId() : null);
		new ForecastTask().execute(city);
	}

	class ForecastTask extends AsyncTask<String, Integer, Cursor> {

		private DateFormat DF_1 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

		public ForecastTask() {
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// updateTime
			TextView view = (TextView) findViewById(R.id.updateTime);
			view.setText("--");
			// clear
			TableLayout layout = (TableLayout) view.getParent().getParent();
			layout.removeViews(1, layout.getChildCount() - 1);
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
			this.onPreExecute();
			onProgressUpdate(80);
			if (cursor != null && cursor.moveToFirst()) {
				// update time
				TextView uptimeView = (TextView) findViewById(R.id.updateTime);
				String text = cursor.getString(cursor.getColumnIndex(Weather.ForecastWeather.TIME));
				uptimeView.setText(text + "更新");
				// add row
				TableLayout layout = (TableLayout) uptimeView.getParent().getParent();
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
				ToastService.toastLong(getApplicationContext(), getResources().getString(R.string.connect_failed));
				Log.e(tag, "can't get forecast weather");
			}
			if (cursor != null) {
				cursor.close();
			}
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
