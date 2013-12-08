package org.weather.weatherman.activity;

import org.weather.weatherman.R;
import org.weather.weatherman.WeatherApplication;
import org.weather.weatherman.content.Weather;

import cn.seddat.weatherman.api.city.City;

import com.baidu.mobstat.StatService;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.OnTabChangeListener;

public class WeathermanActivity extends TabActivity {

	private final String tag = WeathermanActivity.class.getSimpleName();
	private TabHost tabHost;
	private WeatherApplication app;
	private String appName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		app = (WeatherApplication) getApplication();
		appName = getApplicationInfo().loadLabel(getPackageManager()).toString();
		// stats
		// StatService.setDebugOn(true);
		StatService.setLogSenderDelayed(3);// 启动后延迟3s发送统计日志
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
		tabHost.setOnTabChangedListener(new TabChangeListener());
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

	class TabChangeListener implements OnTabChangeListener {
		@Override
		public void onTabChanged(String tabId) {
			// stats
			String tabName = this.getTabName(tabId);
			StatService.onEvent(WeathermanActivity.this, "tabs", tabName, 1);
		}

		private String getTabName(String tag) {
			if (tag == null || tag.length() == 0) {
				return "unknown";
			} else if ("realtime".equalsIgnoreCase(tag)) {
				return getResources().getString(R.string.realtime);
			} else if ("trend".equalsIgnoreCase(tag)) {
				return getResources().getString(R.string.trend);
			} else if ("forecast".equalsIgnoreCase(tag)) {
				return getResources().getString(R.string.forecast);
			} else if ("setting".equalsIgnoreCase(tag)) {
				return getResources().getString(R.string.setting);
			} else {
				return "unknown";
			}
		}

	}

	@Override
	public void onBackPressed() {
		this.addShortcut();
		super.onBackPressed();
	}

	private void addShortcut() {
		SharedPreferences pref = getSharedPreferences(WeathermanActivity.class.getSimpleName(), Context.MODE_PRIVATE);
		if (pref.getBoolean("shortcut-installed", false)) {
			return;
		}
		Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, appName);
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
				Intent.ShortcutIconResource.fromContext(this, R.drawable.icon));
		intent.putExtra(
				Intent.EXTRA_SHORTCUT_INTENT,
				new Intent(this, WeathermanActivity.class).setAction("android.intent.action.MAIN").addCategory(
						"android.intent.category.LAUNCHER"));
		intent.putExtra("duplicate", false);
		this.sendBroadcast(intent);
		Editor editor = pref.edit();
		editor.putBoolean("shortcut-installed", true);
		editor.commit();
		Log.i(tag, "install shortcut for " + appName);
	}

}