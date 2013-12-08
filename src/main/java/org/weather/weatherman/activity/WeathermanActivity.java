package org.weather.weatherman.activity;

import org.weather.weatherman.R;
import org.weather.weatherman.WeatherApplication;
import org.weather.weatherman.content.Weather;

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
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import cn.seddat.weatherman.api.city.City;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mobstat.StatService;

public class WeathermanActivity extends TabActivity {

	private final String tag = WeathermanActivity.class.getSimpleName();
	private TabHost tabHost;
	private WeatherApplication app;
	private LocationClient locationClient;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		app = (WeatherApplication) getApplication();
		// stats
		// StatService.setDebugOn(true);
		StatService.setLogSenderDelayed(3);// 启动后延迟3s发送统计日志
		// location
		locationClient = new LocationClient(getApplicationContext());
		locationClient.setAK("tQHM3bNhLOkS0BFBRuzf8FQP");
		locationClient.registerLocationListener(new BDLocationListener() {

			@Override
			public void onReceivePoi(BDLocation location) {
				if (location == null)
					return;
				StringBuffer sb = new StringBuffer(256);
				sb.append("time : ");
				sb.append(location.getTime());
				sb.append("\nerror code : ");
				sb.append(location.getLocType());
				sb.append("\nlatitude : ");
				sb.append(location.getLatitude());
				sb.append("\nlontitude : ");
				sb.append(location.getLongitude());
				sb.append("\nradius : ");
				sb.append(location.getRadius());
				if (location.getLocType() == BDLocation.TypeGpsLocation) {
					sb.append("\nspeed : ");
					sb.append(location.getSpeed());
					sb.append("\nsatellite : ");
					sb.append(location.getSatelliteNumber());
				} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
					sb.append("\naddr : ");
					sb.append(location.getAddrStr());
				}
				Toast.makeText(getApplicationContext(), sb.toString(), Toast.LENGTH_LONG).show();
				Log.i(tag, sb.toString());
			}

			@Override
			public void onReceiveLocation(BDLocation arg0) {
			}
		});
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setAddrType("all");// 返回的定位结果包含地址信息
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
		option.setScanSpan(5000);// 设置发起定位请求的间隔时间为5000ms
		option.disableCache(true);// 禁止启用缓存定位
		option.setPoiNumber(5); // 最多返回POI个数
		option.setPoiDistance(1000); // poi查询距离
		option.setPoiExtraInfo(true); // 是否需要POI的电话和地址等详细信息
		locationClient.setLocOption(option);
		locationClient.start();
		locationClient.requestLocation();
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
		String appName = getApplicationInfo().loadLabel(getPackageManager()).toString();
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