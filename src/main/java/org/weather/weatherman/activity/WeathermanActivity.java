package org.weather.weatherman.activity;

import org.weather.api.cn.city.City;
import org.weather.weatherman.R;
import org.weather.weatherman.WeatherApplication;
import org.weather.weatherman.content.Weather;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
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
		// check update
//		DomobUpdater.checkUpdate(this, WeatherApplication.DOMOB_PUBLISHER_ID);
		// city
		TextView cityView = (TextView) findViewById(R.id.city);
		cityView.getPaint().setFakeBoldText(true);
		City city = this.getDefaultCitycode();
		if (city != null) {
			app.setCity(city);
			cityView.setText(city.getName());
		}
		// network
		// if (!checkNetwork()) {
		// Log.i(WeathermanActivity.class.getSimpleName(), "network not found");
		// Toast.makeText(getApplicationContext(),
		// getResources().getText(R.string.network_disconnected),
		// Toast.LENGTH_LONG).show();
		// }
		// tab widget
		tabHost = getTabHost();
		Resources res = getResources();
		TabHost.TabSpec tabSpec = tabHost.newTabSpec("realtime")
				.setIndicator(res.getString(R.string.realtime), res.getDrawable(R.drawable.icon_realtime))
				.setContent(new Intent().setClass(this, RealtimeActivity.class));
		tabHost.addTab(tabSpec);
		tabSpec = tabHost.newTabSpec("trend")
				.setIndicator(res.getString(R.string.trend), res.getDrawable(R.drawable.icon_trend))
				.setContent(new Intent().setClass(this, TrendActivity.class));
		tabHost.addTab(tabSpec);
		tabSpec = tabHost.newTabSpec("forecast")
				.setIndicator(res.getString(R.string.forecast), res.getDrawable(R.drawable.icon_forecast))
				.setContent(new Intent().setClass(this, ForecastActivity.class));
		tabHost.addTab(tabSpec);
		tabSpec = tabHost.newTabSpec("setting")
				.setIndicator(res.getString(R.string.setting), res.getDrawable(R.drawable.icon_setting))
				.setContent(new Intent().setClass(this, SettingActivity.class));
		tabHost.addTab(tabSpec);
		tabHost.setCurrentTab(city != null ? 0 : 3);
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

	boolean checkNetwork() {
		ConnectivityManager conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		State state = conn.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
		if (state != null && (state == State.CONNECTED || state == State.CONNECTING)) {
			return true;
		}
		state = conn.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		if (state != null && (state == State.CONNECTED || state == State.CONNECTING)) {
			return true;
		}
		return false;
	}

}