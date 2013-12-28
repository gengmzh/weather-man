package org.weather.weatherman.activity;

import java.util.List;

import org.weather.weatherman.R;
import org.weather.weatherman.WeatherApplication;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import cn.seddat.weatherman.api.city.City;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mobstat.StatService;

public class WeathermanActivity extends TabActivity implements OnTabChangeListener, BDLocationListener {

	private final String tag = WeathermanActivity.class.getSimpleName();
	private WeatherApplication app;
	private TabHost tabHost;
	private TextView cityView;
	private LocationClient locationClient;
	private CityResolver cityResolver;
	List<City> provinces;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		app = (WeatherApplication) getApplication();
		// 百度移动统计
		// StatService.setDebugOn(true);
		StatService.setLogSenderDelayed(3);// 启动后延迟3s发送统计日志
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
		tabSpec = tabHost.newTabSpec("forecast")
				.setIndicator(res.getString(R.string.forecast), res.getDrawable(R.drawable.icon_forecast))
				.setContent(new Intent().setClass(this, ForecastActivity.class));
		tabHost.addTab(tabSpec);
		tabSpec = tabHost.newTabSpec("trend")
				.setIndicator(res.getString(R.string.trend), res.getDrawable(R.drawable.icon_trend))
				.setContent(new Intent().setClass(this, TrendActivity.class));
		tabHost.addTab(tabSpec);
		tabSpec = tabHost.newTabSpec("setting")
				.setIndicator(res.getString(R.string.setting), res.getDrawable(R.drawable.icon_setting))
				.setContent(new Intent().setClass(this, SettingActivity.class));
		tabHost.addTab(tabSpec);
		// tabHost.setCurrentTab(0);
		tabHost.setOnTabChangedListener(this);
		// city
		cityView = (TextView) findViewById(R.id.city);
		cityView.getPaint().setFakeBoldText(true);
		if (app.getCity() != null) {
			cityView.setText(app.getCity().getName());
		}
		// 百度地图地位API
		locationClient = new LocationClient(getApplicationContext());
		locationClient.setAK("tQHM3bNhLOkS0BFBRuzf8FQP");
		locationClient.registerLocationListener(this);
		// 初始化城市信息
		cityResolver = new CityResolver(getContentResolver());
		provinces = cityResolver.findCity(null);
		if (provinces == null || provinces.isEmpty()) {
			try {
				cityResolver.initCity();
			} catch (Exception e) {
				Log.e(tag, "init city failed", e);
			}
			provinces = cityResolver.findCity(null);
		}
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

	@Override
	public void onTabChanged(String tabId) {
		String tabName = null;
		if (tabId == null || tabId.length() == 0) {
			tabName = "unknown";
		} else if ("realtime".equalsIgnoreCase(tabId)) {
			tabName = getResources().getString(R.string.realtime);
		} else if ("trend".equalsIgnoreCase(tabId)) {
			tabName = getResources().getString(R.string.trend);
		} else if ("forecast".equalsIgnoreCase(tabId)) {
			tabName = getResources().getString(R.string.forecast);
		} else if ("setting".equalsIgnoreCase(tabId)) {
			tabName = getResources().getString(R.string.setting);
		} else {
			tabName = "unknown";
		}
		// 统计各个Tab点击情况
		StatService.onEvent(WeathermanActivity.this, "tabs", tabName, 1);
	}

	@Override
	public void onReceiveLocation(BDLocation location) {
		if (location == null) {
			return;
		}
		Log.i(tag, "location: " + location.toJsonString());
		// Toast.makeText(this, location.toJsonString(),
		// Toast.LENGTH_LONG).show();
		City c1 = null, c2 = null, c3 = null;
		String province = location.getProvince(), city = location.getCity(), district = location.getDistrict();
		if (province == null || province.length() == 0 || city == null || city.length() == 0 || district == null
				|| district.length() == 0) {
			Log.e(tag, "get location failed");
		} else { // parse
			for (City prov : provinces) {
				if (province.contains(prov.getName()) || prov.getName().contains(province)) {
					c1 = prov;
					break;
				}
			}
			if (c1 != null) {
				List<City> cities = cityResolver.findCity(c1.getId());
				for (City cit : cities) {
					if (city.contains(cit.getName()) || cit.getName().contains(city)) {
						c2 = cit;
						break;
					}
				}
				if (c2 != null) {
					List<City> districts = cityResolver.findCity(c2.getId());
					for (City dis : districts) {
						if (district.contains(dis.getName()) || dis.getName().contains(district)) {
							c3 = dis;
							break;
						}
					}
				}
			}
		}
		// result
		if (c3 != null && c2 != null && c1 != null) {
			app.setCity(c3);
			cityView.setText(c3.getName());
			cityResolver.saveLocationSetting(c1, c2, c3);
			tabHost.setCurrentTab(0);
			// 停止定时定位
			LocationClientOption option = locationClient.getLocOption();
			option.setScanSpan(0);
			locationClient.setLocOption(option);
			if (locationClient.isStarted()) {
				locationClient.stop();
			}
		} else {
			tabHost.setCurrentTab(3);
		}
	}

	public void onReceivePoi(BDLocation location) {
		if (location == null) {
			return;
		}
		Log.i(tag, "poi: " + location.toJsonString());
	}

	protected void onStart() {
		super.onStart();
		if (app.getCity() == null) {
			if (!locationClient.isStarted()) {
				locationClient.start();
			}
			// option
			LocationClientOption option = new LocationClientOption();
			option.disableCache(true);// 禁止启用缓存定位
			option.setOpenGps(true);
			option.setAddrType("all");// 返回的定位结果包含地址信息
			option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
			option.setProdName("weather-man");
			option.setScanSpan(1000);// 设置发起定位请求的间隔时间为1000ms
			option.setPriority(LocationClientOption.NetWorkFirst);
			option.setPoiNumber(10); // 最多返回POI个数
			option.setPoiDistance(1000); // poi查询距离
			option.setPoiExtraInfo(true); // 是否需要POI的电话和地址等详细信息
			locationClient.setLocOption(option);
			// request
			locationClient.requestLocation();
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