package org.weather.weatherman.activity;

import org.weather.api.cn.city.City;
import org.weather.api.cn.city.CityTree;
import org.weather.weatherman.R;
import org.weather.weatherman.WeatherApplication;
import org.weather.weatherman.content.CityManager;
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
		// app
		app = (WeatherApplication) getApplication();
		CityManager cityManager = CityManager.getInstance();
		app.setCityTree(cityManager.readCityFile());
		City city = this.getDefaultCitycode();
		app.setCitycode(city.getId());
		// city view
		TextView cityView = (TextView) findViewById(R.id.city);
		cityView.getPaint().setFakeBoldText(true);
		cityView.setText(city.getName());
		app.setCityView(cityView);
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
		tabHost.setCurrentTab(0);
	}

	City getDefaultCitycode() {
		String citycode = null;
		Cursor cursor = getContentResolver().query(Weather.Setting.CONTENT_URI, null, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			citycode = cursor.getString(cursor.getColumnIndex(Weather.Setting.CITY3));
		}
		CityTree tree = app.getCityTree();
		if (citycode != null && citycode.length() > 0) {
			return tree.findCity(citycode);
		}
		return tree.getProvince().get(0).getChildren().get(0);
	}

//	class CitySpan extends URLSpan {
//
//		public CitySpan(String url) {
//			super(url);
//		}
//
//		@Override
//		public void onClick(View widget) {
//			String citycode = getURL();
//			app.setCitycode(citycode);
//			// refresh
//			Context context = tabHost.getCurrentView().getContext();
//			if (context instanceof RealtimeActivity) {
//				RealtimeActivity realtime = (RealtimeActivity) context;
//				realtime.refresh();
//			} else if (context instanceof ForecastActivity) {
//				ForecastActivity forecast = (ForecastActivity) context;
//				forecast.refresh();
//			} else {
//				Log.i(WeathermanActivity.class.getSimpleName(), "context " + context + " is illegal");
//			}
//		}
//
//	}

}