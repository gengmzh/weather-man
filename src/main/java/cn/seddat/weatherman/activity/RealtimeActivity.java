package cn.seddat.weatherman.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import cn.seddat.weatherman.R;
import cn.seddat.weatherman.WeathermanApplication;
import cn.seddat.weatherman.content.Weather;
import cn.seddat.weatherman.content.WeatherService;

import com.baidu.mobstat.StatService;

public class RealtimeActivity extends Activity {

	private static final String tag = RealtimeTask.class.getSimpleName();

	private WeathermanApplication app;
	private WeatherService weatherService;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.realtime);
		app = (WeathermanApplication) getApplication();
		weatherService = new WeatherService(this);
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
		new AQITask().execute(city);
	}

	class RealtimeTask extends AsyncTask<String, Integer, Weather.RealtimeWeather> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// updateTime
			TextView view = (TextView) findViewById(R.id.updateTime);
			view.setText("--");
			// temperature
			view = (TextView) findViewById(R.id.temperatue);
			view.setText("--");
			// wind
			view = (TextView) findViewById(R.id.wind);
			view.setText("--");
			// humidity
			view = (TextView) findViewById(R.id.humidity);
			view.setText("--");
			// living index
			TableLayout layout = (TableLayout) view.getParent().getParent();
			layout.removeViews(5, layout.getChildCount() - 5);
		}

		@Override
		protected Weather.RealtimeWeather doInBackground(String... params) {
			onProgressUpdate(0);
			String city = (params != null && params.length > 0 ? params[0] : null);
			if (city == null || city.length() == 0) {
				return null;
			}
			onProgressUpdate(20);
			Weather.RealtimeWeather realtime = weatherService.findRealtimeWeather(city);
			onProgressUpdate(60);
			return realtime;
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
		protected void onPostExecute(Weather.RealtimeWeather realtime) {
			this.onPreExecute();
			super.onPostExecute(realtime);
			onProgressUpdate(80);
			if (realtime != null) {
				// updateTime
				TextView view = (TextView) findViewById(R.id.updateTime);
				view.setText("天气实况，" + realtime.getTime() + "更新");
				// temperature
				view = (TextView) findViewById(R.id.temperatue);
				view.setText(realtime.getTemperature());
				// wind
				view = (TextView) findViewById(R.id.wind);
				view.setText(realtime.getWindDirection() + "，" + realtime.getWindForce());
				// humidity
				view = (TextView) findViewById(R.id.humidity);
				view.setText("湿度" + realtime.getHumidity());
				// living index
				TableLayout layout = (TableLayout) view.getParent().getParent();
				for (int i = 0; i < realtime.getIndexSize(); i++) {
					TableRow row = new TableRow(layout.getContext());
					view = new TextView(layout.getContext());
					view.setText(realtime.getIndexName(i) + "：");
					row.addView(view);
					view = new TextView(layout.getContext());
					view.setText(realtime.getIndexValue(i) + "。" + realtime.getIndexDesc(i));
					row.addView(view);
					layout.addView(row);
				}
			} else {
				ToastService.toastLong(getApplicationContext(), getResources().getString(R.string.connect_failed));
				Log.e(tag, "can't get realtime weather");
			}
			onProgressUpdate(100);
		}
	}

	class AQITask extends AsyncTask<String, Integer, Weather.AirQualityIndex> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			TextView view = (TextView) findViewById(R.id.AQI);
			view.setText("--");
		}

		@Override
		protected Weather.AirQualityIndex doInBackground(String... params) {
			String city = (params != null && params.length > 0 ? params[0] : null);
			if (city == null || city.length() == 0) {
				return null;
			}
			return weatherService.findAirQualityIndex(city);
		}

		@Override
		protected void onPostExecute(Weather.AirQualityIndex aqi) {
			this.onPreExecute();
			super.onPostExecute(aqi);
			if (aqi != null) {
				TextView view = (TextView) findViewById(R.id.AQI);
				int value = aqi.getCurrentAQI();
				if (value >= 0) {
					String text = "指数" + value + "，" + Weather.AirQualityIndex.getAQITitle(value);
					view.setText(text);
					int color = getResources().getColor(Weather.AirQualityIndex.getAQIColor(value));
					view.setTextColor(color);
				} else {
					view.setText("--");
				}
			} else {
				ToastService.toastLong(getApplicationContext(), getResources().getString(R.string.AQI_request_failed));
				Log.e(tag, "can't get AQI");
			}
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
