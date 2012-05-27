package org.weather.weatherman.activity;

import org.weather.api.cn.city.City;
import org.weather.weatherman.R;
import org.weather.weatherman.WeatherApplication;
import org.weather.weatherman.content.Weather;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TextView;

public class WeathermanActivity extends TabActivity {

	private TabHost tabHost;
	private WeatherApplication app;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		app = (WeatherApplication) getApplication();
		// city
		TextView cityView = (TextView) findViewById(R.id.city);
		cityView.getPaint().setFakeBoldText(true);
		City city = this.getDefaultCitycode();
		if (city != null) {
			app.setCity(city);
			cityView.setText(city.getName());
		}
		// tab widget
		tabHost = getTabHost();
		Resources res = getResources();
		TabHost.TabSpec tabSpec = tabHost.newTabSpec("realtime").setIndicator(res.getString(R.string.realtime))
				.setContent(new Intent().setClass(this, RealtimeActivity.class));
		tabHost.addTab(tabSpec);
		tabSpec = tabHost.newTabSpec("forecast").setIndicator(res.getString(R.string.forecast))
				.setContent(new Intent().setClass(this, ForecastActivity.class));
		tabHost.addTab(tabSpec);
		tabSpec = tabHost.newTabSpec("setting").setIndicator(res.getString(R.string.setting))
				.setContent(new Intent().setClass(this, SettingActivity.class));
		tabHost.addTab(tabSpec);
		tabHost.setCurrentTab(city != null ? 0 : 2);
	}

	City getDefaultCitycode() {
		Cursor cursor = getContentResolver().query(Weather.Setting.CONTENT_URI, null, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			String id = cursor.getString(cursor.getColumnIndex(Weather.Setting.CITY3_CODE));
			String name = cursor.getString(cursor.getColumnIndex(Weather.Setting.CITY3_NAME));
			return new City(id, name);
		}
		return null;
	}

}