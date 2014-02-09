/**
 * 
 */
package org.weather.weatherman.activity;

import java.util.HashMap;
import java.util.Map;

import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.weather.weatherman.R;
import org.weather.weatherman.WeatherApplication;
import org.weather.weatherman.achartengine.LineChartFactory;
import org.weather.weatherman.achartengine.MyXYSeries;
import org.weather.weatherman.achartengine.MyXYSeriesRenderer;
import org.weather.weatherman.content.Weather;

import android.app.Activity;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.baidu.mobstat.StatService;

/**
 * @author gengmaozhang01
 * @since 2014-2-8 下午9:13:21
 */
public class AQIActivity extends Activity {

	private static final String tag = AQITask.class.getSimpleName();

	private WeatherApplication app;
	private LinearLayout container;
	private Resources resources;
	private String aqiTag = "hourly";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aqi);
		app = (WeatherApplication) getApplication();
		container = (LinearLayout) findViewById(R.id.aqiContainer);
		resources = getResources();
		// event
		RadioGroup aqiSelector = (RadioGroup) findViewById(R.id.AQI_selector);
		aqiSelector.setOnCheckedChangeListener(new AQISelectorListener());
	}

	class AQISelectorListener implements RadioGroup.OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (group.getCheckedRadioButtonId()) {
			case R.id.AQI_hourly:
				aqiTag = "hourly";
				break;
			case R.id.AQI_daily:
				aqiTag = "daily";
				break;
			default:
				break;
			}
			ProgressBar progressBar = (ProgressBar) getParent().findViewById(R.id.progressBar);
			progressBar.setVisibility(View.VISIBLE);
			refreshData();
		}

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
	 * @since 2014-1-25 下午6:06:22
	 */
	public void refreshData() {
		String city = (app.getCity() != null ? app.getCity().getId() : null);
		new AQITask().execute(city);
	}

	private String getAQITitle(int value) {
		String text = null;
		if (value <= 50) {
			text = "优";
		} else if (value <= 100) {
			text = "良";
		} else if (value <= 150) {
			text = "轻度污染";
		} else if (value <= 200) {
			text = "中度污染";
		} else if (value <= 300) {
			text = "重度污染";
		} else {
			text = "严重污染";
		}
		return text;
	}

	private int getAQIColor(int value) {
		int id = R.color.AQI_perfect;
		if (value <= 50) {
			id = R.color.AQI_perfect;
		} else if (value <= 100) {
			id = R.color.AQI_fine;
		} else if (value <= 150) {
			id = R.color.AQI_smell_little;
		} else if (value <= 200) {
			id = R.color.AQI_smell_middle;
		} else if (value <= 300) {
			id = R.color.AQI_smell_heavy;
		} else {
			id = R.color.AQI_smell_fatal;
		}
		return getResources().getColor(id);
	}

	class AQITask extends AsyncTask<String, Integer, Cursor> {

		public AQITask() {
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
		protected Cursor doInBackground(String... params) {
			onProgressUpdate(0);
			String city = (params != null && params.length > 0 ? params[0] : null);
			if (city == null || city.length() == 0) {
				return null;
			}
			onProgressUpdate(20);
			Uri uri = Uri.withAppendedPath(Weather.AirQualityIndex.CONTENT_URI, city);
			onProgressUpdate(40);
			Cursor cursor = getContentResolver().query(uri, null, null, null, null);
			onProgressUpdate(60);
			return cursor;
		}

		@Override
		protected void onPostExecute(Cursor cursor) {
			super.onPostExecute(cursor);
			// clear
			if (container.getChildCount() > 0) {
				container.removeViews(0, container.getChildCount());
			}
			onProgressUpdate(70);
			// 当前AQI
			if (cursor != null && cursor.moveToFirst()) {
				do {
					String tag = cursor.getString(cursor.getColumnIndex(Weather.AirQualityIndex.TAG));
					if ("current".equalsIgnoreCase(tag)) {
						String time = cursor.getString(cursor.getColumnIndex(Weather.AirQualityIndex.TIME));
						TextView view = (TextView) findViewById(R.id.updateTime);
						if (time != null && time.length() > 0) {
							view.setText(time + "更新");
						} else {
							view.setText("--");
						}
						int value = cursor.getInt(cursor.getColumnIndex(Weather.AirQualityIndex.AQI));
						view = (TextView) findViewById(R.id.AQI);
						if (value >= 0) {
							String text = String.valueOf(value) + ", " + getAQITitle(value);
							view.setText(text);
							view.setTextColor(getAQIColor(value));
						} else {
							view.setText("--");
						}
						break;
					}
				} while (cursor.moveToNext());
			}
			// dataSet
			XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();
			Map<Double, String> xlabels = new HashMap<Double, String>();
			MyXYSeries aqiSeries = new MyXYSeries(resources.getString(R.string.AQI_series_name));
			if (cursor != null && cursor.moveToFirst()) {
				double i = 0;
				do {
					String tag = cursor.getString(cursor.getColumnIndex(Weather.AirQualityIndex.TAG));
					if (aqiTag == null || aqiTag.equalsIgnoreCase(tag)) {
						String time = cursor.getString(cursor.getColumnIndex(Weather.AirQualityIndex.TIME));
						String value = cursor.getString(cursor.getColumnIndex(Weather.AirQualityIndex.AQI));
						if ("hourly".equalsIgnoreCase(tag)) {
							time = time.substring(8);
						} else if ("daily".equalsIgnoreCase(tag)) {
							time = time.substring(5);
						}
						xlabels.put(++i, time);
						aqiSeries.add(i, Double.parseDouble(value));
					}
				} while (cursor.moveToNext());
			} else {
				ToastService.toastLong(getApplicationContext(), getResources().getString(R.string.connect_failed));
				Log.e(tag, "can't get forecast weather");
			}
			dataSet.addSeries(aqiSeries);
			onProgressUpdate(80);
			// render
			XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
			// renderer.setChartTitle(res.getString(R.string.trend_title));
			// renderer.setAxisTitleTextSize(16);
			// renderer.setChartTitleTextSize(20);
			// renderer.setLabelsTextSize(15);
			// renderer.setLegendTextSize(15);
			renderer.setApplyBackgroundColor(true);
			renderer.setBackgroundColor(Color.WHITE);
			renderer.setMarginsColor(Color.WHITE);
			renderer.setPointSize(4f);
			renderer.setAxesColor(Color.DKGRAY);
			renderer.setXLabelsColor(Color.BLACK);
			// renderer.setXTitle("日期");
			renderer.setYLabelsColor(0, Color.BLACK);
			renderer.setYLabelsAlign(Align.RIGHT);
			// renderer.setYTitle("温度");
			renderer.setMargins(new int[] { 20, 20, 0, 10 });
			// renderer.setShowGrid(true);
			// renderer.setGridColor(Color.RED);
			renderer.setXAxisMin(aqiSeries.getMinX() - 0.5);
			renderer.setXAxisMax(aqiSeries.getMaxX() + 0.5);
			for (Double x : xlabels.keySet()) {
				if (aqiTag == null || ("hourly".equalsIgnoreCase(aqiTag) && x % 3 == 1.0)
						|| ("daily".equalsIgnoreCase(aqiTag) && x % 2 == 1.0)) {
					renderer.addXTextLabel(x, xlabels.get(x));
				}
			}
			renderer.setXLabels(0);
			renderer.setYAxisMax(aqiSeries.getMaxY() + 10);
			renderer.setYAxisMin(aqiSeries.getMinY() - 10);
			int step = (int) (renderer.getYAxisMax() - renderer.getYAxisMin());
			step = (step < 50 ? 5 : (step < 100 ? 100 : 20));
			for (int y = 0; y <= aqiSeries.getMaxY() + 10; y += step) {
				renderer.addYTextLabel(y, String.valueOf(y));
			}
			renderer.setYLabels(0);
			// renderer.setXTitle(res.getString(R.string.trend_x_title));
			renderer.setZoomButtonsVisible(true);
			renderer.setPanLimits(new double[] { -10, 20, -10, 40 });
			renderer.setZoomLimits(new double[] { -10, 20, -10, 40 });
			renderer.setZoomRate(1.05f);
			MyXYSeriesRenderer aqiRender = new MyXYSeriesRenderer();
			aqiRender.setColor(Color.BLUE);
			aqiRender.setDisplayChartValues(true);
			aqiRender.setPointStyle(PointStyle.DIAMOND);
			aqiRender.setFillPoints(true);
			for (int i = 0; i < aqiSeries.getItemCount(); i++) {
				aqiRender.setPointColor(i, getAQIColor((int) aqiSeries.getY(i)));
			}
			renderer.addSeriesRenderer(aqiRender);
			onProgressUpdate(90);
			// show
			GraphicalView chart = LineChartFactory.getLineChartView(container.getContext(), dataSet, renderer);
			chart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.FILL_PARENT));
			container.addView(chart);
			// clear
			if (cursor != null) {
				cursor.close();
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
