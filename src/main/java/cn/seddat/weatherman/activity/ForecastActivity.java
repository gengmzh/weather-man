package cn.seddat.weatherman.activity;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import cn.domob.android.ads.DomobAdEventListener;
import cn.domob.android.ads.DomobAdView;
import cn.domob.android.ads.DomobAdManager.ErrorCode;
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
	private GridView gridView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.forecast);
		app = (WeathermanApplication) getApplication();
		weatherService = new WeatherService(this);
		// grid
		gridView = (GridView) findViewById(R.id.fc_container);
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			private int colorDefault = getResources().getColor(R.color.fc_grid_item_default);
			private int colorSelected = getResources().getColor(R.color.fc_grid_item_selected);

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				for (int i = 0; i < parent.getChildCount(); i++) {
					if (i != position) {
						parent.getChildAt(i).setBackgroundColor(colorDefault);
					}
				}
				view.setBackgroundColor(colorSelected);
			}
		});
		// ad
		RelativeLayout adContainer = (RelativeLayout) findViewById(R.id.ad_container);
		DomobAdView adView = new DomobAdView(this, WeathermanApplication.DOMOB_PUBLISHER_ID,
				WeathermanApplication.DOMOB_PPID_MAIN, DomobAdView.INLINE_SIZE_FLEXIBLE, true);
		adView.setAdEventListener(new MainAdEventListener());
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		adView.setLayoutParams(params);
		adContainer.addView(adView);
	}

	class MainAdEventListener implements DomobAdEventListener {

		@Override
		public void onDomobAdReturned(DomobAdView arg0) {// 请求广告成功返回
			StatService.onEvent(ForecastActivity.this, "ad", "returned", 1);
			Log.i(tag, "ad returned");
		}

		@Override
		public void onDomobAdFailed(DomobAdView arg0, ErrorCode arg1) { // 请求广告失败
			StatService.onEvent(ForecastActivity.this, "ad", "failed", 1);
			Log.i(tag, "ad failed");
		}

		@Override
		public void onDomobAdOverlayPresented(DomobAdView arg0) {// Loading Page成功
			StatService.onEvent(ForecastActivity.this, "ad", "overlayPresented", 1);
			Log.i(tag, "ad overlay presented");
		}

		@Override
		public void onDomobAdOverlayDismissed(DomobAdView arg0) {// Loading Page关闭
			StatService.onEvent(ForecastActivity.this, "ad", "overlayDismissed", 1);
			Log.i(tag, "ad overlay dismissed");
		}

		@Override
		public void onDomobAdClicked(DomobAdView arg0) {
			StatService.onEvent(ForecastActivity.this, "ad", "clicked", 1);
			Log.i(tag, "ad clicked");
		}

		@Override
		public void onDomobLeaveApplication(DomobAdView arg0) {
			StatService.onEvent(ForecastActivity.this, "ad", "leaveApplication", 1);
			Log.i(tag, "leave application");
		}

		@Override
		public Context onDomobAdRequiresCurrentContext() {
			return ForecastActivity.this;
		}

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
		this.setUpdateTime("--");
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
		new RealtimeTask().execute(city);
		new AQITask().execute(city);
		new ForecastTask().execute(city);
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

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// weather
			TextView view = (TextView) findViewById(R.id.rt_weather);
			view.setText("--");
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
				ForecastActivity.this.setUpdateTime(realtime.getTime());
				// weather
				TextView view = (TextView) findViewById(R.id.rt_weather);
				view.setText(realtime.getTemperature() + "，" + realtime.getWindDirection() + realtime.getWindForce()
						+ "，湿度" + realtime.getHumidity());
			} else {
				ToastService.toastLong(getApplicationContext(), getResources().getString(R.string.rt_request_failed));
				Log.e(tag, "can't get realtime weather");
			}
			onProgressUpdate(100);
		}
	}

	class AQITask extends AsyncTask<String, Integer, Weather.AirQualityIndex> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			TextView view = (TextView) findViewById(R.id.rt_aqi);
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
				TextView view = (TextView) findViewById(R.id.rt_aqi);
				int value = aqi.getCurrentAQI();
				if (value >= 0) {
					view.setText(value + "，" + Weather.AirQualityIndex.getAQITitle(value));
					view.setTextColor(getResources().getColor(Weather.AirQualityIndex.getAQIColor(value)));
				} else {
					view.setText("--");
				}
			} else {
				ToastService.toastLong(getApplicationContext(), getResources().getString(R.string.AQI_request_failed));
				Log.e(tag, "can't get AQI");
			}
		}
	}

	class ForecastTask extends AsyncTask<String, Integer, Weather.ForecastWeather> {

		public ForecastTask() {
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// clear
			if (gridView.getChildCount() > 0) {
				gridView.removeViews(0, gridView.getChildCount());
			}
		}

		@Override
		protected Weather.ForecastWeather doInBackground(String... params) {
			onProgressUpdate(0);
			String citycode = (params != null && params.length > 0 ? params[0] : null);
			if (citycode == null || citycode.length() == 0) {
				return null;
			}
			onProgressUpdate(20);
			Weather.ForecastWeather forecast = weatherService.findForecastWeather(citycode);
			onProgressUpdate(60);
			return forecast;
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
		protected void onPostExecute(Weather.ForecastWeather forecast) {
			this.onPreExecute();
			super.onPostExecute(forecast);
			onProgressUpdate(80);
			if (forecast != null) {
				// update time
				ForecastActivity.this.setUpdateTime(forecast.getTime());
				// add weather
				List<ForecastData> datas = new ArrayList<ForecastData>();
				for (int i = 0; i < forecast.getForecastSize(); i++) {
					ForecastData item = new ForecastData();
					datas.add(item);
					// 日期
					String date = forecast.getForecastTime(i);
					item.setDate(date);
					// 温度
					String temp = forecast.getForecastTemperature(i);
					if (temp != null && temp.length() > 0) {
						String[] temps = temp.split("~");
						item.setTemperature(temps[0], temps.length > 1 ? temps[1] : null);
					}
					// 天气
					item.setWeather(forecast.getForecastWeather(i), null);
					// 风力、风向
					item.setWind(forecast.getForecastWind(i), forecast.getForecastWindForce(i));
				}
				ForecastAdapter adapter = new ForecastAdapter(ForecastActivity.this, datas, R.layout.forecast_item,
						new String[] { ForecastData.KEY_DATE, ForecastData.KEY_TEMPERATURE, ForecastData.KEY_WEATHER,
								ForecastData.KEY_WIND }, new int[] { R.id.fc_date, R.id.fc_temperature,
								R.id.fc_weather, R.id.fc_wind });
				gridView.setAdapter(adapter);
			} else {
				ToastService.toastLong(getApplicationContext(), getResources().getString(R.string.fc_request_failed));
				Log.e(tag, "can't get forecast weather");
			}
			onProgressUpdate(100);
		}
	}

	class ForecastData extends HashMap<String, String> {

		private static final long serialVersionUID = -7768044685142672986L;
		static final String KEY_DATE = "fc_date", KEY_TEMPERATURE = "fc_temperature", KEY_WEATHER = "fc_weather",
				KEY_WIND = "fc_wind";

		public ForecastData() {
		}

		private String date;

		public String getDate() {
			return get(KEY_DATE);
		}

		public ForecastData setDate(String date) {
			String d = date.substring(5, 10);
			Calendar cal = Calendar.getInstance();
			try {
				DateFormat format = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
				cal.setTime(format.parse(date.substring(0, 10)));
				int week = cal.get(Calendar.DAY_OF_WEEK);
				if (week == Calendar.MONDAY) {
					d += "(一)";
				} else if (week == Calendar.TUESDAY) {
					d += "(二)";
				} else if (week == Calendar.WEDNESDAY) {
					d += "(三)";
				} else if (week == Calendar.THURSDAY) {
					d += "(四)";
				} else if (week == Calendar.FRIDAY) {
					d += "(五)";
				} else if (week == Calendar.SATURDAY) {
					d += "(六)";
				} else if (week == Calendar.SUNDAY) {
					d += "(日)";
				}
			} catch (ParseException e) {
				Log.e(tag, "parse date failed", e);
			}
			put(KEY_DATE, d);
			this.date = date;
			return this;
		}

		public boolean isSameDay(String date) {
			if (this.date != null && date != null) {
				return this.date.substring(0, 10).equals(date.substring(0, 10));
			}
			return false;
		}

		public String getTemperature() {
			return get(KEY_TEMPERATURE);
		}

		public ForecastData setTemperature(String low, String high) {
			String t = null;
			if (low != null && low.length() > 0) {
				t = low;
				if (high != null && high.length() > 0) {
					t = t.replace("℃", "") + " ~ " + high;
				}
			} else {
				t = (high != null ? high : "");
			}
			put(KEY_TEMPERATURE, t);
			return this;
		}

		public String getWeather() {
			return get(KEY_WEATHER);
		}

		public ForecastData setWeather(String low, String high) {
			String w = null;
			if (low != null && low.length() > 0) {
				if (high != null && high.length() > 0) {
					if (low.equals(high)) {
						w = low;
					} else if (!low.contains("转") && !high.contains("转")) {
						w = low + "转" + high;
					} else {
						w = low + "，夜间" + high;
					}
				} else {
					w = low;
				}
			} else {
				w = (high != null ? high : "");
			}
			put(KEY_WEATHER, w);
			return this;
		}

		public String getWind() {
			return get(KEY_WIND);
		}

		public ForecastData setWind(String wind, String force) {
			String w = null;
			if (wind != null && wind.length() > 0) {
				if (!"无持续风向".equals(wind)) {
					w = wind;
				}
			}
			if (force != null && force.length() > 0) {
				w = (w != null && w.length() > 0 ? w + "，" : "") + force;
			}
			put(KEY_WIND, w);
			return this;
		}

	}

	class ForecastAdapter extends SimpleAdapter {

		public ForecastAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from,
				int[] to) {
			super(context, data, resource, from, to);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			// height
			GridView grid = (GridView) parent;
			int number = this.getNumColumns(grid, 3);
			if (number <= 0 || (position + 1) % number != 0) {
				return view;
			}
			List<View> views = new ArrayList<View>();
			views.add(view);
			int maxHeight = view.getHeight();
			for (int i = 1; i < number; i++) {
				View v = grid.getChildAt(position - i);
				maxHeight = Math.max(maxHeight, v.getHeight());
				views.add(v);
			}
			for (View v : views) {
				if (v.getHeight() < maxHeight) {
					ViewGroup.LayoutParams params = v.getLayoutParams();
					params.height = maxHeight;
					v.setLayoutParams(params);
				}
			}
			return view;
		}

		private int getNumColumns(GridView gridView, int defaultNumber) {
			try {
				Field field = GridView.class.getDeclaredField("mNumColumns");
				field.setAccessible(true);
				Object count = field.get(gridView);
				return Integer.parseInt(count.toString());
			} catch (Exception ex) {
				Log.e(tag, "get numColumns failed", ex);
				return defaultNumber;
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
