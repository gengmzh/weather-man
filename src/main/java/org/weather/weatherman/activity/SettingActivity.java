package org.weather.weatherman.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.weather.api.cn.city.City;
import org.weather.weatherman.R;
import org.weather.weatherman.WeatherApplication;
import org.weather.weatherman.content.Weather;
import org.weather.weatherman.content.WeatherContentProvider;

import cn.domob.android.ads.DomobUpdater;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * @author gmz
 * @time 2012-5-19
 */
public class SettingActivity extends Activity {

	private CityResolver cityResolver;
	private WeatherApplication app;
	private TextView cityView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		cityResolver = new CityResolver(getContentResolver());
		app = (WeatherApplication) getApplication();
		cityView = (TextView) getParent().findViewById(R.id.city);
		// register events
		Spinner city1Spinner = (Spinner) findViewById(R.id.city1);
		Spinner city2Spinner = (Spinner) findViewById(R.id.city2);
		Spinner city3Spinner = (Spinner) findViewById(R.id.city3);
		city1Spinner.setOnItemSelectedListener(new CitySelectedListener(city2Spinner));
		city2Spinner.setOnItemSelectedListener(new CitySelectedListener(city3Spinner));
		city3Spinner.setOnItemSelectedListener(new CitySelectedListener(null));
		Spinner uptimeSpinner = (Spinner) findViewById(R.id.updateTimeSpinner);
		uptimeSpinner.setOnItemSelectedListener(new UptimeSelectedListener());
		Button updater = (Button) findViewById(R.id.updater);
		updater.setOnClickListener(new UpdaterClickListener());
		// progress
		ProgressBar progressBar = (ProgressBar) getParent().findViewById(R.id.progressBar);
		progressBar.setVisibility(View.VISIBLE);
		new SettingTask().execute();
	}

	class SettingTask extends AsyncTask<String, Integer, Cursor> {

		private List<City> city1List;

		@Override
		protected Cursor doInBackground(String... params) {
			onProgressUpdate(0);
			// init city
			city1List = cityResolver.findCity(null);
			if (city1List == null || city1List.isEmpty()) {
				try {
					initCity();
				} catch (Exception e) {
					Log.e(WeatherContentProvider.class.getSimpleName(), "init city failed", e);
					return null;
				}
				city1List = cityResolver.findCity(null);
			}
			// reset
			Cursor cursor = getContentResolver().query(Weather.Setting.CONTENT_URI, null, null, null, null);
			onProgressUpdate(60);
			return cursor;
		}

		void initCity() throws IOException {
			InputStream ins = WeatherContentProvider.class.getClassLoader().getResourceAsStream(
					"org/weather/weatherman/activity/city.properties");
			BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
			City c1 = null, c2 = null;
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] ls = line.split("\t");
				if (ls.length < 2) {
					continue;
				}
				City tmp = new City(ls[0], ls[1]);
				ContentValues values = new ContentValues();
				values.put(Weather.City.CODE, tmp.getId());
				values.put(Weather.City.NAME, tmp.getName());
				if (tmp.getId().length() == 5) {
					c1 = tmp;
				} else if (tmp.getId().length() == 7) {
					c2 = tmp;
					values.put(Weather.City.PARENT, c1.getId());
				} else if (tmp.getId().length() == 9) {
					values.put(Weather.City.PARENT, c2.getId());
				}
				getContentResolver().insert(Weather.City.CONTENT_URI, values);
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			int progress = (values != null && values.length > 0 ? values[0] : 0);
			ProgressBar progressBar = (ProgressBar) getParent().findViewById(R.id.progressBar);
			if (progressBar != null) {
				Log.i(SettingTask.class.getSimpleName(), progress + "/" + progressBar.getMax());
				progressBar.setProgress(progress);
				if (progress >= progressBar.getMax()) {
					progressBar.setVisibility(View.GONE);
				}
			}
		}

		@Override
		protected void onPostExecute(Cursor cursor) {
			super.onPostExecute(cursor);
			// city
			Spinner city1Spinner = (Spinner) findViewById(R.id.city1);
			ArrayAdapter<City> city1Adapter = new ArrayAdapter<City>(getApplicationContext(),
					android.R.layout.simple_spinner_item, city1List);
			city1Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			city1Spinner.setAdapter(city1Adapter);
			onProgressUpdate(70);
			// update time
			Spinner uptimeSpinner = (Spinner) findViewById(R.id.updateTimeSpinner);
			ArrayAdapter<CharSequence> updateAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
					R.array.updateTime, android.R.layout.simple_spinner_item);
			updateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			uptimeSpinner.setAdapter(updateAdapter);
			onProgressUpdate(80);
			// reset
			if (cursor != null && cursor.moveToFirst()) {
				// reset city
				Spinner city2Spinner = (Spinner) findViewById(R.id.city2);
				Spinner city3Spinner = (Spinner) findViewById(R.id.city3);
				String c1 = cursor.getString(cursor.getColumnIndex(Weather.Setting.CITY1_CODE));
				Log.i(SettingActivity.class.getSimpleName(), "city1: " + c1);
				this.selectCity(city1Spinner, c1);
				String c2 = cursor.getString(cursor.getColumnIndex(Weather.Setting.CITY2_CODE));
				Log.i(SettingActivity.class.getSimpleName(), "city2: " + c2);
				this.selectCity(city2Spinner, c2);
				String c3 = cursor.getString(cursor.getColumnIndex(Weather.Setting.CITY3_CODE));
				Log.i(SettingActivity.class.getSimpleName(), "city3: " + c3);
				this.selectCity(city3Spinner, c3);
				onProgressUpdate(90);
				// reset uptime
				String updatetime = cursor.getString(cursor.getColumnIndex(Weather.Setting.UPTIME));
				Log.i(SettingActivity.class.getSimpleName(), "uptime: " + updatetime);
				if (updatetime != null && updatetime.length() > 0) {
					for (int i = 0; i < uptimeSpinner.getCount(); i++) {
						String hour = String.valueOf(uptimeSpinner.getItemAtPosition(i));
						if (hour.equals(updatetime)) {
							uptimeSpinner.setSelection(i);
							break;
						}
					}
				}
			} else {
				Log.e(SettingActivity.class.getSimpleName(), "can't find setting info");
			}
			onProgressUpdate(100);
		}

		private void selectCity(Spinner spinner, String cityId) {
			if (spinner == null || cityId == null || cityId.length() == 0) {
				return;
			}
			for (int i = 0; i < spinner.getCount(); i++) {
				City city = (City) spinner.getItemAtPosition(i);
				if (city.getId().equals(cityId)) {
					spinner.setSelection(i, true);
					break;
				}
			}
		}

	}

	class CitySelectedListener implements OnItemSelectedListener {

		private Spinner spinner;

		public CitySelectedListener(Spinner spinner) {
			super();
			this.spinner = spinner;
		}

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			City city = (City) arg0.getItemAtPosition(arg2);
			if (spinner != null) {
				ArrayAdapter<City> adapter = new ArrayAdapter<City>(arg0.getContext(),
						android.R.layout.simple_spinner_item, cityResolver.findCity(city.getId()));
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner.setAdapter(adapter);
			} else {
				Spinner city1 = (Spinner) findViewById(R.id.city1);
				Spinner city2 = (Spinner) findViewById(R.id.city2);
				City c1 = (City) city1.getSelectedItem(), c2 = (City) city2.getSelectedItem();
				City c3 = (City) arg0.getItemAtPosition(arg2);
				ContentValues values = new ContentValues();
				values.put(Weather.Setting.CITY1_CODE, c1.getId());
				values.put(Weather.Setting.CITY1_NAME, c1.getName());
				values.put(Weather.Setting.CITY2_CODE, c2.getId());
				values.put(Weather.Setting.CITY2_NAME, c2.getName());
				values.put(Weather.Setting.CITY3_CODE, c3.getId());
				values.put(Weather.Setting.CITY3_NAME, c3.getName());
				getContentResolver().update(Weather.Setting.CONTENT_URI, values, null, null);
				app.setCity(c3);
				cityView.setText(c3.getName());
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}

	}

	class UptimeSelectedListener implements OnItemSelectedListener {

		public UptimeSelectedListener() {
		}

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			String updatetime = String.valueOf(arg0.getItemAtPosition(arg2));
			ContentValues values = new ContentValues();
			values.put(Weather.Setting.UPTIME, updatetime);
			getContentResolver().update(Weather.Setting.CONTENT_URI, values, null, null);
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

	class UpdaterClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			DomobUpdater.checkUpdate(getApplicationContext(), WeatherApplication.DOMOB_PUBLISHER_ID);
		}
	}

}
