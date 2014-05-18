package cn.seddat.weatherman.activity;

import java.util.List;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import cn.seddat.weatherman.R;
import cn.seddat.weatherman.WeathermanApplication;
import cn.seddat.weatherman.api.city.City;
import cn.seddat.weatherman.content.SettingService;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mobstat.StatService;

public class WeathermanActivity extends TabActivity {

	private static final String tag = WeathermanActivity.class.getSimpleName();
	private WeathermanApplication app;
	private String appName;

	private SettingService settingService;

	private TabHost tabHost;
	private TextView cityView;
	private LocationClient locationClient;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		this.setContentView(R.layout.main);
		this.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.main_title);
		// title
		app = (WeathermanApplication) getApplication();
		appName = getApplicationInfo().loadLabel(getPackageManager()).toString();
		settingService = new SettingService(this);
		// 百度移动统计
		// StatService.setDebugOn(true);
		StatService.setLogSenderDelayed(3);// 启动后延迟3s发送统计日志
		// tab widget
		tabHost = getTabHost();
		final String[] tags = new String[] { "forecast", "realtime", "trend", "aqi" };
		final int[] labels = new int[] { R.string.forecast, R.string.realtime, R.string.trend, R.string.AQI_title };
		final Class<?>[] tagets = new Class<?>[] { ForecastActivity.class, RealtimeActivity.class, TrendActivity.class,
				AQIActivity.class };
		final Resources res = getResources();
		final LayoutInflater inflater = LayoutInflater.from(this);
		final int defaultTab = 0;
		for (int i = 0, size = tags.length; i < size; i++) {
			View tab = inflater.inflate(R.layout.main_tab_indicator, getTabWidget(), false);
			TextView title = (TextView) tab.findViewById(android.R.id.title);
			title.setText(res.getString(labels[i]));
			if (i == defaultTab) {
				title.setTextColor(res.getColor(R.color.main_tab_selected));
			}
			TabHost.TabSpec tabSpec = tabHost.newTabSpec(tags[i]).setIndicator(tab)
					.setContent(new Intent().setClass(this, tagets[i]));
			tabHost.addTab(tabSpec);
		}
		tabHost.setCurrentTab(defaultTab);
		tabHost.setOnTabChangedListener(new WeatherTabChangeListener());
		// init city
		cityView = (TextView) findViewById(R.id.city);
		cityView.getPaint().setFakeBoldText(true);
		if (app.getCity() != null) {
			cityView.setText(app.getCity().getName());
		}
		// 切换城市点击事件
		CityPromptClickListener listener = new CityPromptClickListener();
		ImageView cityPrompt = (ImageView) findViewById(R.id.cityPrompt);
		cityPrompt.setOnClickListener(listener);
		TextView cityHint = (TextView) findViewById(R.id.cityHint);
		cityHint.setOnClickListener(listener);
		// 百度地图地位API
		locationClient = new LocationClient(getApplicationContext());
		locationClient.setAK("tQHM3bNhLOkS0BFBRuzf8FQP");
		locationClient.registerLocationListener(new BaiduLocationListener());
	}

	@Override
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
			option.setScanSpan(1000);// 设置发起定位请求的间隔时间为1000ms，大于等于1000是定时定位
			option.setPriority(LocationClientOption.NetWorkFirst);
			option.setPoiNumber(10); // 最多返回POI个数
			option.setPoiDistance(1000); // poi查询距离
			option.setPoiExtraInfo(true); // 是否需要POI的电话和地址等详细信息
			locationClient.setLocOption(option);
			// request
			locationClient.requestLocation();
			// 统计切换城市事件
			StatService.onEvent(this, "city-setting", "location-automatically", 1);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {// 修改城市结果
			if (resultCode == 1) {
				City c1 = new City(data.getStringExtra("city1Id"), data.getStringExtra("city1Name"));
				City c2 = new City(data.getStringExtra("city2Id"), data.getStringExtra("city2Name"));
				City c3 = new City(data.getStringExtra("city3Id"), data.getStringExtra("city3Name"));
				this.resetCity(c1, c2, c3);
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void resetCity(City province, City city, City district) {
		Log.i(tag, "reset city to " + district);
		settingService.saveSetting(province, city, district);
		app.setCity(district);
		cityView.setText(district.getName());
		// refresh data
		ProgressBar progressBar = (ProgressBar) this.findViewById(R.id.progressBar);
		progressBar.setVisibility(View.VISIBLE);
		Activity activity = getLocalActivityManager().getActivity(tabHost.getCurrentTabTag());
		if (RealtimeActivity.class.isInstance(activity)) {
			((RealtimeActivity) activity).refreshData();
		} else if (ForecastActivity.class.isInstance(activity)) {
			((ForecastActivity) activity).refreshData();
		} else if (TrendActivity.class.isInstance(activity)) {
			((TrendActivity) activity).refreshData();
		} else {
			Log.e(tag, "unknown activity " + (activity != null ? activity.getClass().getName() : "null"));
		}
		// 统计切换城市事件
		StatService.onEvent(this, "city-setting", "reset-city", 1);
	}

	class WeatherTabChangeListener implements OnTabChangeListener {

		@Override
		public void onTabChanged(String tabId) {
			// 切换背景色
			Resources res = getResources();
			for (int i = 0, size = tabHost.getTabWidget().getChildCount(), current = tabHost.getCurrentTab(); i < size; i++) {
				final View tab = tabHost.getTabWidget().getChildTabViewAt(i);
				final TextView title = (TextView) tab.findViewById(android.R.id.title);
				final int color = i != current ? R.color.main_tab_default : R.color.main_tab_selected;
				title.setTextColor(res.getColor(color));
			}
			// 统计各个Tab点击情况
			String tabName = null;
			if (tabId == null || tabId.length() == 0) {
				tabName = "unknown";
			} else if ("forecast".equalsIgnoreCase(tabId)) {
				tabName = getResources().getString(R.string.forecast);
			} else if ("realtime".equalsIgnoreCase(tabId)) {
				tabName = getResources().getString(R.string.realtime);
			} else if ("trend".equalsIgnoreCase(tabId)) {
				tabName = getResources().getString(R.string.trend);
			} else if ("aqi".equalsIgnoreCase(tabId)) {
				tabName = getResources().getString(R.string.AQI_title);
			} else {
				tabName = "unknown";
			}
			StatService.onEvent(WeathermanActivity.this, "tabs", tabName, 1);
		}
	}

	class CityPromptClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// 打开修改城市页面
			final WeathermanActivity act = WeathermanActivity.this;
			act.startActivityForResult(new Intent(act, CityActivity.class), 1);
			// 统计切换城市事件
			StatService.onEvent(act, "city-setting", "change-manually", 1);
		}

	}

	class BaiduLocationListener implements BDLocationListener {

		private int count = 0;
		private int maxLocationTimes = 30;

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null) {
				return;
			}
			count++;
			Log.i(tag, "location: " + location.toJsonString());
			City c1 = null, c2 = null, c3 = null;
			String province = location.getProvince(), city = location.getCity(), district = location.getDistrict();
			if (province == null || province.length() == 0 || city == null || city.length() == 0 || district == null
					|| district.length() == 0) {
				Log.e(tag, "get location failed by round " + count);
			} else { // parse
				List<City> provinces = settingService.findCity(null);
				for (City prov : provinces) {
					if (province.contains(prov.getName()) || prov.getName().contains(province)) {
						c1 = prov;
						break;
					}
				}
				if (c1 != null) {
					List<City> cities = settingService.findCity(c1.getId());
					for (City cit : cities) {
						if (city.contains(cit.getName()) || cit.getName().contains(city)) {
							c2 = cit;
							break;
						}
					}
					if (c2 != null) {
						List<City> districts = settingService.findCity(c2.getId());
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
				resetCity(c1, c2, c3);
				this.stopLocation(); // 停止定时定位
			} else {
				ToastService
						.toastLong(getApplicationContext(), getResources().getString(R.string.city_location_failed));
				if (count > maxLocationTimes) {
					this.stopLocation();
				}
			}
		}

		private void stopLocation() {
			LocationClientOption option = locationClient.getLocOption();
			option.setScanSpan(0);
			locationClient.setLocOption(option);
			if (locationClient.isStarted()) {
				locationClient.stop();
			}
			Log.i(tag, "baidu location client stopped");
		}

		public void onReceivePoi(BDLocation location) {
			if (location == null) {
				return;
			}
			Log.i(tag, "poi: " + location.toJsonString());
		}

	}

	private long backTime = 0;

	@Override
	public void onBackPressed() {
		long time = System.currentTimeMillis();
		if (time - backTime > 2000) {
			backTime = time;
			ToastService.toast(this, "再按一次退出" + appName, Toast.LENGTH_SHORT);
		} else {
			this.addShortcut();
			super.onBackPressed();
		}
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