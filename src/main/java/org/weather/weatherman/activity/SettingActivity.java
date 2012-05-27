package org.weather.weatherman.activity;

import org.weather.api.cn.city.City;
import org.weather.api.cn.city.CityTree;
import org.weather.weatherman.R;
import org.weather.weatherman.WeatherApplication;
import org.weather.weatherman.content.CityManager;
import org.weather.weatherman.content.Weather;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * @author gmz
 * @time 2012-5-19
 */
public class SettingActivity extends Activity {

	private WeatherApplication app;
	private TextView cityView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		app = (WeatherApplication) getApplication();
		cityView = (TextView) getParent().findViewById(R.id.city);
		// city
		CityTree cityTree = CityManager.getInstance().readCityFile();
		Spinner city1Spinner = (Spinner) findViewById(R.id.city1);
		Spinner city2Spinner = (Spinner) findViewById(R.id.city2);
		Spinner city3Spinner = (Spinner) findViewById(R.id.city3);
		ArrayAdapter<City> city1Adapter = new ArrayAdapter<City>(this, android.R.layout.simple_spinner_item,
				cityTree.getProvince());
		city1Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		city1Spinner.setAdapter(city1Adapter);
		city1Spinner.setOnItemSelectedListener(new CityCascadingListener(city2Spinner));
		city2Spinner.setOnItemSelectedListener(new CityCascadingListener(city3Spinner));
		city3Spinner.setOnItemSelectedListener(new CitySelectionListener());
		// update time
		Spinner uptimeSpinner = (Spinner) findViewById(R.id.updateTimeSpinner);
		ArrayAdapter<CharSequence> updateAdapter = ArrayAdapter.createFromResource(this, R.array.updateTime,
				android.R.layout.simple_spinner_item);
		updateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		uptimeSpinner.setAdapter(updateAdapter);
		uptimeSpinner.setOnItemSelectedListener(new UpdatetimeSelectionListener());
		// reset
		Cursor cursor = getContentResolver().query(Weather.Setting.CONTENT_URI, null, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			String c1 = cursor.getString(cursor.getColumnIndex(Weather.Setting.CITY1_CODE));
			Log.i(SettingActivity.class.getSimpleName(), "city1: " + c1);
			this.selectCity(city1Spinner, c1);
			String c2 = cursor.getString(cursor.getColumnIndex(Weather.Setting.CITY2_CODE));
			Log.i(SettingActivity.class.getSimpleName(), "city2: " + c2);
			this.selectCity(city2Spinner, c2);
			String c3 = cursor.getString(cursor.getColumnIndex(Weather.Setting.CITY3_CODE));
			Log.i(SettingActivity.class.getSimpleName(), "city3: " + c3);
			this.selectCity(city3Spinner, c3);
			String updatetime = cursor.getString(cursor.getColumnIndex(Weather.Setting.UPTIME));
			Log.i(SettingActivity.class.getSimpleName(), "uptime: " + updatetime);
			if (updatetime != null && updatetime.length() > 0) {
				for (int i = 0; i < uptimeSpinner.getCount(); i++) {
					String hour = String.valueOf(uptimeSpinner.getItemAtPosition(i));
					if (hour.equals(updatetime)) {
						uptimeSpinner.setSelection(i);
					}
				}
			}
		} else {
			Log.e(SettingActivity.class.getSimpleName(), "can't find setting info");
		}
	}

	private void selectCity(Spinner spinner, String cityId) {
		if (spinner == null || cityId == null || cityId.length() == 0) {
			return;
		}
		for (int i = 0; i < spinner.getCount(); i++) {
			City city = (City) spinner.getItemAtPosition(i);
			if (city.getId().equals(cityId)) {
				spinner.setSelection(i, true);
			}
		}
	}

	class CityCascadingListener implements OnItemSelectedListener {

		private Spinner spinner;

		public CityCascadingListener(Spinner spinner) {
			super();
			this.spinner = spinner;
		}

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			City city = (City) arg0.getItemAtPosition(arg2);
			ArrayAdapter<City> adapter = new ArrayAdapter<City>(arg0.getContext(),
					android.R.layout.simple_spinner_item, city.getChildren());
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter);
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}

	}

	class CitySelectionListener implements OnItemSelectedListener {

		public CitySelectionListener() {
		}

		@Override
		public void onItemSelected(AdapterView<?> city3, View arg1, int arg2, long arg3) {
			Spinner city1 = (Spinner) findViewById(R.id.city1);
			Spinner city2 = (Spinner) findViewById(R.id.city2);
			City c1 = (City) city1.getSelectedItem(), c2 = (City) city2.getSelectedItem();
			City c3 = (City) city3.getItemAtPosition(arg2);
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

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

	class UpdatetimeSelectionListener implements OnItemSelectedListener {

		public UpdatetimeSelectionListener() {
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

}
