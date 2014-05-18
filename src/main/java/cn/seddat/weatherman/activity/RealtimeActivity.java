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

	class RealtimeTask extends AsyncTask<String, Integer, Weather.RealtimeWeather> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// updateTime
			TextView view = (TextView) findViewById(R.id.updateTime);
			view.setText("--");
			// index
			TableLayout table = (TableLayout) findViewById(R.id.rt_index);
			table.removeAllViews();
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
				view.setText("生活指数，" + realtime.getTime() + "更新");
				// index
				TableLayout table = (TableLayout) findViewById(R.id.rt_index);
				for (int i = 0; i < realtime.getIndexSize(); i++) {
					TableRow row = new TableRow(table.getContext());
					view = new TextView(table.getContext());
					view.setText(realtime.getIndexName(i) + "：");
					row.addView(view);
					view = new TextView(table.getContext());
					view.setText(realtime.getIndexValue(i) + "。" + realtime.getIndexDesc(i));
					row.addView(view);
					table.addView(row);
				}
			} else {
				ToastService.toastLong(getApplicationContext(), getResources().getString(R.string.rt_request_failed));
				Log.e(tag, "can't get realtime weather");
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
