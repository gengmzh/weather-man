/**
 * 
 */
package org.weather.weatherman.activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.weather.weatherman.R;
import org.weather.weatherman.WeatherApplication;
import org.weather.weatherman.content.Weather;

import android.app.Activity;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * @author gmz
 * 
 */
public class TrendActivity extends Activity {

	private WeatherApplication app;
	private LinearLayout layout;
	private Resources res;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trend);
		app = (WeatherApplication) getApplication();
		layout = (LinearLayout) findViewById(R.id.trendContainer);
		res = getResources();
	}

	@Override
	protected void onResume() {
		super.onResume();
		ProgressBar progressBar = (ProgressBar) getParent().findViewById(R.id.progressBar);
		progressBar.setVisibility(View.VISIBLE);
		String city = (app.getCity() != null ? app.getCity().getId() : null);
		new TrendTask().execute(city);
	}

	class TrendTask extends AsyncTask<String, Integer, Cursor> {

		private DateFormat DF_1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		public TrendTask() {
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
				Log.i(TrendTask.class.getSimpleName(), progress + "/" + progressBar.getMax());
				progressBar.setProgress(progress);
				if (progress >= progressBar.getMax()) {
					progressBar.setVisibility(View.GONE);
				}
			}
		}

		@Override
		protected void onPostExecute(Cursor cursor) {
			super.onPostExecute(cursor);
			// clear
			if (layout.getChildCount() > 0) {
				layout.removeViews(0, layout.getChildCount());
			}
			onProgressUpdate(70);
			// dataSet
			XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();
			XYSeries tempSeries = new XYSeries(res.getString(R.string.trend_temperature));
			Map<Double, String> xlabels = new HashMap<Double, String>();
			if (cursor != null && cursor.moveToFirst()) {
				double i = 0;
				// update time
				String text = cursor.getString(cursor.getColumnIndex(Weather.ForecastWeather.TIME));
				Calendar cal = Calendar.getInstance();
				try {
					cal.setTime(DF_1.parse(text));
				} catch (Exception e) {
					Log.e(ForecastActivity.class.getSimpleName(), "parse update time failed", e);
				}
				cal.add(Calendar.HOUR_OF_DAY, -12);
				String prevDate = null;
				do {
					i++;
					// date
					cal.add(Calendar.HOUR_OF_DAY, 12);
					String date = (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.DAY_OF_MONTH);
					xlabels.put(i, (prevDate != null && prevDate.equals(date)) ? "" : date);
					prevDate = date;
					// temperature
					String tp = cursor.getString(cursor.getColumnIndex(Weather.ForecastWeather.TEMPERATURE));
					Double temp = Double.valueOf(tp.substring(0, tp.length() - 1));
					tempSeries.add(i, temp);
				} while (cursor.moveToNext());
				xlabels.put(++i, "");
			} else {
				Toast.makeText(getApplicationContext(), getResources().getText(R.string.connect_failed),
						Toast.LENGTH_LONG).show();
				Log.e(ForecastActivity.class.getName(), "can't get forecast weather");
			}
			dataSet.addSeries(tempSeries);
			onProgressUpdate(80);
			// render
			XYMultipleSeriesRenderer render = new XYMultipleSeriesRenderer();
			render.setChartTitle(res.getString(R.string.trend_title));
			render.setMargins(new int[] { 20, 10, 0, 10 });
			render.setShowGrid(true);
			for (Double x : xlabels.keySet()) {
				render.addXTextLabel(x, xlabels.get(x));
			}
			render.setXLabels(0);
			for (int t = 0; t <= tempSeries.getMaxY() + 5; t += 5) {
				render.addYTextLabel(t, t + "℃");
			}
			render.setYLabels(0);
			render.setXTitle(res.getString(R.string.trend_x_title));
			XYSeriesRenderer tempRender = new XYSeriesRenderer();
			tempRender.setColor(Color.YELLOW);
			tempRender.setDisplayChartValues(true);
			tempRender.setPointStyle(PointStyle.DIAMOND);
			render.addSeriesRenderer(tempRender);
			render.setZoomButtonsVisible(true);
			render.setPanLimits(new double[] { -10, 20, -10, 40 });
			render.setZoomLimits(new double[] { -10, 20, -10, 40 });
			render.setZoomRate(1.05f);
			onProgressUpdate(90);
			// show
			View chart = ChartFactory.getLineChartView(getApplicationContext(), dataSet, render);
			chart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.FILL_PARENT));
			layout.addView(chart);
			onProgressUpdate(100);
		}

	}

}
