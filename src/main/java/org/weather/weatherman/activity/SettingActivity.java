package org.weather.weatherman.activity;

import org.weather.api.cn.city.City;
import org.weather.api.cn.city.CityTree;
import org.weather.weatherman.R;
import org.weather.weatherman.WeatherApplication;
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

/**
 * @author gmz
 * @time 2012-5-19
 */
public class SettingActivity extends Activity {

	WeatherApplication app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		// city
		app = (WeatherApplication) getApplication();
		CityTree cityTree = app.getCityTree();
		Spinner city1Spinner = (Spinner) findViewById(R.id.city1);
		Spinner city2Spinner = (Spinner) findViewById(R.id.city2);
		Spinner city3Spinner = (Spinner) findViewById(R.id.city3);
		ArrayAdapter<City> adapter = new ArrayAdapter<City>(this, android.R.layout.simple_spinner_item,
				cityTree.getProvince());
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		city1Spinner.setAdapter(adapter);
		city1Spinner.setOnItemSelectedListener(new CityCascadingListener(city2Spinner));
		city2Spinner.setOnItemSelectedListener(new CityCascadingListener(city3Spinner));
		city3Spinner.setOnItemSelectedListener(new CitySelectionListener());
		// update time
		Spinner updateSpinner = (Spinner) findViewById(R.id.updateTimeSpinner);
		ArrayAdapter<CharSequence> updateAdapter = ArrayAdapter.createFromResource(this, R.array.updateTime,
				android.R.layout.simple_spinner_item);
		updateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		updateSpinner.setAdapter(updateAdapter);
		updateSpinner.setOnItemSelectedListener(new UpdatetimeSelectionListener());
		// reset
		Cursor cursor = getContentResolver().query(Weather.Setting.CONTENT_URI, null, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			String c1 = cursor.getString(cursor.getColumnIndex(Weather.Setting.CITY1));
			Log.i(SettingActivity.class.getSimpleName(), "city1: " + c1);
			this.selectCity(city1Spinner, c1);
			String c2 = cursor.getString(cursor.getColumnIndex(Weather.Setting.CITY2));
			Log.i(SettingActivity.class.getSimpleName(), "city2: " + c2);
			this.selectCity(city2Spinner, c2);
			String c3 = cursor.getString(cursor.getColumnIndex(Weather.Setting.CITY3));
			Log.i(SettingActivity.class.getSimpleName(), "city3: " + c3);
			this.selectCity(city3Spinner, c3);
			String updatetime = cursor.getString(cursor.getColumnIndex(Weather.Setting.UPDATETIME));
			Log.i(SettingActivity.class.getSimpleName(), "updatetime: " + updatetime);
			if (updatetime != null && updatetime.length() > 0) {
				for (int i = 0; i < updateSpinner.getCount(); i++) {
					String hour = String.valueOf(updateSpinner.getItemAtPosition(i));
					if (hour.equals(updatetime)) {
						updateSpinner.setSelection(i);
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
			values.put(Weather.Setting.CITY1, c1.getId());
			values.put(Weather.Setting.CITY2, c2.getId());
			values.put(Weather.Setting.CITY3, c3.getId());
			getContentResolver().update(Weather.Setting.CONTENT_URI, values, null, null);
			app.setCitycode(c3.getId());
			app.getCityView().setText(c3.getName());
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
			values.put(Weather.Setting.UPDATETIME, updatetime);
			getContentResolver().update(Weather.Setting.CONTENT_URI, values, null, null);
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

}
