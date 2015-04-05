package cn.seddat.weatherman.activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import cn.seddat.weatherman.R;
import cn.seddat.weatherman.WeathermanApplication;
import cn.seddat.weatherman.content.Weather;
import cn.seddat.weatherman.content.WeatherService;

import com.baidu.mobstat.StatService;

/**
 * @since 2012-5-18
 * @author gmz
 * 
 */
public class ForecastActivity extends Activity {

	private static final String tag = ForecastActivity.class.getSimpleName();

	private WeathermanApplication app;
	private WeatherService weatherService;

	// private DomobAdView adView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.forecast);
		app = (WeathermanApplication) getApplication();
		weatherService = new WeatherService(this);
		// ad
		/**
		 * RelativeLayout adContainer = (RelativeLayout) findViewById(R.id.ad_container); adView = new DomobAdView(this,
		 * WeathermanApplication.DOMOB_PUBLISHER_ID, WeathermanApplication.DOMOB_PPID_MAIN,
		 * DomobAdView.INLINE_SIZE_FLEXIBLE, true); adView.setAdEventListener(new MainAdEventListener());
		 * RelativeLayout.LayoutParams params = new
		 * RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
		 * RelativeLayout.LayoutParams.WRAP_CONTENT); params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		 * adView.setLayoutParams(params); adContainer.addView(adView);
		 */
	}

	/**
	 * class MainAdEventListener implements DomobAdEventListener {
	 * 
	 * @Override public void onDomobAdReturned(DomobAdView arg0) {// 请求广告成功返回 StatService.onEvent(ForecastActivity.this,
	 *           "ad", "returned", 1); Log.i(tag, "ad returned"); }
	 * @Override public void onDomobAdFailed(DomobAdView arg0, ErrorCode arg1) { // 请求广告失败
	 *           StatService.onEvent(ForecastActivity.this, "ad", "failed", 1); Log.i(tag, "ad failed"); }
	 * @Override public void onDomobAdOverlayPresented(DomobAdView arg0) {// Loading Page成功
	 *           StatService.onEvent(ForecastActivity.this, "ad", "overlayPresented", 1); Log.i(tag,
	 *           "ad overlay presented"); }
	 * @Override public void onDomobAdOverlayDismissed(DomobAdView arg0) {// Loading Page关闭
	 *           StatService.onEvent(ForecastActivity.this, "ad", "overlayDismissed", 1); Log.i(tag,
	 *           "ad overlay dismissed"); }
	 * @Override public void onDomobAdClicked(DomobAdView arg0) { StatService.onEvent(ForecastActivity.this, "ad",
	 *           "clicked", 1); Log.i(tag, "ad clicked"); }
	 * @Override public void onDomobLeaveApplication(DomobAdView arg0) { StatService.onEvent(ForecastActivity.this,
	 *           "ad", "leaveApplication", 1); Log.i(tag, "leave application"); }
	 * @Override public Context onDomobAdRequiresCurrentContext() { return ForecastActivity.this; }
	 * 
	 *           }
	 */

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		ProgressBar progressBar = (ProgressBar) getParent().findViewById(R.id.progressBar);
		progressBar.setVisibility(View.VISIBLE);
		this.setUpdateTime("--");
		this.refreshData();
		/*
		 * if (this.adView != null) { this.adView.requestRefreshAd(); }
		 */
		// stats
		StatService.onResume(this);
	}

	/**
	 * @author gengmaozhang01
	 * @since 2014-1-25 下午6:06:02
	 */
	public void refreshData() {
		String city = (app.getCity() != null ? app.getCity().getId() : null);
		new RealtimeTask().execute(city);
		new ForecastTask().execute(city);
		new AQITask().execute(city);
	}

	private void setUpdateTime(String time) {
		TextView view = (TextView) findViewById(R.id.updateTime);
		if (time == null || "--".equals(time)) {
			view.setText("--");
		} else {
			String text = view.getText().toString();
			Log.i(tag, text + ", " + time);
			if (text.length() >= 23) {
				Log.i(tag, text.substring(5, 21));
				if (text.substring(5, 21).compareTo(time) < 0) {
					view.setText("天气预报，" + time + "更新");
				}
			} else {
				view.setText("天气预报，" + time + "更新");
			}
		}
	}

	class RealtimeTask extends AsyncTask<String, Integer, Weather.RealtimeWeather> {

		private final TextView weatherView = (TextView) findViewById(R.id.rt_weather);
		private final TableLayout indexView = (TableLayout) findViewById(R.id.li_today);

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// weather
			weatherView.setText("--");
			// index
			indexView.removeAllViews();
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
			super.onPostExecute(realtime);
			this.onProgressUpdate(80);
			if (realtime == null) {
				ToastService.toastLong(getApplicationContext(), getResources().getString(R.string.rt_request_failed));
				Log.e(tag, "can't get realtime weather");
				return;
			}
			this.onPreExecute();
			// updateTime
			ForecastActivity.this.setUpdateTime(realtime.getTime());
			// weather
			weatherView.setText(realtime.getTemperature() + "，" + realtime.getWindDirection() + realtime.getWindForce()
					+ "，湿度" + realtime.getHumidity());
			// index
			for (int i = 0; i < realtime.getIndexSize(); i++) {
				TableRow row = new TableRow(indexView.getContext());
				TextView view = new TextView(indexView.getContext());
				view.setText(realtime.getIndexName(i) + "：");
				row.addView(view);
				view = new TextView(indexView.getContext());
				view.setText(realtime.getIndexValue(i) + "。" + realtime.getIndexDesc(i));
				row.addView(view);
				indexView.addView(row);
			}
			this.onProgressUpdate(100);
		}
	}

	class ForecastTask extends AsyncTask<String, Integer, Weather.ForecastWeather> {

		private final TextView currentForcastView = (TextView) findViewById(R.id.fc_today);
		private final TableLayout futureForcastView = (TableLayout) findViewById(R.id.fc_future);
		private final LayoutInflater inflater = LayoutInflater.from(ForecastActivity.this);

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			currentForcastView.setText("--");
			futureForcastView.removeAllViews();
		}

		@Override
		protected Weather.ForecastWeather doInBackground(String... params) {
			String citycode = (params != null && params.length > 0 ? params[0] : null);
			if (citycode == null || citycode.length() == 0) {
				return null;
			}
			Weather.ForecastWeather forecast = weatherService.findForecastWeather(citycode);
			return forecast;
		}

		@Override
		protected void onPostExecute(Weather.ForecastWeather forecast) {
			super.onPostExecute(forecast);
			if (forecast == null || forecast.getForecastSize() < 7) {
				ToastService.toastLong(getApplicationContext(), getResources().getString(R.string.fc_request_failed));
				Log.e(tag, "can't get forecast weather");
				return;
			}
			this.onPreExecute();
			// update time
			ForecastActivity.this.setUpdateTime(forecast.getTime());
			// today weather
			currentForcastView.setText(forecast.getForecastWeather(0) + "，" + forecast.getForecastTemperature(0) + "，"
					+ forecast.getForecastWind(0) + "，" + forecast.getForecastWindForce(0));
			// forecast weather
			TableRow row = new TableRow(ForecastActivity.this);
			this.addForecast(row, forecast, 1);
			this.addForecast(row, forecast, 2);
			this.addForecast(row, forecast, 3);
			futureForcastView.addView(row);
			row = new TableRow(ForecastActivity.this);
			this.addForecast(row, forecast, 4);
			this.addForecast(row, forecast, 5);
			this.addForecast(row, forecast, 6);
			futureForcastView.addView(row);
		}

		private void addForecast(TableRow row, Weather.ForecastWeather forecast, int index) {
			LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.forecast_item, row, false);
			row.addView(layout);
			// date
			String date = forecast.getForecastTime(index);
			try {
				Calendar cal = Calendar.getInstance();
				DateFormat format = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
				cal.setTime(format.parse(date.substring(0, 10)));
				int week = cal.get(Calendar.DAY_OF_WEEK);
				date = date.substring(5, 10);
				if (week == Calendar.MONDAY) {
					date += "(一)";
				} else if (week == Calendar.TUESDAY) {
					date += "(二)";
				} else if (week == Calendar.WEDNESDAY) {
					date += "(三)";
				} else if (week == Calendar.THURSDAY) {
					date += "(四)";
				} else if (week == Calendar.FRIDAY) {
					date += "(五)";
				} else if (week == Calendar.SATURDAY) {
					date += "(六)";
				} else if (week == Calendar.SUNDAY) {
					date += "(日)";
				}
			} catch (Exception e) {
				Log.e(tag, "parse date failed", e);
			}
			TextView view = (TextView) layout.findViewById(R.id.fc_date);
			view.setText(date);
			// temperature
			view = (TextView) layout.findViewById(R.id.fc_temperature);
			view.setText(forecast.getForecastTemperature(index));
			// weather
			view = (TextView) layout.findViewById(R.id.fc_weather);
			view.setText(forecast.getForecastWeather(index));
			// wind
			view = (TextView) layout.findViewById(R.id.fc_wind);
			view.setText(forecast.getForecastWind(index) + "，" + forecast.getForecastWindForce(index));
		}
	}

	class AQITask extends AsyncTask<String, Integer, Weather.AirQualityIndex> {

		private final TextView aqiView = (TextView) findViewById(R.id.rt_aqi);

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			aqiView.setText("--");
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
			super.onPostExecute(aqi);
			if (aqi == null) {
				ToastService.toastLong(getApplicationContext(), getResources().getString(R.string.AQI_request_failed));
				Log.e(tag, "can't get AQI");
				return;
			}
			this.onPreExecute();
			int value = aqi.getCurrentAQI();
			if (value >= 0) {
				aqiView.setText(value + "，" + Weather.AirQualityIndex.getAQITitle(value));
				aqiView.setTextColor(getResources().getColor(Weather.AirQualityIndex.getAQIColor(value)));
			} else {
				aqiView.setText("--");
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
